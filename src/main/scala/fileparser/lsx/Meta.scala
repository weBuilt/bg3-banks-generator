package fileparser.lsx

import cats.data.Validated
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
  val metaFilename = "meta.lsx"
  val metaNotFound: MyException = Exceptions.SimpleException(s"$metaFilename not found")
  val modsNotFound: MyException = Exceptions.SimpleException(s"Mods folder not found")
  val tooManyMetaFiles: MyException = Exceptions.SimpleException(s"Too many $metaFilename files found")
  def folderMismatch(folderInMeta: String, actualParent: String): MyException = Exceptions.SimpleException(s"Folder mismatch. Folder specified in file is $folderInMeta while file located in $actualParent")

  /** look for meta.lsx in sources/Mods/_/ */
  def find(sources: File, folder: Option[String]): Validated[MyException, Meta] =
    Validated.cond(sources.isDirectory, sources, Exceptions.noDir)
      .andThen(findModsDir)
      .andThen(findMetaFiles(_, folder))
      .andThen(validateAndParseMeta)

  def findModsDir(sources: File): Validated[MyException, File] =
    sources
      .listFiles()
      .find(f => f.isDirectory && f.getName == "Mods")
      .toValid(modsNotFound)

  def findMetaFiles(modsDir: File, folder: Option[String]): Validated[MyException, List[File]] =
    modsDir
      .listFiles()
      .filter(f => f.isDirectory && folder.forall(f.getName.==))
      .flatMap { subdir =>
        subdir
          .listFiles()
          .find(f => f.isFile && f.getName == metaFilename)
      }
      .toList
      .valid[MyException]

  def validateAndParseMeta(metaFiles: List[File]): Validated[MyException, Meta] =
    metaFiles match {
      case file :: Nil =>
        fromFile(file).andThen { meta =>
          val subdir = file.getParentFile.getName
          Validated.cond(meta.folder == subdir, meta, folderMismatch(meta.folder, subdir))
        }
      case Nil => metaNotFound.invalid[Meta]
      case _ => tooManyMetaFiles.invalid[Meta]
    }

  def fromFile(file: File): Validated[MyException, Meta] =
    LSX.read(file)
      .andThen {
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
          meta.toValid[MyException](LSX.malformedXMLException)
        case _ =>
          LSX.malformedXMLException.invalid[Meta]
      }

  def path(name: String): Path =
    Paths.get("Mods", name, metaFilename)

  def default(
    name: String,
    author: String,
    uuid: Option[String] = None,
  ): String =
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
       |                    <attribute id="UUID" type="FixedString" value="${uuid.getOrElse(UUID.generate)}" />
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
