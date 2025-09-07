package domain

import scala.xml.NodeSeq

trait XmlRepresentation {
  def xml: NodeSeq
}
