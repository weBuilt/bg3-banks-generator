package domain

import scala.xml.NodeSeq

trait BankElement {
  def xmlRepr: NodeSeq
}
