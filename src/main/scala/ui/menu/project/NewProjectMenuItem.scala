package ui.menu.project

import app.State
import app.controls.ProjectControls
import app.controls.ProjectControls.ValidatedNewProject
import cats.data.{Validated, ValidatedNec}
import cats.implicits._
import domain.DataValidation.{ValidatedData, ValidationMessage}
import domain.Exceptions.MyException
import scalafx.application.Platform
import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.DirectoryChooser
import ui.{UIApp, ViewElements}
import util.FileUtils

import scala.util.matching.Regex

object NewProjectMenuItem {
  def apply(): MenuItem = new MenuItem {
    text = "New"
    accelerator = new KeyCodeCombination(KeyCode.N, KeyCombination.ControlDown)
    onAction = _ => {
      ProjectMenu.saveOrDiscard(newProject)
    }
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
}
