package fileparser

import domain.BankElement.Generated
import domain.Texture
import util.FileUtils

import java.nio.file.Path

object DDSParser {
//  val powershellScriptPath: String = Paths.get("src/main/powershell/get-filemetadata.ps1").toAbsolutePath.toString
//  val kv: Regex = """([^:]*):(.*)""".r
  val specificValuesMap: Map[String, SpecificValues] = Map(
    "MSK" -> SpecificValues("62", "False", "MSK"),
    "BM" -> SpecificValues("62", "True", "BM"),
    "NM" -> SpecificValues("64", "False", "NM"),
    "PM" -> SpecificValues("62", "False", "PM"),
  )
  val parameterNames: Map[String, String] = Map(
    "MSK" -> "MSKColor",
    "BM" -> "basecolor",
    "NM" -> "normalmap",
    "PM" -> "physicalmap",
  )
  val typesWithSuffixes: List[(String, List[String])] = List(
    "MSK" -> List("msk", "mskcloth", "mskcolor"),
    "BM" -> List("bm", "basemap", "basecolor", "basecolour"),
    "NM" -> List("nm", "normal", "normalmap"),
    "PM" -> List("pm", "physical", "physicalmap"),
  )
  val typeBySuffix: List[(String, String)] = typesWithSuffixes.flatMap {
    case (tpe, suffixes) =>
      suffixes.map(_ -> tpe)
  }

  //todo run in 1 ps session ?
/*  def getAttributes(file: Path): Map[String, String] = {
    val cmd = s"""powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "& { . '$powershellScriptPath'; Get-FileMetaData '${file.toAbsolutePath.toString}'}" """
    val res = cmd.lazyLines.toList
    res.collect {
      case kv(k, v) =>
        (k.trim, v.trim)
    }.toMap
  }*/

  def parseTextures(files: List[Path], modSources: Path): List[Texture] =
    files.map { file =>
      val name = file.getFileName.toString.split("\\.").dropRight(1).mkString(".")
      texture(name, file, modSources, Map.empty)
      // looks like width and height do nothing. speed up by using default values
      //      attributes.get("Name").map(name => name.split("\\.").dropRight(1).mkString(".")).flatMap(texture(_, file, modSources, attributes))
    }

  def texture(
    name: String,
    file: Path,
    modSources: Path,
    attrs: Map[String, String],
  ): Texture = {
    val txtre = Texture(
      name = name,
      source = Generated,
      sourceFile = FileUtils.forwardslash(modSources.relativize(file)),
      format = "62",
      width = attrs.getOrElse("Width", "2048").filter(_.isDigit),
      height = attrs.getOrElse("Width", "2048").filter(_.isDigit),
      template = name,
    )
    specificValues(name) match {
      case Some((materialName, value)) =>
      txtre.copy(
        format = value.format,
        srgb = value.srgb,
        tpe = value.tpe,
        materialName = Some(materialName)
      )
      case _ =>
        txtre
    }
  }

  def specificValues(name: String): Option[(String, SpecificValues)] =
    processName(name).flatMap(t => specificValuesMap.get(t.tpe).map(t.name -> _))

  def processName(name: String): Option[MaterialNameWithTextureType] = {
    val lcName = name.toLowerCase
    typeBySuffix.collectFirst {
      case (suffix, tpe) if lcName.endsWith(suffix) =>
        MaterialNameWithTextureType(name.dropRight(suffix.length).reverse.dropWhile(!_.isLetterOrDigit).reverse, tpe)
    }
  }

  case class MaterialNameWithTextureType(name: String, tpe: String)

  case class SpecificValues(
    format: String,
    srgb: String,
    tpe: String,
  )
}
