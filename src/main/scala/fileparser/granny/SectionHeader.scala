package fileparser.granny

case class SectionHeader(
  compression: Long,
  offsetInFile: Long,
  compressedSize: Long,
  uncompressedSize: Long,
  alignment: Long,
  first16bit: Long,
  first8bit: Long,
  relocationsOffset: Long,
  numRelocations: Long,
  mixedMarshallingDataOffset: Long,
  numMixedMarshallingData: Long,
) extends GR2HeaderElement {
  val byteCount: Int = 44
  val hasCompression: Boolean = (compression != 0) && (compressedSize != uncompressedSize)

  override def toString: String =
    s"""SectionHeader(
       | compression: $compression
       | offsetInFile: $offsetInFile
       | compressedSize: $compressedSize
       | uncompressedSize: $uncompressedSize
       | alignment: $alignment
       | first16bit: $first16bit
       | first8bit: $first8bit
       | relocationsOffset: $relocationsOffset
       | numRelocations: $numRelocations
       | mixedMarshallingDataOffset: $mixedMarshallingDataOffset
       | numMixedMarshallingData: $numMixedMarshallingData
       |)""".stripMargin

}
