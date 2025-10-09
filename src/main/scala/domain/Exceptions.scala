package domain

object Exceptions {
  sealed trait MyException {
    def message: String
  }

  final case class SimpleException(message: String) extends MyException
  val noFile: MyException = SimpleException("No Such File")
  val noDir: MyException = SimpleException("No Such Directory")
}
