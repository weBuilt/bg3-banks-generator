package app.controls

import app.controls.ProjectControls.ValidatedNewProject
import fileparser.lsx.Meta

import java.nio.file.{Files, Path, Paths}

object DefaultFiles {
  def init(project: ValidatedNewProject): Unit = {
    val sourcesPath = project.sources.toPath
    Files.writeString(sourcesPath.resolve(Meta.path(project.name)), Meta.default(project.name, project.author))
  }

  def defaultStructure(name: String): DefaultStructure = {
    val assets = Paths.get("Generated", "Public", name, "Assets")
    val localization = Paths.get("Localization", "English")
    val mods = Paths.get("Mods", name)
    val (publicDirs, publicFiles) = {
      val pub = Paths.get("Public")
      val game = pub.resolve(Paths.get("Game", "GUI", "Assets"))
      val controllerIcons = game.resolve(Paths.get("ControllerUIicons", "items_png"))
      val tooltips = game.resolve(Paths.get("Tooltips", "ItemIcons"))
      val self = pub.resolve(name)
      val selfIcons = self.resolve(Paths.get("Assets", "Textures", "Icons"))
      val selfContent = self.resolve("Content")
      val selfUI = selfContent.resolve(Paths.get("UI", "[PAK]_UI"))
      val selfBanks = selfContent.resolve(Paths.get("Assets", "Characters", "[PAK]_Armor"))
      val gui = self.resolve("GUI")
      val rootTemplates = self.resolve("RootTemplates")
      val stats = self.resolve(Paths.get("Stats", "Generated", "Data"))
      val treasureTables = stats.getParent.resolve("TreasureTable.txt")
      val objects = stats.resolve("Objects.txt")
      (
        List(
          controllerIcons,
          tooltips,
          selfIcons,
          selfUI,
          selfBanks,
          gui,
          rootTemplates,
          stats,
        ),
        List(treasureTables, objects)
      )
    }
    DefaultStructure(
      directories = assets :: localization :: mods :: publicDirs,
      files = publicFiles,
    )
  }

  case class DefaultStructure(directories: List[Path], files: List[Path])
}
