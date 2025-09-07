package fileparser.granny

case class GR2Header(
  version: Long,
  fileSize: Long,
  crc: Long,
  sectionsOffset: Long,
  numSections: Long,
  rootType: SectionReference,
  rootNode: SectionReference,
  tag: Long,
  extraTags: List[Long],
  //v7+
  stringTableCrc: Option[Long],
  reserved1: Option[Long],
  reserved2: Option[Long],
  reserved3: Option[Long],
) {
  val byteCount: Int = (if (version >= 7) 56 else 40) + rootType.byteCount + rootNode.byteCount

  override def toString: String =
    s"""
       |GR2Header(
       | version: $version
       | fileSize: $fileSize
       | crc: $crc
       | sectionsOffset: $sectionsOffset
       | numSections: $numSections
       | rootType: $rootType
       | rootNode: $rootNode
       | tag: ${tag.toHexString}
       | extraTags: ${extraTags.map(_.toHexString).mkString("[", ", ", "]")}
       | stringTableCrc: $stringTableCrc
       | reserved1: $reserved1
       | reserved2: $reserved2
       | reserved3: $reserved3
       |)""".stripMargin
}
