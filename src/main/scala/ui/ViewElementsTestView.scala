package ui

import cats.implicits._
import domain.DataValidation
import scalafx.scene.layout.{Pane, VBox}

object ViewElementsTestView {
  val view = new Pane {
    children = ViewElements.textInput2(
      label = "test",
      str => {
        val warnings = List(
          Option.when(str.contains("test"))("test here."),
          Option.when(str.contains("TEST"))("Big test here."),
        ).flatten.map(DataValidation.ValidationMessage(_))
        val validated = if (str.exists(_.isDigit)) DataValidation.ValidationMessage("has digit!").invalidNec
        else str.validNec
        DataValidation.ValidatedData(validated, warnings)
      }
    ).node
  }
}
