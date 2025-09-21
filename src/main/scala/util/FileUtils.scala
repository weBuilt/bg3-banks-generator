package util

import java.nio.file.{Path, Paths}

object FileUtils {
  def forwardslash(str: String): String = str.replaceAll("\\\\", "/")

  def forwardslash(path: Path): String = forwardslash(path.toString)

  def removeExtension(path: Path): String = {
    val filenameString = forwardslash(path)
    val lastDot: Int = filenameString.lastIndexOf(".")
    if (lastDot >= 0) filenameString.substring(0, lastDot)
    else filenameString
  }

  def removeExtension(str: String): String = removeExtension(Paths.get(str))
}
