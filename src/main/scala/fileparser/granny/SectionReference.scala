package fileparser.granny

case class SectionReference(
  section: Long,
  offset: Long,
) extends GR2HeaderElement {
  val byteCount: Int = 8

  override def toString: String =
    s"""SectionReference(
       | section: $section
       | offset: $offset
       |)""".stripMargin
}
