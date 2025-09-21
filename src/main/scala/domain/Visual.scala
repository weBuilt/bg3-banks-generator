package domain

import util.PackedVersion

import scala.xml.NodeSeq

case class Visual(
  name: String,
  source: BankElement.BankElementSource,
  meshes: List[Mesh],
  id: String,
  sourceFile: String,
  template: String,
  attachBone: String = "",
  attachmentSkeletonResource: String = "",
  blueprintInstanceResourceID: String = "",
  boundsMax: String = "0.59605384 1.5662533 0.124864005",
  boundsMin: String = "-0.59605384 0.93167603 -0.16559535",
  clothColliderResourceID: String = "",
  hairPresetResourceId: String = "",
  hairType: String = "0",
  materialType: String = "0",
  needsSkeletonRemap: String = "False",
  remapperSlotId: String = "",
  scalpMaterialId: String = "",
  skeletonResource: String = "",
  skeletonSlot: String = "",
  slot: String = "Body",
  softbodyResourceID: String = "",
  supportsVertexColorMask: String = "True",
  originalFileVersion: String = PackedVersion.defaultVersion.string64,
  extraChildren: NodeSeq = Visual.defaultExtra,
) extends BankElementWithComment {
  val bankName: String = Visual.bankName

  val xmlBase: NodeSeq =
    <node id="Resource">
      <attribute id="AttachBone" type="FixedString" value={attachBone}/>
      <attribute id="AttachmentSkeletonResource" type="FixedString" value={attachmentSkeletonResource}/>
      <attribute id="BlueprintInstanceResourceID" type="FixedString" value={blueprintInstanceResourceID}/>
      <attribute id="BoundsMax" type="fvec3" value={boundsMax}/>
      <attribute id="BoundsMin" type="fvec3" value={boundsMin}/>
      <attribute id="ClothColliderResourceID" type="FixedString" value={clothColliderResourceID}/>
      <attribute id="HairPresetResourceId" type="FixedString" value={hairPresetResourceId}/>
      <attribute id="HairType" type="uint8" value={hairType}/>
      <attribute id="ID" type="FixedString" value={id}/>
      <attribute id="MaterialType" type="uint8" value={materialType}/>
      <attribute id="Name" type="LSString" value={name}/>
      <attribute id="NeedsSkeletonRemap" type="bool" value={needsSkeletonRemap}/>
      <attribute id="RemapperSlotId" type="FixedString" value={remapperSlotId}/>
      <attribute id="ScalpMaterialId" type="FixedString" value={scalpMaterialId}/>
      <attribute id="SkeletonResource" type="FixedString" value={skeletonResource}/>
      <attribute id="SkeletonSlot" type="FixedString" value={skeletonSlot}/>
      <attribute id="Slot" type="FixedString" value={slot}/>
      <attribute id="SoftbodyResourceID" type="FixedString" value={softbodyResourceID}/>
      <attribute id="SourceFile" type="LSString" value={sourceFile}/>
      <attribute id="SupportsVertexColorMask" type="bool" value={supportsVertexColorMask}/>
      <attribute id="Template" type="FixedString" value={template}/>
      <attribute id="_OriginalFileVersion_" type="int64" value={originalFileVersion}/>
      <children>
        {meshes.map(_.xml)}{extraChildren}
      </children>
    </node>
}

object Visual {
  val bankName: String = "VisualBank"
  val defaultExtra: NodeSeq = <node id="AnimationWaterfall">
    <attribute id="Object" type="FixedString" value=""/>
  </node>
      <node id="Base"/>
      <node id="ClothParams"/>
      <node id="ClothProxyMapping"/>
}
