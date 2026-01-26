package domain

import cats.Show
import cats.data.{Validated, ValidatedNec}

object Exceptions {
  trait MyException {
    def message: String
  }

  object MyException {
    implicit val myExceptionShow: Show[MyException] = _.message

    def fromThrowable(throwable: Throwable): MyException = SimpleException(throwable.getMessage)

  }

  type MyValidated[T] = Validated[MyException, T]
  type MyValidatedNec[T] = ValidatedNec[MyException, T]

  implicit class ThrowableWrap[T](validatedWithThrowable: Validated[Throwable, T]) {
    def myException: MyValidated[T] = validatedWithThrowable.leftMap(MyException.fromThrowable)
  }

  final case class SimpleException(message: String) extends MyException

  val noFile: MyException = SimpleException("No Such File")
  val noDir: MyException = SimpleException("No Such Directory")
  val malformedPath: MyException = SimpleException("Malformed Path")
  val emptyInput: MyException = SimpleException("Empty Input")
}
