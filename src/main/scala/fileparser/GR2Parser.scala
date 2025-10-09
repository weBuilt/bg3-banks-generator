package fileparser

import lsx.BankElement.Generated
import util.{FileUtils, UUID}
import lsx.{Mesh, Visual}

import java.nio.file.{Path, Paths}
import scala.sys.process._
import scala.util.matching.Regex

object GR2Parser {
  val meshRegex: Regex = """name=([^;]+);order=([^l]+);lod=(.+)""".r
  val templateRegex: Regex = """template=(.+)""".r

  def parse(path: Path, modSources: Path): Either[String, Visual] = {
    val parserPath = Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getParent.getParent.resolve(Paths.get("bin", "gr2-parser.exe"))
    val res: String = s"\"${parserPath.toAbsolutePath}\" \"${path.toAbsolutePath.toString}\"".!!
    val lines: Array[String] = res.split("\r\n")
    val meshes: Array[String] = lines.filter(_.startsWith("name"))
    val template: Option[String] = lines.filter(_.startsWith("template")).collectFirst {
      case templateRegex(templateName) => templateName
    }
    val name: String = FileUtils.removeExtension(path.getFileName)
    val relativePath: Path = modSources.relativize(path)
    val templateName: String = s"""${FileUtils.removeExtension(relativePath)}.${template.getOrElse("Dummy_Root")}.0"""
    Either.cond(
      meshes.nonEmpty,
      Visual(
        name = name,
        source = Generated,
        meshes = meshes.collect {
          case meshRegex(meshName, order, lod) =>
            Mesh(
              objectId = s"$name.$meshName.$order",
              materialId = UUID.empty,
              lod = lod,
              order = order.toIntOption,
            )
        }.toList,
        id = UUID.empty,
        sourceFile = FileUtils.forwardslash(relativePath),
        template = templateName,
      ),
      res,
    )
  }
}
