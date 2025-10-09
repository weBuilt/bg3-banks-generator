package util

import java.io.File
import java.nio.file.{Path, Paths}

object FileUtils {
  def forwardslash(str: String): String = str.replaceAll("\\\\", "/")

  def forwardslash(path: Path): String = forwardslash(path.toString)

  def filename(str: String): String = Paths.get(str).getFileName.toString

  def removeExtension(path: Path): String = {
    val filenameString = path.getFileName.toString
    val lastDot: Int = filenameString.lastIndexOf(".")
    if (lastDot >= 0) filenameString.substring(0, lastDot)
    else filenameString
  }

  def removeExtension(str: String): String = removeExtension(Paths.get(str))

  def extension(file: File): Option[String] = extension(file.toPath)

  def extension(str: String): Option[String] = extension(Paths.get(str))

  def extension(path: Path): Option[String] = {
    val filename = path.getFileName.toString
    val lastDot: Int = filename.lastIndexOf(".")
    Option.when(lastDot >= 0) {
      filename.substring(lastDot)
    }
  }
}
