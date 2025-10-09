package lsx

import lsx.BankElement.BankElementSource

import java.nio.file.Path

trait BankElement extends XmlRepresentation {
  def bankName: String

  def source: BankElementSource
}

object BankElement {
  sealed trait BankElementSource

  case class Existing(bankFile: Path) extends BankElementSource

  case object Generated extends BankElementSource
}
