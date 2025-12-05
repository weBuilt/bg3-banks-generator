package fileparser.lsx

import cats.implicits._
import domain.Exceptions
import domain.Exceptions.MyException
import util.{PackedVersion, UUID}

import java.io.File
import java.nio.file.{Files, Path, Paths}

case class Meta(
  author: String,
  name: String,
  uuid: String,
  folder: String,
  version: PackedVersion,
  lsx: LSX.Save,
)

object Meta {
  val filename = "meta.lsx"
  val metaNotFound: MyException = Exceptions.SimpleException(s"$filename not found")
  val modsNotFound: MyException = Exceptions.SimpleException(s"Mods folder not found")
  val tooManyMetaFiles: MyException = Exceptions.SimpleException(s"Too many $filename files found")
  val folderMismatch: MyException = Exceptions.SimpleException(s"Folder mismatch")

  /** look for meta.lsx in sources/Mods/_/ */
  def find(sources: File): Either[MyException, Meta] =
    for {
      sources <- Either.cond(sources.exists() && sources.isDirectory, sources, Exceptions.noDir)
      modsDir <- findModsDir(sources)
      metaFiles <- findMetaFiles(modsDir)
      meta <- validateAndParseMeta(metaFiles)
    } yield meta

  def findModsDir(sources: File): Either[MyException, File] =
    sources
      .listFiles()
      .find(f => f.isDirectory && f.getName == "Mods")
      .toRight(modsNotFound)

  def findMetaFiles(modsDir: File): Either[MyException, List[File]] =
    modsDir
      .listFiles()
      .filter(_.isDirectory)
      .flatMap { subdir =>
        subdir
          .listFiles()
          .find(f => f.isFile && f.getName == filename)
      }
      .toList
      .asRight[MyException]

  def validateAndParseMeta(metaFiles: List[File]): Either[MyException, Meta] =
    metaFiles match {
      case file :: Nil =>
        fromFile(file).flatMap { meta =>
          val subdir = file.getParentFile
          Either.cond(meta.folder == subdir.getName, meta, folderMismatch)
        }
      case Nil => metaNotFound.asLeft[Meta]
      case _ => tooManyMetaFiles.asLeft[Meta]
    }

  def fromFile(file: File): Either[MyException, Meta] =
    LSX.read(file)
      .flatMap {
        case lsx@LSX.Save(_, Seq(LSX.Region("Config", node))) if node.name == "root" =>
          val meta = for {
            moduleInfo <- node.children.find(_.name == "ModuleInfo")
            author <- moduleInfo.attr("Author")
            name <- moduleInfo.attr("Name")
            uuid <- moduleInfo.attr("UUID")
            folder <- moduleInfo.attr("Folder")
            version <- moduleInfo.attr("Version64")
            versionLong <- version.value.toLongOption
            packedVersion = PackedVersion.fromInt64(versionLong)
          } yield Meta(author.value, name.value, uuid.value, folder.value, packedVersion, lsx)
          meta.toRight[MyException](LSX.malformedXMLException)
        case _ =>
          LSX.malformedXMLException.asLeft[Meta]
      }

  def path(name: String): Path =
    Paths.get("Mods", name, filename)

  def default(name: String, author: String): String =
    s"""<?xml version="1.0" encoding="utf-8"?>
       |<save>
       |    <version major="4" minor="0" revision="9" build="333" />
       |    <region id="Config">
       |        <node id="root">
       |            <children>
       |                <node id="Dependencies" />
       |                <node id="ModuleInfo">
       |                    <attribute id="Author" type="LSWString" value="$author" />
       |                    <attribute id="CharacterCreationLevelName" type="FixedString" value="" />
       |                    <attribute id="Description" type="LSWString" value="$name" />
       |                    <attribute id="Folder" type="LSWString" value="$name" />
       |                    <attribute id="GMTemplate" type="FixedString" value="" />
       |                    <attribute id="LobbyLevelName" type="FixedString" value="" />
       |                    <attribute id="MD5" type="LSString" value="" />
       |                    <attribute id="MainMenuBackgroundVideo" type="FixedString" value="" />
       |                    <attribute id="MenuLevelName" type="FixedString" value="" />
       |                    <attribute id="Name" type="FixedString" value="$name" />
       |                    <attribute id="NumPlayers" type="uint8" value="4" />
       |                    <attribute id="PhotoBooth" type="FixedString" value="" />
       |                    <attribute id="StartupLevelName" type="FixedString" value="" />
       |                    <attribute id="Tags" type="LSWString" value="" />
       |                    <attribute id="Type" type="FixedString" value="Add-on" />
       |                    <attribute id="UUID" type="FixedString" value="${UUID.generate}" />
       |                    <attribute id="Version64" type="int64" value="36028797018963968" />
       |                    <children>
       |                        <node id="PublishVersion">
       |                            <attribute id="Version64" type="int64" value="36028797018963968" />
       |                        </node>
       |                        <node id="Scripts" />
       |                        <node id="TargetModes">
       |                            <children>
       |                                <node id="Target">
       |                                    <attribute id="Object" type="FixedString" value="Story" />
       |                                </node>
       |                            </children>
       |                        </node>
       |                    </children>
       |                </node>
       |            </children>
       |        </node>
       |    </region>
       |</save>""".stripMargin
}
