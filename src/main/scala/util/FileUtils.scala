package util

import cats.data.Validated
import cats.implicits._
import domain.Exceptions._

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

object FileUtils {
  def forwardslash(str: String): String = str.replaceAll("\\\\", "/")

  def forwardslash(path: Path): String = forwardslash(path.toString)

  case class FilenameWithExtension(filename: String, extension: String)

  object FilenameWithExtension {
    def apply(path: Path): FilenameWithExtension = {
      val filenameString = path.getFileName.toString
      val lastDot: Int = filenameString.lastIndexOf(".")
      if (lastDot >= 0) FilenameWithExtension(filenameString.substring(0, lastDot), filenameString.substring(lastDot))
      else FilenameWithExtension(filenameString, "")
    }

    def unapply(path: Path): Option[FilenameWithExtension] = Some(apply(path))

    def apply(path: String): Validated[MyException, FilenameWithExtension] = validatedPath(path).map(apply)

    def unapply(path: String): Option[FilenameWithExtension] = pathOpt(path).map(apply)

    def apply(file: File): FilenameWithExtension = apply(file.toPath)

    def unapply(file: File): FilenameWithExtension = apply(file)
  }

  def isValidAbsolutePath(path: String): Boolean =
    Try {
      Paths.get(path).isAbsolute
    }.toOption.contains(true)

  def exists(path: String): Validated[MyException, Boolean] =
    validatedPath(path)
      .andThen { p =>
        Try {
          Files.exists(p)
        }.toValidated.myException
      }

  def pathOpt(path: String): Option[Path] = Try {
    Paths.get(path)
  }.toOption

  def validatedPath(path: String): Validated[MyException, Path] = Try {
    Paths.get(path)
  }.toValidated.myException
}
