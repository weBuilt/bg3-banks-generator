package domain

import scala.xml.NodeSeq

case class Bank(
  name: String,
  elements: List[BankElement]
) extends XmlRepresentation {
  override def xml: NodeSeq =
    <region id={name}>
      <node id={name}>
        <children>
          {elements.map(_.xml)}
        </children>
      </node>
    </region>
}
