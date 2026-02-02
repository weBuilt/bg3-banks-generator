package app

import cats.Semigroup
import cats.implicits._

import java.io.File
import java.nio.file.Paths
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ParSeq
import scala.util.Try
import scala.xml._

object GameFilesAnalyzer extends App {
  val unpackedDataPath = Paths.get(args(0))

  def recursiveFiles(filter: File => Boolean, file: File): ParSeq[File] =
    if (file.isDirectory) file.listFiles().toList.par.flatMap(recursiveFiles(filter, _))
    else if (filter(file)) ParSeq(file)
    else ParSeq.empty[File]

  def stats(file: File): (Int, Int) =
    if (file.isDirectory) {
      val files = file.listFiles().toList
      val sub = files.filter(_.isDirectory)
      val substats = sub.map(stats)
      val (subdircounts, subfilecounts) = substats.unzip
      (subdircounts.sum + sub.length, subfilecounts.sum + files.length - sub.length)
    } else (0, 0)

  //  val (totaldirs, totalfiles) = stats(unpackedDataPath.toFile)
  //  println(f"there are $totalfiles%,d files in $totaldirs%,d directories")

  val startTime = System.currentTimeMillis()


  //region -> (node -> objects)s
  case class Region(
    name: String,
    banks: List[Bank],
  )

  case class Bank(
    name: String,
    objects: List[LSXNode],
  )

  case class LSXNode(
    name: String,
    attributes: List[LSXAttribute],
    children: List[LSXNode],
  )

  case class LSXAttribute(
    name: String,
    tpe: String,
  )

  implicit val lsxNodeSemigroup: Semigroup[LSXNode] = new Semigroup[LSXNode] {
    def combine(x: LSXNode, y: LSXNode): LSXNode =
      LSXNode(
        x.name,
        (x.attributes ::: y.attributes).distinct,
        combineById(x.children ::: y.children)(_.name)(this),
      )
  }
  implicit val bankSemigroup: Semigroup[Bank] = (x: Bank, y: Bank) => Bank(x.name, combineById(x.objects ::: y.objects)(_.name))
  implicit val regionSemigroup: Semigroup[Region] = (x: Region, y: Region) => Region(x.name, combineById(x.banks ::: y.banks)(_.name))

  def combineById[T: Semigroup, K](list: Seq[T])(k: T => K): List[T] =
    list.groupBy(k).flatMap(_._2.combineAllOption).toList

  //для начала нас интересуют общие структуры TextureBank, MaterialBank и GameObject с типом item
  //также нужен парсер на параметры игровых и кастомных шейдеров
  val regionsToParse = List(
    //"TextureBank",
    //"MaterialBank",
    //"VisualBank",
    "Templates"
  )

  def lsxAttribute(node: Node): Option[LSXAttribute] = Try {
    LSXAttribute(
      node \@ "id",
      node \@ "type",
    )
  }.toOption

  def lsxNode(node: Node): Option[LSXNode] = Try {
    val name = node \@ "id"
    val xmlAttributes = node \ "attribute"
    val attributes = xmlAttributes.flatMap(lsxAttribute)
    val isItemTemplateOrNotTemplate = true/*(name != "GameObject") || xmlAttributes.exists { n =>
      n \@ "id" == "Type" &&
        n \@ "value" == "item"
    }*/
    val children1 = (node \ "children" \ "node").flatMap(lsxNode)
    val children2 = (node \ "node").flatMap(lsxNode)
    Option.when(isItemTemplateOrNotTemplate)(
      LSXNode(
        name,
        attributes.toList,
        (children1 ++ children2).toList,
      )
    )
  }.toOption.flatten

  def lsxBank(node: Node): Option[Bank] = Try {
    val id = node \@ "id"
    val objects = node \ "children" \ "node"
    val filteredObjects = objects.filter{obj =>
      obj \@ "id" == "GameObjects" && {
        (obj \ "attribute").exists{ attr=>
          attr \@ "id" == "Type" &&
            attr \@ "value" == "item"

        }
      }
    }
    Bank(id, filteredObjects.flatMap(lsxNode).toList)
  }.toOption

  def lsxRegion(node: Node): Option[Region] = Try {
    val regionId = node \@ "id"
    Option.when(regionsToParse contains regionId) {
      val nodes = node \ "node"
      val nodesWithObjects = nodes.flatMap(lsxBank)
      Region(regionId, combineById(nodesWithObjects)(_.name))
    }
  }.toOption.flatten

  def getRegions(file: File): List[Region] = Try {
    val xml = XML.loadFile(file)
    val regions = (xml \ "region").flatMap(lsxRegion)
    combineById(regions)(_.name)
  }.getOrElse(List.empty)

  val files = recursiveFiles(_.getName.endsWith(".lsx"), unpackedDataPath.toFile)
  val listFilesTime = System.currentTimeMillis()
  println(f"""found ${files.size}%,d lsx files in ${(listFilesTime - startTime) / 1000d}%,.2f seconds""")
  val allRegions = files.flatMap(getRegions)

  val allRegionsTime = System.currentTimeMillis()

  println(f"""found ${allRegions.size}%,d regions in ${(allRegionsTime - listFilesTime) / 1000d}%,.2f seconds""")
  val regions = allRegions.groupBy(_.name).flatMap(_._2.toList.combineAllOption)
  val regionsParseEndTime = System.currentTimeMillis()
  regions.foreach(println)
  println(f"""parsed ${regions.size}%,d region types in ${(regionsParseEndTime - allRegionsTime) / 1000d}%,.2f seconds""")

}
