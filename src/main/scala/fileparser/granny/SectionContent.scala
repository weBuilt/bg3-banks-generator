package fileparser.granny

case class SectionContent(
  header: SectionHeader,
  content: Array[Byte],
)
