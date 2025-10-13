package app

import java.nio.file.{Files, Path, Paths}

object Config {
  val configDirectory: Path = Paths.get(System.getProperty("user.home")).resolve(".wblt_bg3bg")
  val windowConfig: Path = configDirectory.resolve("window.json")
  val appConfig: Path = configDirectory.resolve("app.json")

  case class WindowConfiguration(
    x: Double = 0d,
    y: Double = 0d,
    width: Double = 800d,
    height: Double = 600d,
    screen: Int = 0,
    maximized: Boolean = false,
  )

  case class AppConfiguration(
    lastProject: Option[ProjectReference],
    recent: List[ProjectReference],
  )

  case class ProjectReference(
    id: String,
    name: String,
    sources: String,
  )

  def currentReference(): Option[ProjectReference] =
    Option.when(State.meta.isNotNull.get()) {
      ProjectReference(
        State.meta.value.uuid,
        State.meta.value.name,
        State.sources.value,
      )
    }

}