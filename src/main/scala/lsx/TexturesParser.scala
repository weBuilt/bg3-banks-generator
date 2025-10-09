package lsx

import fileparser.DDSParser

import scala.xml.{Node, NodeSeq}

object TexturesParser extends LSXParser {
  val bankName: String = Texture.bankName
  val nodeId: String = "Resource"

  override def parse(node: Node, source: BankElement.BankElementSource): BankElement = {
    val attributes: NodeSeq = node \ "attribute"
    val processedName = DDSParser.processName(attr(attributes, "Name"))
    Texture(
      name = attr(attributes, "Name"),
      source = source,
      id = attr(attributes, "ID"),
      sourceFile = attr(attributes, "SourceFile"),
      format = attr(attributes, "Format"),
      width = attr(attributes, "Width"),
      height = attr(attributes, "Height"),
      depth = attr(attributes, "Depth"),
      localized = attr(attributes, "Localized"),
      srgb = attr(attributes, "SRGB"),
      streaming = attr(attributes, "Streaming"),
      template = attr(attributes, "Template"),
      tpe = processedName.map(_.tpe).getOrElse(""),
      materialName = processedName.map(_.name),
    )
  }
}
