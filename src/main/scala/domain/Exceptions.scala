package domain

import cats.Show

object Exceptions {
  trait MyException {
    def message: String
  }

  object MyException {
    implicit val myExceptionShow: Show[MyException] = _.message
  }

  final case class SimpleException(message: String) extends MyException
  val noFile: MyException = SimpleException("No Such File")
  val noDir: MyException = SimpleException("No Such Directory")
  val emptyInput: MyException = SimpleException("Empty Input")
}
