package domain

import cats.data.ValidatedNec

object DataValidation {
  case class ValidationMessage(message: String)

  case class ValidatedData[Data](
    validated: ValidatedNec[ValidationMessage, Data],
    warnings: List[ValidationMessage] = Nil,
  )
}
