package lsx

import scala.xml.NodeSeq

case class Mesh(
  objectId: String,
  materialId: String,
  lod: String,
  order: Option[Int],
) extends XmlRepresentation {
  def xml: NodeSeq =
    <node id="Objects">
      <attribute id="LOD" type="uint8" value={lod}/>
      <attribute id="MaterialID" type="FixedString" value={materialId}/>
      <attribute id="ObjectID" type="FixedString" value={objectId}/>
    </node>
}
