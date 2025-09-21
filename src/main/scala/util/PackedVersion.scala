package util

case class PackedVersion(
  major: Int,
  minor: Int,
  revision: Int,
  build: Int
) {
  def toVersion64: Long = {
    ((major & 0x7f).toLong << 55) |
      ((minor & 0xff).toLong << 47) |
      ((revision & 0xffff).toLong << 31) |
      ((build & 0x7fffffff).toLong)
  }
  def string64: String = toVersion64.toString

  def toVersion32: Int = {
    ((major & 0x0f) << 28) |
      ((minor & 0x0f) << 24) |
      ((revision & 0xff) << 16) |
      (build & 0xffff)
  }
}

object PackedVersion {
  //idk if it actually matters. v7 mods are working at v6
  val defaultVersion: PackedVersion = PackedVersion(4, 0, 9, 333)

  def fromInt64(packed: Long): PackedVersion = {
    PackedVersion(
      major = (packed >> 55 & 0x7f).toInt,
      minor = (packed >> 47 & 0xff).toInt,
      revision = (packed >> 31 & 0xffff).toInt,
      build = (packed & 0x7fffffff).toInt
    )
  }

  def fromInt32(packed: Int): PackedVersion = {
    PackedVersion(
      major = (packed >> 28 & 0x0f),
      minor = (packed >> 24 & 0x0f),
      revision = (packed >> 16 & 0xff),
      build = (packed & 0xffff)
    )
  }
}

