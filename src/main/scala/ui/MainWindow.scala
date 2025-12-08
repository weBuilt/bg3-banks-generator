package ui

import scalafx.scene.layout.BorderPane
import ui.menu.MainMenuBar

object MainWindow {
  lazy val mainWindow: BorderPane = new BorderPane {
    top = MainMenuBar.mainMenuBar
    bottom = StatusBar.statusBar
  }
}
