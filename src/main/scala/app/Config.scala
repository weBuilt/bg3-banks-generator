package app

import java.nio.file.{Path, Paths}

object Config {
  val configDirectory: Path = Paths.get(System.getProperty("user.home")).resolve(".wblt_bg3bg")
  val windowConfig: Path = configDirectory.resolve("window.json")
}