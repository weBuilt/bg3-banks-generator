package ui.menu.project

import app.State
import app.controls.ProjectControls
import scalafx.scene.control.MenuItem
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.stage.DirectoryChooser
import ui.UIApp

object OpenProjectMenuItem {
  def apply(): MenuItem = new MenuItem {
    text = "Open"
    accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
    onAction = _ => ProjectMenu.saveOrDiscard(openProject)
  }

  def openProject(): Unit = {
    val directoryChooser = new DirectoryChooser {
      initialDirectory <== State.sourcesParent
    }
    val sources = directoryChooser.showDialog(UIApp.primaryStage)
    Option(sources).foreach(ProjectControls.openProject)
  }
}
