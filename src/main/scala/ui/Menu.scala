package ui

import app.State
import app.controls.ProjectControls
import fileparser.lsx.Meta
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, MenuBar, MenuItem, Menu => FXMenu}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.stage.DirectoryChooser

object Menu {
  /**
   * Project:
   * -New (open dialogue for creating new mod)
   * -From Existing Sources (select sources directory, find meta.lsx, get name and author)
   * -Open (filechooser for project savestate files)
   * -Save (force save project state and generate lsx)
   * -Recent (submenu with last 10 savestates)
   *
   * savestate and config to appdata?
   */
  lazy val menu: MenuBar = new MenuBar {
    menus = projectMenu :: Nil
  }
  lazy val projectMenu: FXMenu = new FXMenu {
    text = "Project"
    items = {
      val nw = new MenuItem {
        text = "New"
        accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)
        onAction = _ => {
          println("new")
        }
      }
      val open = new MenuItem {
        text = "Open"
        accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown, KeyCombination.ShiftDown)
        onAction = _ => {
          println("open")
          if (State.meta.isNotNull.get) {
            val alert = new Alert(AlertType.Confirmation) {
              contentText = "Save current project?"
              headerText = None
              buttonTypes =
                ButtonType.Yes ::
                  ButtonType.No ::
                  ButtonType.Cancel ::
                  Nil
            }
            alert.initOwner(UIApp.primaryStage)
            alert.showAndWait() match {
              case Some(ButtonType.Yes) =>
                println("save current")
                openProject()
              case Some(ButtonType.No) =>
                println("discard current")
                openProject()
              case _ =>
                println("cancel")
            }
          } else openProject()
        }
      }
      val save = new MenuItem {
        text = "New"
        accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)
        onAction = _ => println("save")
      }
      val recent = new FXMenu {
        text = "Recent"
        items = Nil
      }
      nw ::
        open ::
        save ::
        recent ::
        Nil
    }
  }
  def openProject(): Unit = {
    val directoryChooser = new DirectoryChooser
    val sources = directoryChooser.showDialog(UIApp.primaryStage)
    ProjectControls.openProject(sources)
  }
}
