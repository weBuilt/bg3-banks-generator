package domain

import scala.xml.NodeSeq

case class TextureUsage(
  enabled: String = "True",
  exportAsPreset: String = "True",
  groupName: String = "01 Texture Map",
  id: String,
  parameterName: String,
  extraNodes: NodeSeq = NodeSeq.Empty,
) extends XmlRepresentation {
  val xml: NodeSeq = <node id="Texture2DParameters">
    <attribute id="Enabled" type="bool" value={enabled}/>
    <attribute id="ExportAsPreset" type="bool" value={exportAsPreset}/>
    <attribute id="GroupName" type="FixedString" value={groupName}/>
    <attribute id="ID" type="FixedString" value={id}/>
    <attribute id="ParameterName" type="FixedString" value={parameterName}/>
    {extraNodes}
  </node>
}
