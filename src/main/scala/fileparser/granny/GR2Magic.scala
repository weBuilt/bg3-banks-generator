package fileparser.granny

case class GR2Magic(
  signatureBytes: Array[Byte],
  headerSize: Long,
  headerFormat: Long,
  reserved1: Long,
  reserved2: Long,
) extends GR2HeaderElement {
  val byteCount: Int = 32
  val format: Either[String, FormatValue] = FormatValue(signatureBytes)

  override def toString: String =
    s"""GR2Magic(
       | format: $format
       | headerSize: $headerSize
       | headerFormat: $headerFormat
       | reserved1: $reserved1
       | reserved2: $reserved2
       |)""".stripMargin
}
