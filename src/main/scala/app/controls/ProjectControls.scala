package app.controls

import app.Config._
import app.controls.DefaultFiles.DefaultStructure
import app.{Config, State}
import cats.data._
import cats.syntax.all._
import domain.Exceptions.MyException
import fileparser.lsx.Meta
import io.circe.generic.auto._
import io.circe.generic.extras.Configuration
import io.circe.parser.decode
import io.circe.syntax._

import java.io.File
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object ProjectControls {

  implicit val customConfig: Configuration = Configuration.default.withDefaults

  def openProject(reference: ProjectReference): Unit =
    openProject(Paths.get(reference.sources).toFile)

  def openProject(sources: File): Unit = {
    val meta = Meta.find(sources)
    meta match {
      case Left(value) => println(value.message)
      case Right(value) =>
        State.meta.update(value)
        State.sources.update(sources.toString)
    }
  }

  def addToRecent(reference: ProjectReference): Unit = {
    val currentConfig = Option.when(Files.exists(Config.appConfig)) {
      decode[Config.AppConfiguration](Files.readString(Config.appConfig))
    }.flatMap(_.toOption)
    val currentRecent = currentConfig.toList.flatMap(_.recent)
    val newRecent = reference :: currentRecent.filterNot(_ == reference).take(9)
    val configurationJson = AppConfiguration(
      State.currentReference().orElse(currentConfig.flatMap(_.lastProject)),
      newRecent,
    ).asJson
    Files.writeString(Config.appConfig, configurationJson.spaces2)
  }

  def init(): Unit =
    Option.when(Files.exists(appConfig)) {
        decode[AppConfiguration](Files.readString(appConfig))
      }.flatMap(_.toOption)
      .foreach { appConfig =>
        appConfig.lastProject.foreach(openProject)
      }

  def saveCurrentProject(): Unit = {
    //serialization
    State.currentReference().foreach(addToRecent)
    State.currentProjectConfig().foreach { project =>
      Files.writeString(Config.projectConfig(project.reference), project.asJson.spaces2)
    }
  }

  case class ValidatedNewProject(name: String, author: String, sources: File)

  def initializeNewProject(project: ValidatedNewProject): Unit = {
    if (!project.sources.exists()) project.sources.mkdir()
    val DefaultStructure(defaultDirs, defaultFiles) = DefaultFiles.defaultStructure(project.name)
    defaultDirs
      .map(project.sources.toPath.resolve)
      .foreach(Files.createDirectories(_))
    defaultFiles
      .map(project.sources.toPath.resolve)
      .foreach(Files.createFile(_))
    DefaultFiles.init(project)
  }

  case object DriveNotFound extends MyException {
    def message: String = "Drive not found."
  }

  case object InsufficientPermissions extends MyException {
    def message: String = "Insufficient permissions."
  }

  def validateNewProjectPathAvailability(path: String): ValidatedNec[MyException, File] = {
    val file = Paths.get(path).toFile

    @tailrec
    def inner(f: File): ValidatedNec[MyException, File] =
      if (f == null) DriveNotFound.invalidNec
      else if (f.exists()) {
        Try {
          val tmp = f.toPath.resolve("wblt_tmp")
          val tmpFile = tmp.resolve("wblt.tmp")
          Files.createDirectories(tmp)
          Files.createFile(tmpFile)
          Files.delete(tmpFile)
          Files.delete(tmp)
        } match {
          case Failure(exception) => InsufficientPermissions.invalidNec
          case Success(_) => file.validNec
        }
      } else inner(f.getParentFile)

    inner(file)
  }

  //name & author inputs are checked in form. nonempty & theoretically correct path is checked in form
  def validateNewProject(name: String, author: String, sources: String): ValidatedNec[MyException, ValidatedNewProject] =
    validateNewProjectPathAvailability(sources)
      .map(ValidatedNewProject(name, author, _))

}
