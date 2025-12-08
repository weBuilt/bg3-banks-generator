package ui.menu

import scalafx.scene.control._

object MainMenuBar {
  lazy val mainMenuBar: MenuBar = new MenuBar {
    menus = project.ProjectMenu.projectMenu :: Nil
  }
}