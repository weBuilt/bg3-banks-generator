package fileparser.lsx

import scala.xml.{Node, NodeSeq}

object VisualsParser extends LSXParser {
  val bankName: String = Visual.bankName
  val nodeId: String = "Resource"

  override def parse(
    node: Node,
    source: BankElement.BankElementSource,
  ): BankElement = {
    val attributes: NodeSeq = node \ "attribute"
    val (objects, extraChildren) = (node \ "children" \ "node").partition(_.\@("id") == "Objects")
    val meshes = objects.zipWithIndex.map {
      case (obj, idx) =>
      val attributes: NodeSeq = obj \ "attribute"
      Mesh(
        objectId = attr(attributes, "ObjectID"),
        materialId = attr(attributes, "MaterialID"),
        lod = attr(attributes, "LOD"),
        order = Some(idx),
      )
    }.toList
    Visual(
      name = attr(attributes, "Name"),
      source = source,
      meshes = meshes,
      id = attr(attributes, "ID"),
      sourceFile = attr(attributes, "SourceFile"),
      template = attr(attributes, "Template"),
      attachBone = attr(attributes, "AttachBone"),
      attachmentSkeletonResource = attr(attributes, "AttachmentSkeletonResource"),
      blueprintInstanceResourceID = attr(attributes, "BlueprintInstanceResourceID"),
      boundsMax = attr(attributes, "BoundsMax"),
      boundsMin = attr(attributes, "BoundsMin"),
      clothColliderResourceID = attr(attributes, "ClothColliderResourceID"),
      hairPresetResourceId = attr(attributes, "HairPresetResourceId"),
      hairType = attr(attributes, "HairType"),
      materialType = attr(attributes, "MaterialType"),
      needsSkeletonRemap = attr(attributes, "NeedsSkeletonRemap"),
      remapperSlotId = attr(attributes, "RemapperSlotId"),
      scalpMaterialId = attr(attributes, "ScalpMaterialId"),
      skeletonResource = attr(attributes, "SkeletonResource"),
      skeletonSlot = attr(attributes, "SkeletonSlot"),
      slot = attr(attributes, "Slot"),
      softbodyResourceID = attr(attributes, "SoftbodyResourceID"),
      supportsVertexColorMask = attr(attributes, "SupportsVertexColorMask"),
      originalFileVersion = attr(attributes, "_OriginalFileVersion_"),
      extraChildren = extraChildren,
    )
  }
}
