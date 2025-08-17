package domain

import scala.xml.{Comment, NodeSeq}

case class Texture(
  name: String,
  id: String = "",
  sourceFile: String = "",
  format: String = "",
  width: String = "2048",
  height: String = "2048",
  depth: String = "1",
  localized: String = "False",
  srgb: String = "False",
  streaming: String = "True",
  template: String = "",
  tpe: String = "",
  materialName: String = "",
  fromFile: Boolean = false,
  hasLsx: Boolean = false,
) extends BankElement {
  val comment: Comment = Comment(s"$materialName $tpe $id")
  val xml: NodeSeq =
    <node id="Resource">
      <attribute id="Name" type="LSString" value={name}/>
      <attribute id="ID" type="FixedString" value={id}/>
      <attribute id="SourceFile" type="LSString" value={sourceFile}/>
      <attribute id="Format" type="uint32" value={format}/>
      <attribute id="Width" type="int32" value={width}/>
      <attribute id="Height" type="int32" value={height}/>
      <attribute id="Depth" type="int32" value={depth}/>
      <attribute id="SRGB" type="bool" value={srgb}/>
      <attribute id="Streaming" type="bool" value={streaming}/>
      <attribute id="Template" type="FixedString" value={template}/>
      <attribute id="Type" type="int32" value="1"/>
    </node>
  val xmlRepr: NodeSeq = comment ++ xml
}
