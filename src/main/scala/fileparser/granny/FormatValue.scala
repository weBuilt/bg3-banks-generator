package fileparser.granny

import java.nio.ByteOrder

sealed trait FormatValue {
  def byteOrder: ByteOrder
}

object FormatValue {
  def apply(bytes: Array[Byte]): Either[String, FormatValue] = {
    val unsignedBytes: Array[Int] = bytes.map(_ & 0xFF)
    SignatureValue.signatureValues.collectFirst {
      case signatureValue if signatureValue.byteValue.sameElements(unsignedBytes) =>
        signatureValue.formatValue
    }.toRight("Incorrect Signature")
  }

  trait LittleEndian extends FormatValue {
    def byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
  }

  trait BigEndian extends FormatValue {
    def byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
  }

  case object LittleEndian32 extends FormatValue with LittleEndian

  case object BigEndian32 extends FormatValue with BigEndian

  case object LittleEndian64 extends FormatValue with LittleEndian

  case object BigEndian64 extends FormatValue with BigEndian
}