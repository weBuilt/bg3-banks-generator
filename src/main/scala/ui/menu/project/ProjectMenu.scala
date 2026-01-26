package ui.menu.project

import app.State
import app.controls.ProjectControls
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, Menu}
import ui.UIApp

object ProjectMenu {

  lazy val projectMenu: Menu = new Menu {
    text = "Project"
    items =
      NewProjectMenuItem() ::
        OpenProjectMenuItem() ::
        SaveProjectMenuItem() ::
        RecentProjectsMenu.recentProjectsMenu ::
        Nil
  }

  def confirmSaveAlert: Alert =
    new Alert(AlertType.Confirmation) {
      initOwner(UIApp.primaryStage)
      contentText = "Save current project?"
      headerText = None
      buttonTypes =
        ButtonType.Yes ::
          ButtonType.No ::
          ButtonType.Cancel ::
          Nil
    }

  def saveOrDiscard(next: () => Unit): Unit =
    if (State.ProjectState.meta.isNotNull.get) {
      val alert = confirmSaveAlert
      alert.showAndWait() match {
        case Some(ButtonType.Yes) =>
          ProjectControls.saveCurrentProject()
          next()
        case Some(ButtonType.No) => next()
        case _ => {}
      }
    } else next()
}
