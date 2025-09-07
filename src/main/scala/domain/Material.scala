package domain

import scala.xml.NodeSeq

case class Material(
  name: String,
  source: BankElement.BankElementSource,
  id: String = "",
  materialType: String = "4",
  sourceFile: String = "Public/Shared/Assets/Materials/Characters/CHAR_BASE_AlphaTest_2S.lsf",
  originalFileVersion: String = "144115207403209030",
  textures: Seq[TextureUsage] = Nil,
  extraChildren: NodeSeq = Material.defaultExtra,
) extends BankElementWithComment {
  val bankName: String = Material.bankName

  val xmlBase: NodeSeq =
    <node id="Resource">
      <attribute id="DiffusionProfileUUID" type="FixedString" value=""/>
      <attribute id="ID" type="FixedString" value={id}/>
      <attribute id="MaterialType" type="uint8" value={materialType}/>
      <attribute id="Name" type="LSString" value={name}/>
      <attribute id="SourceFile" type="LSString" value={sourceFile}/>
      <attribute id="_OriginalFileVersion_" type="int64" value={originalFileVersion}/>
      <children>
        {extraChildren}{textures.map(_.xml)}{Material.magicalTexture}
      </children>
    </node>
}

object Material {
  val bankName: String = "MaterialBank"
  val magicalTexture: NodeSeq = <node id="Texture2DParameters">
    <attribute id="Enabled" type="bool" value="False"/>
    <attribute id="ExportAsPreset" type="bool" value="True"/>
    <attribute id="GroupName" type="FixedString" value=""/>
    <attribute id="ID" type="FixedString" value="2765e270-52ce-6f45-eddf-d0451042b54c"/>
    <attribute id="ParameterName" type="FixedString" value=""/>
  </node>
  val defaultExtra: NodeSeq = <node id="ScalarParameters">
    <attribute id="BaseValue" type="float" value="0.7"/>
    <attribute id="Enabled" type="bool" value="False"/>
    <attribute id="ExportAsPreset" type="bool" value="True"/>
    <attribute id="GroupName" type="FixedString" value=""/>
    <attribute id="ParameterName" type="FixedString" value="Armor_Dirt_Sharpness"/>
    <attribute id="Value" type="float" value="0.7"/>
  </node>
    <node id="ScalarParameters">
      <attribute id="BaseValue" type="float" value="0.5"/>
      <attribute id="Enabled" type="bool" value="False"/>
      <attribute id="ExportAsPreset" type="bool" value="True"/>
      <attribute id="GroupName" type="FixedString" value=""/>
      <attribute id="ParameterName" type="FixedString" value="Armor_Blood_Sharpness"/>
      <attribute id="Value" type="float" value="0.5"/>
    </node>
    <node id="ScalarParameters">
      <attribute id="BaseValue" type="float" value="4"/>
      <attribute id="Enabled" type="bool" value="False"/>
      <attribute id="ExportAsPreset" type="bool" value="False"/>
      <attribute id="GroupName" type="FixedString" value=""/>
      <attribute id="ParameterName" type="FixedString" value="Dirt_AO_Sharpness"/>
      <attribute id="Value" type="float" value="4"/>
    </node>
    <node id="ScalarParameters">
      <attribute id="BaseValue" type="float" value="4"/>
      <attribute id="Enabled" type="bool" value="False"/>
      <attribute id="ExportAsPreset" type="bool" value="False"/>
      <attribute id="GroupName" type="FixedString" value=""/>
      <attribute id="ParameterName" type="FixedString" value="Blood_AO_Sharpness"/>
      <attribute id="Value" type="float" value="4"/>
    </node>
    <node id="ScalarParameters">
      <attribute id="BaseValue" type="float" value="0"/>
      <attribute id="Enabled" type="bool" value="False"/>
      <attribute id="ExportAsPreset" type="bool" value="True"/>
      <attribute id="GroupName" type="FixedString" value=""/>
      <attribute id="ParameterName" type="FixedString" value="Dirt"/>
      <attribute id="Value" type="float" value="0"/>
    </node>
    <node id="ScalarParameters">
      <attribute id="BaseValue" type="float" value="0"/>
      <attribute id="Enabled" type="bool" value="False"/>
      <attribute id="ExportAsPreset" type="bool" value="True"/>
      <attribute id="GroupName" type="FixedString" value=""/>
      <attribute id="ParameterName" type="FixedString" value="Blood"/>
      <attribute id="Value" type="float" value="0"/>
    </node>
    <node id="ScalarParameters">
      <attribute id="BaseValue" type="float" value="0.5"/>
      <attribute id="Enabled" type="bool" value="False"/>
      <attribute id="ExportAsPreset" type="bool" value="True"/>
      <attribute id="GroupName" type="FixedString" value=""/>
      <attribute id="ParameterName" type="FixedString" value="Reflectance"/>
      <attribute id="Value" type="float" value="0.5"/>
    </node>
}

