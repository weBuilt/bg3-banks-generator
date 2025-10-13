package app.controls

import app.Config.ProjectReference
import app.State
import fileparser.lsx.Meta

import java.io.File
import java.nio.file.Paths

object ProjectControls {
  def openProject(reference: ProjectReference): Unit =
    openProject(Paths.get(reference.sources).toFile)

  def openProject(sources: File): Unit = {
    val meta = Meta.find(sources)
    meta match {
      case Left(value) => println(value.message)
      case Right(value) =>
        State.meta.update(value)
        State.sources.update(sources.toString)
    }
  }
}
