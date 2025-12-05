package ui

import app.State
import app.controls.ProjectControls
import app.controls.ProjectControls.ValidatedNewProject
import cats.data.{Validated, ValidatedNec}
import cats.implicits._
import domain.DataValidation.{ValidatedData, ValidationMessage}
import domain.Exceptions.MyException
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Menu => FXMenu, _}
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.DirectoryChooser
import util.FileUtils

import scala.util.matching.Regex

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
          saveOrDiscard(newProject)
        }
      }
      val open = new MenuItem {
        text = "Open"
        accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)
        onAction = _ => saveOrDiscard(openProject)
      }
      val save = new MenuItem {
        text = "Save"
        accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)
        onAction = _ => ProjectControls.saveCurrentProject()
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

  def openProject(): Unit = {
    val directoryChooser = new DirectoryChooser {
      initialDirectory <== State.sourcesParent
    }
    val sources = directoryChooser.showDialog(UIApp.primaryStage)
    Option(sources).foreach(ProjectControls.openProject)
  }

  val allowedCharacters: Regex = """[A-Za-z_\d]+""".r

  def newProject(): Unit = {
    val name = ViewElements.textInput2(
      label = "Mod Name",
      validation = str => ValidatedData(
        if (allowedCharacters matches str) str.validNec
        else ValidationMessage("Name contains invalid characters.").invalidNec
      ),
      mandatory = true
    )
    val author = ViewElements.textInput2(
      label = "Mod Author",
      validation = str => ValidatedData(
        if (allowedCharacters matches str) str.validNec
        else ValidationMessage("Author contains invalid characters.").invalidNec
      ),
      mandatory = true
    )
    val sourceText = ViewElements.textInput2(
      label = "Mod Sources",
      validation = str => ValidatedData(
        if (FileUtils.isValidAbsolutePath(str)) str.validNec
        else ValidationMessage("Incorrect path.").invalidNec
      ),
      mandatory = true
    )
    val chooseButton = new Button("Browse...") {
      onAction = _ => {
        val directoryChooser = new DirectoryChooser {
          initialDirectory <== State.sourcesParent
        }
        val sources = directoryChooser.showDialog(UIApp.primaryStage)
        sourceText.rawValue.value = if (sources != null) sources.getAbsolutePath else ""
      }
    }
    val source = new HBox(10) {
      children = sourceText.node :: chooseButton :: Nil
    }
    val errors = new Label() {
      style = "-fx-text-fill: red; -fx-font-size: 12px;"
      visible = false
    }

    val pane: DialogPane = new DialogPane {
      buttonTypes =
        ButtonType.OK ::
          ButtonType.Cancel ::
          Nil
      content = new VBox(10) {
        children = errors :: name.node :: author.node :: source :: Nil
      }
      lookupButton(ButtonType.OK).disable <== !name.correct || !author.correct || !sourceText.correct
    }

    val newProjectDialog = new Dialog[ValidatedNec[MyException, Option[ValidatedNewProject]]] {
      initOwner(UIApp.primaryStage)
      title = "New Project"
      headerText = None
      dialogPane = pane

      resultConverter = {
        case ButtonType.OK =>
          ProjectControls.validateNewProject(name.rawValue(), author.rawValue(), sourceText.rawValue()).map(Some(_))
        case ButtonType.Cancel =>
          None.validNec[MyException]
      }

      onCloseRequest = ev => {
        result() match {
          case Validated.Valid(Some(project)) =>
            ProjectControls.initializeNewProject(project)
            ProjectControls.openProject(project.sources)
          case Validated.Invalid(e) =>
            errors.text = e.mkString_("\n")
            errors.visible = true
            ev.consume()
          case Validated.Valid(None) => {}
        }
      }
    }

    Platform.runLater(name.focusable.foreach(_.requestFocus()))
    newProjectDialog.show()
  }

  def saveOrDiscard(next: () => Unit): Unit =
    if (State.meta.isNotNull.get) {
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
