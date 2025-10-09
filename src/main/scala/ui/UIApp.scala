package ui

import app.Config
import io.circe.generic.extras.Configuration
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import javafx.stage.Screen
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Stage

import java.nio.file.Files

object UIApp extends JFXApp3 {
  implicit val customConfig: Configuration = Configuration.default.withDefaults

  def start(): Unit = {
    val savedState = loadWindowState()
    val screen = getScreenByNumber(savedState.screen)
    val (x, y, width, height) = adjustWindowPosition(savedState, screen)
    primaryStage.x = x
    primaryStage.y = y
    primaryStage.width = width
    primaryStage.height = height
    primaryStage.maximized = savedState.maximized
    stage = primaryStage
  }

  lazy val primaryStage: JFXApp3.PrimaryStage = new JFXApp3.PrimaryStage {
    title = "Baldur's Gate 3 Bank Manager"
    scene = new Scene(MainWindow.mainWindow)
    onCloseRequest = { _ =>
      saveWindowState(this)
    }
  }

  case class WindowState(
    x: Double = 0d,
    y: Double = 0d,
    width: Double = 800d,
    height: Double = 600d,
    screen: Int = 0,
    maximized: Boolean = false,
  )

  def saveWindowState(stage: Stage): Unit = {
    Files.createDirectories(Config.configDirectory)
    if (Files.notExists(Config.windowConfig)) Files.createFile(Config.windowConfig)
    val currentScreen = getScreenNumberByPosition(stage)

    val windowState = WindowState(
      x = stage.getX,
      y = stage.getY,
      width = stage.getWidth,
      height = stage.getHeight,
      screen = currentScreen,
      maximized = stage.maximized.value)
    Files.write(Config.windowConfig, windowState.asJson.spaces2.getBytes)
  }

  def loadWindowState(): WindowState =
    if (app.Config.configDirectory.toFile.exists()) {
      decode[WindowState](Files.readString(Config.windowConfig)).toOption.getOrElse(WindowState())
    } else WindowState()


  def getScreenByNumber(screenNumber: Int): Screen = {
    val screens = scalafx.stage.Screen.screens.toList
    if (screenNumber < screens.length) {
      screens(screenNumber)
    } else {
      scalafx.stage.Screen.primary
    }
  }

  def getScreenNumberByPosition(stage: Stage): Int = {
    def intersects(screen: Screen): Boolean =
      screen.getBounds.intersects(
        stage.getX,
        stage.getY,
        stage.getWidth,
        stage.getHeight,
      )

    scalafx.stage.Screen.screens.toList.zipWithIndex.collectFirst {
      case (screen, idx) if intersects(screen) =>
        idx
    }.getOrElse(0)
  }
  def adjustWindowPosition(
    settings: WindowState,
    screen: Screen,
  ): (Double, Double, Double, Double) = {
    val visualBounds = screen.getVisualBounds

    val maxX = visualBounds.getMaxX - settings.width
    val maxY = visualBounds.getMaxY - settings.height

    val adjustedWidth = math.min(settings.width, visualBounds.getWidth)
    val adjustedHeight = math.min(settings.height, visualBounds.getHeight)

    val x = if (settings.x < visualBounds.getMinX || settings.x > maxX) {
      (visualBounds.getMaxX - adjustedWidth) / 2 + visualBounds.getMinX
    } else {
      math.max(settings.x, visualBounds.getMinX)
    }

    val y = if (settings.y < visualBounds.getMinY || settings.y > maxY) {
      (visualBounds.getMaxY - adjustedHeight) / 2 + visualBounds.getMinY
    } else {
      math.max(settings.y, visualBounds.getMinY)
    }

    (x, y, adjustedWidth, adjustedHeight)
  }
}
