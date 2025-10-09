package lsx

import BankElement.{BankElementSource, Existing}

import java.nio.file.{Files, Path}
import scala.util.Try
import scala.xml._

trait LSXParser {
  def parse(node: Node, source: BankElementSource): BankElement

  def bankName: String

  def nodeId: String

  def attr(attributes: NodeSeq, name: String): String =
    attributes.filter(_.\@("id") == name).\@("value")
}

object LSXParser {
  val parsers: List[LSXParser] = MaterialsParser :: TexturesParser :: VisualsParser :: Nil
  private val prettyPrinter = new PrettyPrinter(300, 4)
  private val version: Elem = <version major="4" minor="0" revision="9" build="0" lslib_meta="v1,bswap_guids,lsf_adjacency"/>

  def parse(path: Path): ParseResult =
    if (Files.exists(path)) Try {
      val save: Elem = XML.loadFile(path.toFile)
      val regions = save \ "region"
      val elements = parsers.flatMap { parser =>
        regions
          .filter(_.\@("id") == parser.bankName)
          .\("node")
          .filter(_.\@("id") == parser.bankName)
          .\("children")
          .\("node")
          .filter(_.\@("id") == parser.nodeId)
          .map(parser.parse(_, Existing(path)))
      }
      val unsupported = (save \ "region").filterNot(parsers.map(_.bankName) contains _.\@("id"))
      ParseResult(elements, Option.when(unsupported.nonEmpty)(generate(unsupported)))
    }.getOrElse(ParseResult(Nil, None)) else ParseResult(Nil, None)

  def generate(elements: List[BankElement]): String = {
    val banks: List[Elem] = elements.groupBy(_.bankName).map {
      case (bankName, elements) =>
        <region id={bankName}>
          <node id={bankName}>
            <children>
              {elements.map(_.xml)}
            </children>
          </node>
        </region>
    }.toList
    generate(banks)
  }

  def generate(nodes: NodeSeq): String = {
    val xml = <save>
      {version}{nodes}
    </save>
    """<?xml version="1.0" encoding="utf-8"?>""" + "\n" + prettyPrinter.format(xml, scala.xml.TopScope)
  }

  case class ParseResult(
    elements: List[BankElement],
    unsupportedBanks: Option[String],
  ) {
    val hasUnsupported: Boolean = unsupportedBanks.nonEmpty
  }
}
