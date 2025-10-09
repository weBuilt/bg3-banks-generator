package ui

import app.State
import scalafx.scene.control.{Menu => FXMenu, MenuBar, MenuItem}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.stage.{DirectoryChooser, FileChooser}
import lsx.Meta

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
      val fromExisting = new MenuItem {
        text = "New From Existing Sources"
        accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown, KeyCombination.ShiftDown)
        onAction = _ => {
          val directoryChooser = new DirectoryChooser
          val sources = directoryChooser.showDialog(UIApp.primaryStage)
          val meta = Meta.find(sources)
          meta match {
            case Left(value) => println(value.message)
            case Right(value) =>
              State.meta.update(value)
              State.sources.update(sources.toString)
          }
          println("existing")
        }
      }
      val open = new MenuItem {
        text = "Open"
        accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
        onAction = _ => println("open")
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
        fromExisting ::
        open ::
        save ::
        recent ::
        Nil
    }
  }
}
