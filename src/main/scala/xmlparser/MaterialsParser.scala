package xmlparser

import domain.{BankElement, Material, TextureUsage}

import scala.xml.{Node, NodeSeq}

object MaterialsParser extends LSXParser {
  val bankName: String = "MaterialBank"
  val nodeId: String = "Resource"

  def parse(node: Node): BankElement = {
    val attributes: NodeSeq = node \ "attribute"
    val id = attr(attributes, "ID")
    val materialType = attr(attributes, "MaterialType")
    val name = attr(attributes, "Name")
    val sourceFile = attr(attributes, "SourceFile")
    val originalFileVersion = attr(attributes, "_OriginalFileVersion_")
    val (textures, extraChildren) = (node \ "children" \ "node").partition(_.\@("id") == "Texture2DParameters")
    val textureUsages = textures.map { textureNode =>
      val textureAttributes = textureNode \ "attribute"
      TextureUsage(
        enabled = attr(textureAttributes, "Enabled"),
        exportAsPreset = attr(textureAttributes, "ExportAsPreset"),
        groupName = attr(textureAttributes, "GroupName"),
        id = attr(textureAttributes, "ID"),
        parameterName = attr(textureAttributes, "ParameterName"),
      )
    }
    Material(
      name = name,
      id = id,
      materialType = materialType,
      sourceFile = sourceFile,
      originalFileVersion = originalFileVersion,
      textures = textureUsages,
      extraChildren = extraChildren,
    )
  }
}
