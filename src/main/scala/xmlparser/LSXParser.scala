package xmlparser

import domain.BankElement

import java.nio.file.{Files, Path}
import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq, XML}

trait LSXParser {
  def parse(node: Node): BankElement

  def bankName: String

  def nodeId: String

  def attr(attributes: NodeSeq, name: String): String =
    attributes.filter(_.\@("id") == name).\@("value")
}

object LSXParser {
  val parsers: List[LSXParser] = MaterialsParser :: TexturesParser :: Nil

  def parse(path: Path): List[BankElement] =
    if (Files.exists(path)) Try {
      val save: Elem = XML.loadFile(path.toFile)
      val elements = parsers.flatMap { parser =>
        save
          .\("region")
          .filter(_.\@("id") == parser.bankName)
          .\("node")
          .filter(_.\@("id") == parser.bankName)
          .\("children")
          .\("node")
          .filter(_.\@("id") == parser.nodeId)
          .map(parser.parse)
      }
      elements
    }.getOrElse(Nil) else Nil

  def generate(elements: List[BankElement]): String = ""
}
