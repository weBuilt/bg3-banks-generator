package fileparser.lsx

import domain.Exceptions
import domain.Exceptions.MyException
import util.FileUtils

import java.io.File
import java.nio.file.Path
import scala.util.{Failure, Success, Try}
import scala.xml.{Node => XMLNode, Attribute => XMLAttribute,_}

object LSX {
  val dotLSX: String = ".lsx"
  val notLSXException: MyException = Exceptions.SimpleException("Not a .lsx file")
  val malformedXMLException: MyException = Exceptions.SimpleException("Malformed LSX")

  case class Attribute(
    name: String,
    tpe: String,
    value: String = "",
  ) {
    def xml: Elem = <attribute id={name} type={tpe} value={value}/>
  }

  object Attribute {
    def from(node: XMLNode): Attribute = Attribute(
      node \@ "id",
      node \@ "type",
      node \@ "value",
    )
  }

  case class Node(
    name: String,
    attributes: Seq[Attribute] = Nil,
    children: Seq[Node] = Nil,
  ) {
    def attr(attrName: String): Option[Attribute] =
      attributes.find(_.name == attrName)

    def xml: Elem =
      <node id={name}>
        {attributes.map(_.xml)}<children>
        {children.map(_.xml)}
      </children>
      </node>
  }

  object Node {
    def from(node: XMLNode): Node = Node(
      node \@ "id",
      (node \ "attribute").map(Attribute.from),
      (node \ "children" \ "node").map(from),
    )
  }

  case class Save(
    version: Save.Version,
    regions: Seq[Region],
  ) {
    def xml: Elem =
      <save>
        {version.xml}{regions.map(_.xml)}
      </save>
  }

  object Save {
    case class Version(
      major: String,
      minor: String,
      revision: String,
      build: String,
      lslibMeta: String,
    ) {
      def xml: Elem =
          <version major={major} minor={minor} revision={revision} build={build} lslib_meta={lslibMeta}/>
    }

    def version(nodeSeq: NodeSeq): Version = Version(
      major = nodeSeq \@ "major",
      minor = nodeSeq \@ "minor",
      revision = nodeSeq \@ "revision",
      build = nodeSeq \@ "build",
      lslibMeta = nodeSeq \@ "lslibMeta",
    )

    def from(elem: Elem): Either[Exceptions.MyException, Save] =
      Either.cond(
        elem.label == "save",
        Save(
          version(elem \ "version"),
          (elem \ "region").flatMap(Region.from)
        ),
        malformedXMLException,
      )
  }

  case class Region(
    name: String,
    node: Node,
  ) {

    def xml: Elem =
      <region id={name}>
        {node.xml}
      </region>

  }

  object Region {
    /** errors in lesser tags can be ignored */
    def from(node: XMLNode): Option[Region] =
      (node \ "node")
        .headOption
        .map { child =>
          Region(node \@ "id", Node.from(child))
        }
  }

  def read(path: File): Either[Exceptions.MyException, Save] = {
    val exists = path.exists()
    val isLSX = FileUtils.extension(path).contains(dotLSX)
    if (!exists) Left(Exceptions.noFile)
    else if (!isLSX) Left(notLSXException)
    else {
      Try {
        val elem = scala.xml.XML.loadFile(path)
        Save.from(elem)
      } match {
        case Failure(exception) =>
          Left(Exceptions.SimpleException(exception.getMessage))
        case Success(value) => value
      }
    }
  }

  private val printer = new xml.PrettyPrinter(300, 4)

  def write(path: Path, save: Save): Boolean = Try {
    XML.save(path.toString, xml.XML.loadString(printer.format(save.xml, xml.TopScope)), xmlDecl = true)
  }.isSuccess
}
