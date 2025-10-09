package ui

import scalafx.scene.layout.BorderPane

object MainWindow {
  lazy val mainWindow: BorderPane = new BorderPane {
    top = Menu.menu
    bottom = StatusBar.statusBar
  }
}
