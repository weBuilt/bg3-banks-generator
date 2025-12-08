package ui.menu.project

import app.controls.ProjectControls
import scalafx.scene.control.MenuItem
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

object SaveProjectMenuItem {
  def apply(): MenuItem = new MenuItem {
    text = "Save"
    accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)
    onAction = _ => ProjectControls.saveCurrentProject()
  }
}
