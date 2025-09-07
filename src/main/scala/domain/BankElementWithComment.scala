package domain

import scala.xml.{Comment, NodeSeq}

trait BankElementWithComment extends BankElement {
  def id: String

  def name: String

  def xmlBase: NodeSeq

  def xml: NodeSeq = Comment(s"$name $id") ++ xmlBase
}
