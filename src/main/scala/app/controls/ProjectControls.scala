package app.controls

import app.Config._
import app.controls.DefaultFiles.DefaultStructure
import app.{Config, State}
import cats.data._
import cats.syntax.all._
import domain.Exceptions._
import fileparser.lsx.Meta
import io.circe.generic.auto._
import io.circe.generic.extras.Configuration
import io.circe.parser.decode
import io.circe.syntax._
import util.FileUtils

import java.io.File
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object ProjectControls {

  implicit val jsonDefaultsOn: Configuration = Configuration.default.withDefaults

  def openProject(reference: ProjectReference): MyValidated[Unit] =
    FileUtils.validatedPath(reference.sources)
      .andThen { path => openProject(path.toFile, Some(reference.folder)) }

  def openProject(sources: File, folder: Option[String]): MyValidated[Unit] =
    Meta.find(sources, folder)
      .map { meta =>
        State.ProjectState.meta.update(meta)
        State.ProjectState.sources.update(sources.toString)
      }


  /** empty config is first run, corrupted config is something went wrong */
  def readAppConfiguration: MyValidated[AppConfiguration] =
    if (Files.notExists(Config.appConfig)) AppConfiguration().valid[MyException]
    else decode[Config.AppConfiguration](Files.readString(Config.appConfig))
      .toValidated.myException

  def saveAppConfiguration(
    appConfiguration: Option[AppConfiguration] = None,
  ): MyValidated[Unit] = Try {
    Files.writeString(Config.appConfig, appConfiguration.getOrElse(State.AppState.config()).asJson.spaces2)
  }.toValidated.void.myException

  def saveCurrentProjectConfiguration(
    projectConfiguration: Option[ProjectConfiguration] = None,
  ): MyValidated[Unit] = Try {
    projectConfiguration
      .orElse(State.ProjectState.config())
      .foreach { project =>
        Files.writeString(Config.projectConfig(project.reference), project.asJson.spaces2)
      }
  }.toValidated.myException

  /** check on startup and every project save
   * read from file every time in case of multiple instances running */
  def readRecentProjects(
    appConfiguration: Option[AppConfiguration] = None,
  ): MyValidated[List[ProjectReference]] =
    appConfiguration.map(_.valid[MyException])
      .getOrElse(readAppConfiguration)
      .map { appConfig =>
        appConfig.recentProjects
          .filter { reference =>
            FileUtils.pathOpt(reference.sources)
              .filter(Files.exists(_))
              .exists { sources =>
                Files.exists(sources.resolve(Meta.path(reference.folder)))
              }
          }
      }

  def addToRecentProjects(reference: ProjectReference): MyValidated[Unit] = {
    val recentProjects = readRecentProjects().getOrElse(Nil)
    val updatedRecentProjects = reference :: recentProjects.filterNot(_.sources == reference.sources).take(9)
    State.AppState.recentProjects.clear()
    State.AppState.recentProjects.addAll(updatedRecentProjects)
    saveAppConfiguration()
  }

  //open on startup setting
  //don't automatically open any project if already opened in other instance
  //some default view with open project menu
  def init(): MyValidatedNec[Unit] =
    readAppConfiguration.toValidatedNec
      .andThen { appConfiguration: AppConfiguration =>
        State.AppState.recentProjects.clear()
        State.AppState.recentProjects.addAll(appConfiguration.recentProjects)
        appConfiguration.recentProjects.headOption match {
          case Some(value) => openProject(value).toValidatedNec
          case None => ().validNec[MyException]
        }
      }

  def saveCurrentProject(): MyValidatedNec[Unit] = {
    //todo serialization
    val serialization = ().valid[MyException]
    val actions: List[MyValidated[Unit]] =
      serialization ::
        saveCurrentProjectConfiguration() ::
        State.ProjectState.reference().map(addToRecentProjects).toList
    actions.map(_.toValidatedNec).sequenceVoid
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
