package util

object UUID {
  def generate: String = java.util.UUID.randomUUID().toString
  val empty: String = "00000000-0000-0000-0000-000000000000"
}
