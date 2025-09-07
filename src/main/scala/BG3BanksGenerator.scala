import domain.BankElement.{Existing, Generated}
import domain._
import fileparser.DDSParser
import fileparser.granny.GR2Parser
import xmlparser.LSXParser

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.annotation.tailrec
import scala.util.Try
import scala.util.matching.Regex

object BG3BanksGenerator
  extends App {
  /** enter mod's root directory here
   * for example """G:\BG3 Modding\bg3-modders-multitool\UnpackedMods\WBLT_Example""" */
  val modSources: String = """"""
  /** enter output subpathes if needed. leave empty for default
   * textures: Public\{Mod name}\Content\Assets\Characters\[PAK]_Armor\textures.lsx
   * materials: Public\{Mod name}\Content\Assets\Characters\[PAK]_Armor\materials.lsx
   * visuals: Public\{Mod name}\Content\Assets\Characters\[PAK]_Armor\visuals.lsx */
  val textureBankOutput: String = """"""
  val materialBankOutput: String = """"""
  val visualBankOutput: String = """"""

  /** that's all folks */

  val modSourcesPath: Path = Paths.get(modSources).normalize()
  val modname: String = modSourcesPath.getParent.relativize(modSourcesPath).toString
  val assetsPath: Path = modSourcesPath.resolve(Paths.get("Generated", "Public", modname))
  val allLsxPath: Path = modSourcesPath.resolve(Paths.get("Public", modname, "Content"))
  val defaultLsxPath: Path = Paths.get("Public", modname, "Content", "Assets", "Characters", "[PAK]_Armor")
  //check and prepare output paths

  def checkPath(path: String): Try[Path] = Try {
    Paths.get(path)
  }.filter(_.toString.nonEmpty)

  val textureBankPath: Path = modSourcesPath.resolve(checkPath(textureBankOutput).getOrElse(defaultLsxPath.resolve("textures.lsx")))
  val materialBankPath: Path = modSourcesPath.resolve(checkPath(materialBankOutput).getOrElse(defaultLsxPath.resolve("materials.lsx")))
  val visualBankPath: Path = modSourcesPath.resolve(checkPath(visualBankOutput).getOrElse(defaultLsxPath.resolve("visuals.lsx")))
  val bankPaths: List[String] = List(textureBankPath, materialBankPath, visualBankPath).map(_.toString).distinct
  //get existing banks from all files under Public/modname/Content directory

  @tailrec
  def recursiveGetFile(filter: File => Boolean, acc: List[Path], paths: List[File]): List[Path] =
    paths match {
      case x :: xs =>
        if (x.isDirectory) recursiveGetFile(filter, acc, x.listFiles().toList ::: xs)
        else recursiveGetFile(filter, if (filter(x)) x.toPath :: acc else acc, xs)
      case _ => acc
    }

  val lsxFiles: List[Path] = recursiveGetFile(_.getName.toLowerCase.endsWith(".lsx"), Nil, allLsxPath.toFile :: Nil)
  //filter .lsf.lsx and .lsx pairs that created by unpacking a mod with both lsf and lsx files
  val filteredLsxFiles = lsxFiles.filterNot{file =>
    file.toFile.getName.endsWith(".lsf.lsx") && {
      val lsxpair = file.toFile.getName.dropRight(8) + ".lsx"
      lsxFiles.exists(_.toFile.getName == lsxpair)
    }
  }
  val processedLsxFiles: List[(Path, LSXParser.ParseResult)] = filteredLsxFiles.map(file => file -> LSXParser.parse(file))
  val existingBankElements: List[BankElement] = processedLsxFiles.flatMap(_._2.elements)
  //cleanup

  val (toUpdate, toDelete) = processedLsxFiles.partition(_._2.hasUnsupported)
  toUpdate.map {
    case (path, result) if bankPaths.contains(path.toString) =>
      path.getParent.resolve("_unsupported" + path.getFileName) -> result
    case e => e
  }.foreach {
    case (path, LSXParser.ParseResult(_, Some(unsupported))) =>
      Files.write(path, unsupported.getBytes(StandardCharsets.UTF_8))
    case _ => {}
  }
  toDelete.foreach(Files delete _._1)


  //initialize files if not exists
  if (!textureBankPath.toFile.exists()) Files.createFile(textureBankPath)
  if (!materialBankPath.toFile.exists()) Files.createFile(materialBankPath)
  if (!visualBankPath.toFile.exists()) Files.createFile(visualBankPath)

  val existingElementsMap: Map[String, List[BankElement]] = existingBankElements.groupBy(_.bankName)

  val ddsAssets: List[Path] = recursiveGetFile(_.getName.toLowerCase.endsWith(".dds"), Nil, assetsPath.toFile :: Nil)
  val gr2Assets: List[Path] = recursiveGetFile(_.getName.toLowerCase.endsWith(".gr2"), Nil, assetsPath.toFile :: Nil)

  val texturesFromFiles: List[Texture] = DDSParser.parseTextures(ddsAssets, modSourcesPath)

  def concatOldWithNew[Element <: BankElement](
    bankName: String,
    updates: List[Element],
    update: (Element, Element) => Element,
    equals: Element => Element => Boolean,
    setId: String => Element => Element,
  ): List[Element] = {
    val existingElements = existingElementsMap.getOrElse(bankName, Nil).collect {
      case element: Element => element
    }
    val (nnew, upd) = updates.partitionMap { element =>
      val existingElement = existingElements.filter(equals(element))
      Option.when(existingElement.nonEmpty)(
        element -> existingElement
      ).toRight(element)
    }
    val old = existingElements.filterNot(t => updates.exists(equals(t)))
    nnew.map(setId(java.util.UUID.randomUUID.toString)) :::
      upd.flatMap { case (u, o) => o.map(update(u, _)) } :::
      old
  }

  val allTextures: List[Texture] = concatOldWithNew[Texture](
    bankName = Texture.bankName,
    updates = texturesFromFiles,
    update = (u, o) => u.copy(id = o.id, source = o.source),
    equals = a => b => Paths.get(a.sourceFile).relativize(Paths.get(b.sourceFile)).equals(Path.of("")),
    setId = id => _.copy(id = id),
  ).sortBy(_.name)
    .map(_.copy(source = Existing(textureBankPath)))

  val materialsFromTextures: List[Material] = allTextures.groupBy(_.materialName).toList.collect {
    case (Some(materialName), textures) =>
      Material(
        name = materialName,
        source = Generated,
        textures = textures.map { texture =>
          TextureUsage(
            id = texture.id,
            parameterName = DDSParser.parameterNames.getOrElse(texture.tpe, ""),
          )
        }
      )
  }

  val allMaterials: List[Material] = concatOldWithNew[Material](
    bankName = Material.bankName,
    updates = materialsFromTextures,
    update = (u, o) => o.copy(
      textures = u.textures ++ o.textures.filterNot(u.textures.map(_.parameterName) contains _.parameterName),
    ),
    equals = a => b => a.name == b.name,
    setId = id => _.copy(id = id),
  ).sortBy(_.name)
    .map(_.copy(source = Existing(materialBankPath)))

  val visualsFromFiles = gr2Assets
    .map { path => path -> GR2Parser.parse(path, modSourcesPath) }
    .foldLeft(List.empty[Visual]) {
      case (acc, (path, Left(error))) =>
        System.err.println(s"error: ${path.getFileName} not processed. $error")
        acc
      case (acc, (path, Right(value))) =>
        println(s"success: ${path.getFileName} has ${value.meshes.size} meshes")
        value :: acc
    }
  val allVisuals = concatOldWithNew[Visual](
    bankName = Visual.bankName,
    updates = visualsFromFiles,
    update = (u, o) => o.copy(
      meshes = o.meshes ::: u.meshes.drop(o.meshes.size),
    ),
    equals = a => b => Paths.get(a.sourceFile).relativize(Paths.get(b.sourceFile)).equals(Path.of("")),
    setId = id => _.copy(id = id),
  ).sortBy(_.name)
    .map(_.copy(source = Existing(visualBankPath)))
  val resultingElements: List[BankElement] =
    allVisuals ::: allMaterials ::: allTextures

  resultingElements.groupBy(_.source).foreach {
    case (Existing(path), elements) =>
      Files.write(path, LSXParser.generate(elements).getBytes(StandardCharsets.UTF_8))
  }
}
