package ui

import cats.data.Validated
import cats.implicits._
import domain.DataValidation.ValidatedData
import scalafx.beans.binding.{Bindings, BooleanBinding, ObjectBinding}
import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control.{ContentDisplay, Label, TextField, Tooltip}
import scalafx.util.Duration

object ViewElements {

  object css {
    val validated = "validated"
    val valid = "valid"
    val warning = "warning"
    val invalid = "invalid"
    val validatedSubclasses = List(invalid, warning, valid)
    val tooltipHidden = "tooltip-hidden"
  }

  type ValidatedValue[Data] = ObjectBinding[ValidatedData[Data]]

  case class TextInput2(
    rawValue: StringProperty,
    validatedValue: ValidatedValue[String],
    node: Node,
    focusable: Option[Node],
    mandatory: Boolean,
  ) {
    def correct: BooleanBinding = Bindings.createBooleanBinding(
      () => validatedValue().validated.exists {
        value => !(mandatory && value.isEmpty)
      },
      validatedValue,
    )
  }


  def textInput2(
    label: String,
    validation: String => ValidatedData[String] = s => ValidatedData(s.validNec),
    mandatory: Boolean = false,
  ): TextInput2 = {
    val textTooltip = new Tooltip() {
      styleClass add css.tooltipHidden
      styleClass add "tooltip-popup"
      showDuration = Duration.Indefinite
    }
    val tf = new TextField() {
      styleClass add css.validated
      tooltip = textTooltip
    }
    val labelProperty =
      if (mandatory) Bindings.createStringBinding(
        () => if (tf.text().isEmpty) label + "*" else label,
        tf.text,
      ) else StringProperty(label)
    val labelNode = new Label {
      text <== labelProperty
      graphic = tf
      contentDisplay = ContentDisplay.Right
    }
    tf.text.onChange {
      (_, _, newText) =>
        tf.styleClass.removeAll(css.validatedSubclasses: _*)
        if (newText.isEmpty) {}
        else {
          val validated = validation(newText)
          val errors = {
            {
              validated.validated match {
                case Validated.Invalid(e) =>
                  e.toList
                case _ => Nil
              }
            } ::: validated.warnings
          }.map(_.message).mkString("\n")
          textTooltip.text = errors
          if (validated.validated.isInvalid) {
            tf.styleClass add css.invalid
            textTooltip.styleClass remove css.tooltipHidden
          } else if (validated.warnings.nonEmpty) {
            tf.styleClass add css.warning
            textTooltip.styleClass remove css.tooltipHidden
          } else {
            tf.styleClass add css.valid
            if (!textTooltip.styleClass.contains(css.tooltipHidden)) textTooltip.styleClass add css.tooltipHidden
          }
        }
    }
    val validatedBinding = Bindings.createObjectBinding(
      () => validation(tf.text()),
      tf.text,
    )

    TextInput2(
      tf.text,
      validatedBinding,
      labelNode,
      Some(tf),
      mandatory,
    )
  }
}
