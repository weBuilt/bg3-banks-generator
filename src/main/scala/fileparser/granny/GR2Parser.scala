package fileparser.granny

import domain.BankElement.Generated
import domain.{Mesh, Visual}
import fileparser.granny.FormatValue.{BigEndian, LittleEndian}

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.file.{Files, Path}

object GR2Parser {
  /*  def writeUInt32(uInt32: Long, array: Array[Byte], pos: Int): Unit = {
      (0 until 4).foreach { p =>
        array.update(pos + p, ((uInt32 >> (p * 8)) & 0xFF).toByte)
      }
    }*/

  def parse(path: Path, modSources: Path): Either[String, Visual] = {
    val content: Array[Byte] = Files.readAllBytes(path)
    //sequential access needed to read signature and headers
    val inputStream: ByteArrayInputStream = new ByteArrayInputStream(content)


    val gr2Magic: GR2Magic = GR2Magic(
      signatureBytes = inputStream.readNBytes(16),
      headerSize = readUInt32(inputStream),
      headerFormat = readUInt32(inputStream),
      reserved1 = readUInt32(inputStream),
      reserved2 = readUInt32(inputStream),
    )

    val header: Either[String, GR2Header] = gr2Magic.format.flatMap {
      case _: LittleEndian =>
        val version = readUInt32(inputStream)
        val fileSize = readUInt32(inputStream)
        val crc = readUInt32(inputStream)
        val sectionsOffset = readUInt32(inputStream)
        val numSections = readUInt32(inputStream)
        val rootType = SectionReference(readUInt32(inputStream), readUInt32(inputStream))
        val rootNode = SectionReference(readUInt32(inputStream), readUInt32(inputStream))
        val tag = readUInt32(inputStream)
        val extraTags = List.fill(4)(readUInt32(inputStream))
        Right(
          GR2Header(
            version = version,
            fileSize = fileSize,
            crc = crc,
            sectionsOffset = sectionsOffset,
            numSections = numSections,
            rootType = rootType,
            rootNode = rootNode,
            tag = tag,
            extraTags = extraTags,
            stringTableCrc = Option.when(version >= 7)(readUInt32(inputStream)),
            reserved1 = Option.when(version >= 7)(readUInt32(inputStream)),
            reserved2 = Option.when(version >= 7)(readUInt32(inputStream)),
            reserved3 = Option.when(version >= 7)(readUInt32(inputStream)),
          )
        )
      case _: BigEndian => Left("Only little-endian GR2 files are supported")
    }
    val sectionHeaders: Either[String, List[SectionHeader]] = header.map { header =>
      List.fill(header.numSections.toInt) {
        SectionHeader(
          compression = readUInt32(inputStream),
          offsetInFile = readUInt32(inputStream),
          compressedSize = readUInt32(inputStream),
          uncompressedSize = readUInt32(inputStream),
          alignment = readUInt32(inputStream),
          first16bit = readUInt32(inputStream),
          first8bit = readUInt32(inputStream),
          relocationsOffset = readUInt32(inputStream),
          numRelocations = readUInt32(inputStream),
          mixedMarshallingDataOffset = readUInt32(inputStream),
          numMixedMarshallingData = readUInt32(inputStream),
        )
      }
    }
    val checkedSizeAndCompression: Either[String, GR2FileFullHeader] = for {
      header <- header
      sectionHeaders <- sectionHeaders
      fullHeader = GR2FileFullHeader(gr2Magic, header, sectionHeaders)
      headerSizeCorrect = fullHeader.byteCount == gr2Magic.headerSize
      //      hasCompression = sectionHeaders.exists(_.hasCompression)
      errorString = List(
        Option.when(headerSizeCorrect)("Incorrect header size. Maybe file is corrupted"),
        //        Option.when(hasCompression)("Only uncompressed GR2 files are supported. LSLib exports uncompressed GR2"),
      ).flatten.mkString(" ")
      result <- Either.cond(/*!hasCompression && */ headerSizeCorrect, fullHeader, errorString)
    } yield result
    //random access needed from this point
    inputStream.close()
    /*    val sections = checkedSizeAndCompression.map { fullHeader =>
          fullHeader.sectionHeaders.map { sectionHeader =>
            val sectionContent = content.slice(
              from = sectionHeader.offsetInFile.toInt,
              until = sectionHeader.offsetInFile.toInt + sectionHeader.compressedSize.toInt,
            )
            SectionContent(
              sectionHeader,
              sectionContent,
            )
          }
        }
        sections.foreach { sections =>
          sections.foreach { section =>
            val sectionStream = new ByteArrayInputStream(section.content)
            (0 until section.header.numRelocations.toInt).foreach{_ =>
              val offsetInSection = readUInt32(sectionStream)
              val reference = SectionReference(readUInt32(sectionStream), readUInt32(sectionStream))
              val fixupAddress = sections(reference.section.toInt).header.offsetInFile + reference.offset
              writeUInt32(fixupAddress, section.content, offsetInSection.toInt)
            }
            sectionStream.close()
          }
        }
        header.map{header =>
          sections.map{sections =>
            val rootSection = sections(header.rootNode.section.toInt)
            sections.foreach{section =>
              Files.write(Paths.get(section.header.offsetInFile.toString), section.content)
            }
          }
        }*/
    checkedSizeAndCompression.map { header =>
      //1 section is always for armature
      val meshesNum: Int = header.sectionHeaders.count(_.compressedSize > 0) - 1
      val name = path.getFileName.toString.split("\\.").dropRight(1).mkString(".")
      val sourceFile = modSources.relativize(path).toString
      val meshes = (0 until meshesNum).map { idx =>
        Mesh(
          objectId = s"$name.$idx.$idx",
          materialId = "00000000-0000-0000-0000-000000000000",
          lod = "0",
        )
      }.toList
      Visual(
        name = name,
        source = Generated,
        meshes = meshes,
        id = "",
        sourceFile = sourceFile,
        template = s"""${sourceFile.split("\\.").dropRight(1).mkString(".")}.Dummy_Root.0""",
      )
    }
  }

  def readUInt32(inputStream: InputStream): Long =
    inputStream.readNBytes(4).foldRight(0L) {
      case (byte, acc) =>
        (acc << 8) | (byte & 0xFF)
    }

  case class GR2FileFullHeader(
    magic: GR2Magic,
    header: GR2Header,
    sectionHeaders: List[SectionHeader],
  ) {
    val byteCount: Int = magic.byteCount + header.byteCount + sectionHeaders.map(_.byteCount).sum
  }

  //  case class Reference(offset: Long)
}
