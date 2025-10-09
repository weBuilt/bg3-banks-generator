package lsx

import scala.xml.{Node, NodeSeq}

object MaterialsParser extends LSXParser {
  val bankName: String = Material.bankName
  val nodeId: String = "Resource"
  private val processableAttributes = List(
    "Enabled",
    "ExportAsPreset",
    "GroupName",
    "ID",
    "ParameterName",
  )

  def parse(node: Node, source: BankElement.BankElementSource): BankElement = {
    val attributes: NodeSeq = node \ "attribute"
    val id = attr(attributes, "ID")
    val materialType = attr(attributes, "MaterialType")
    val name = attr(attributes, "Name")
    val sourceFile = attr(attributes, "SourceFile")
    val originalFileVersion = attr(attributes, "_OriginalFileVersion_")
    val (textures, extraChildren) = (node \ "children" \ "node").partition(_.\@("id") == "Texture2DParameters")
    val textureUsages = textures.map { textureNode =>
      val textureAttributes = textureNode \ "attribute"
      val extraNodes = textureAttributes.filterNot(processableAttributes contains _.\@("id"))
      TextureUsage(
        enabled = attr(textureAttributes, "Enabled"),
        exportAsPreset = attr(textureAttributes, "ExportAsPreset"),
        groupName = attr(textureAttributes, "GroupName"),
        id = attr(textureAttributes, "ID"),
        parameterName = attr(textureAttributes, "ParameterName"),
        extraNodes = extraNodes,
      )
    }
    Material(
      name = name,
      source = source,
      id = id,
      materialType = materialType,
      sourceFile = sourceFile,
      originalFileVersion = originalFileVersion,
      textures = textureUsages,
      extraChildren = extraChildren,
    )
  }
}
