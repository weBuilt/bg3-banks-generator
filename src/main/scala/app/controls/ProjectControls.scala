package app.controls

import app.Config._
import app.{Config, State}
import fileparser.lsx.Meta
import io.circe.generic.auto._
import io.circe.generic.extras.Configuration
import io.circe.parser.decode
import io.circe.syntax._

import java.io.File
import java.nio.file.{Files, Paths}

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
    val newRecent = reference :: (currentRecent.filterNot(_ == reference)).take(9)
    val configurationJson = AppConfiguration(
      currentConfig.flatMap(_.lastProject).orElse(Config.currentReference()),
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
}
