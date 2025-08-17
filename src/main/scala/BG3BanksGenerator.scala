import domain.{BankElement, Material, Texture, TextureUsage}
import fileparser.DDSParser
import xmlparser.LSXParser

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.annotation.tailrec
import scala.xml.PrettyPrinter

object BG3BanksGenerator
  extends App {
  /** enter mod's root folder here
   * for example """G:\BG3 Modding\bg3-modders-multitool\UnpackedMods\WBLT_Example""" */
  val modSources: String = """"""
  /** enter output subpathes if needed. leave empty for default
   * textures: Public\{Mod name}\Content\Assets\Characters\[PAK]_Armor\textures.lsx
   * materials: Public\{Mod name}\Content\Assets\Characters\[PAK]_Armor\materials.lsx */
  val textureBankOutput: String = """"""
  val materialBankOutput: String = """"""
  /** that's all folks */
  val modSourcesPath: Path = Paths.get(modSources)
  val modname: String = modSourcesPath.getParent.relativize(modSourcesPath).toString
  val textureBankPath: Path = modSourcesPath.resolve(
    Option.when(textureBankOutput.isBlank)(
      Paths.get(
        "Public",
        modname,
        "Content",
        "Assets",
        "Characters",
        "[PAK]_Armor",
        "textures.lsx",
      )
    ).getOrElse(Paths.get(textureBankOutput))
  )
  val materialBankPath: Path = modSourcesPath.resolve(
    Option.when(materialBankOutput.isBlank)(
      Paths.get(
        "Public",
        modname,
        "Content",
        "Assets",
        "Characters",
        "[PAK]_Armor",
        "materials.lsx",
      )
    ).getOrElse(Paths.get(materialBankOutput))
  )
  val assetsPath: Path = modSourcesPath.resolve(
    Paths.get(
      "Generated",
      "Public",
      modname,
    )
  )
  //initialize files if not exists
  if (!textureBankPath.toFile.exists()) Files.createFile(textureBankPath)
  if (!materialBankPath.toFile.exists()) Files.createFile(materialBankPath)
  val sameFileTexturesAndMaterials: Boolean = Files.isSameFile(textureBankPath, materialBankPath)
  val (existingTextures, existingMaterials) = {
    val parsedTextures: List[BankElement] = LSXParser.parse(textureBankPath)
    val textures: List[Texture] = parsedTextures.collect { case texture: Texture => texture }
    val materials: List[Material] = {
      if (sameFileTexturesAndMaterials) parsedTextures
      else LSXParser.parse(materialBankPath)
    }.collect { case material: Material => material }
    (textures, materials)
  }

  @tailrec
  def recursiveGetFile(filter: File => Boolean, acc: List[Path], paths: List[File]): List[Path] =
    paths match {
      case x :: xs =>
        if (x.isDirectory) recursiveGetFile(filter, acc, x.listFiles().toList ::: xs)
        else recursiveGetFile(filter, if (filter(x)) x.toPath :: acc else acc, xs)
      case _ => acc
    }

  val ddsAssets: List[Path] = recursiveGetFile(_.getName.toLowerCase.endsWith(".dds"), Nil, assetsPath.toFile :: Nil)
  val texturesFromFiles: List[Texture] = DDSParser.parseTextures(ddsAssets, modSourcesPath)
  val allTextures: List[Texture] = {
    val (nnew, upd) = texturesFromFiles.partitionMap { texture =>
      val existingTexture = existingTextures.find(_.name == texture.name)
      existingTexture.map(texture -> _).toRight(texture)
    }
    val old = existingTextures.filterNot(t => upd.exists(t.name == _._1.name))
    nnew.map(_.copy(id = java.util.UUID.randomUUID.toString)) :::
      upd.map { case (u, o) => u.copy(id = o.id, hasLsx = true) } :::
      old
  }.sortBy(_.name)
  val materialsFromTextures: List[Material] = allTextures.groupBy(_.materialName).toList.map {
    case (materialName, textures) =>
      Material(
        name = materialName,
        textures = textures.map { texture =>
          TextureUsage(
            id = texture.id,
            parameterName = DDSParser.parameterNames.getOrElse(texture.tpe, ""),
          )
        }
      )
  }
  val allMaterials: List[Material] = {
    val (nnew, upd) = materialsFromTextures.partitionMap { material =>
      val existingMaterial = existingMaterials.find(_.name == material.name)
      existingMaterial.map(material -> _).toRight(material)
    }
    val old = existingMaterials.filterNot(t => upd.exists(t.name == _._1.name))
    nnew.map(_.copy(id = java.util.UUID.randomUUID.toString)) :::
      upd.map { case (u, o) => u.copy(id = o.id, extraChildren = o.extraChildren) } :::
      old
  }.sortBy(_.name)
  val prettyPrinter = new PrettyPrinter(300, 4)
  if (sameFileTexturesAndMaterials) {
    val outputxml = <save>
      <version major="4" minor="0" revision="9" build="0" lslib_meta="v1,bswap_guids,lsf_adjacency"/>
      <region id="MaterialBank">
        <node id="MaterialBank">
          <children>
            {allMaterials.map(_.xmlRepr)}
          </children>
        </node>
      </region>
      <region id="TextureBank">
        <node id="TextureBank">
          <children>
            {allTextures.map(_.xmlRepr)}
          </children>
        </node>
      </region>
    </save>
    val outputText = """<?xml version="1.0" encoding="utf-8"?>""" + "\n" + prettyPrinter.format(outputxml, scala.xml.TopScope)
    Files.write(textureBankPath, outputText.getBytes(StandardCharsets.UTF_8))
  } else {
    val outputMaterialsXML = <save>
      <version major="4" minor="0" revision="9" build="0" lslib_meta="v1,bswap_guids,lsf_adjacency"/>
      <region id="MaterialBank">
        <node id="MaterialBank">
          <children>
            {allMaterials.map(_.xmlRepr)}
          </children>
        </node>
      </region>
    </save>
    val outputMaterialsText = """<?xml version="1.0" encoding="utf-8"?>""" + "\n" + prettyPrinter.format(outputMaterialsXML, scala.xml.TopScope)
    Files.write(materialBankPath, outputMaterialsText.getBytes(StandardCharsets.UTF_8))
    val outputTexturesXML = <save>
      <version major="4" minor="0" revision="9" build="0" lslib_meta="v1,bswap_guids,lsf_adjacency"/>
      <region id="TextureBank">
        <node id="TextureBank">
          <children>
            {allTextures.map(_.xmlRepr)}
          </children>
        </node>
      </region>
    </save>
    val outputTexturesText = """<?xml version="1.0" encoding="utf-8"?>""" + "\n" + prettyPrinter.format(outputTexturesXML, scala.xml.TopScope)
    Files.write(textureBankPath, outputTexturesText.getBytes(StandardCharsets.UTF_8))
  }

}
