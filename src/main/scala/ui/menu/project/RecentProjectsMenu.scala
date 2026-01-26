package ui.menu.project

import app.Config.ProjectReference
import app.controls.ProjectControls
import scalafx.scene.control._

object RecentProjectsMenu {
  lazy val recentProjectsMenu: Menu = new Menu {
    text = "Recent"
    items = recentProjectsMenuItems()
  }
  app.State.AppState.recentProjects.onChange {
    recentProjectsMenu.items = recentProjectsMenuItems()
  }

  def recentProjectMenuItem(reference: ProjectReference): MenuItem = new MenuItem {
    text = reference.name
    onAction = _ => ProjectMenu.saveOrDiscard(() => ProjectControls.openProject(reference))
  }

  def recentProjectsMenuItems(): List[MenuItem] =
    app.State.AppState.recentProjects
      .filterNot(app.State.ProjectState.isCurrent)
      .map(recentProjectMenuItem)
      .toList
}
