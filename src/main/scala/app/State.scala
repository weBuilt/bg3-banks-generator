package app

import app.Config.{ProjectConfiguration, ProjectReference}
import fileparser.lsx.Meta
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.beans.property.{ObjectProperty, StringProperty}

import java.io.File
import java.nio.file.Paths

object State {

  val meta: ObjectProperty[Meta] = new ObjectProperty[Meta]
  val sources: StringProperty = new StringProperty()
  val sourcesParent: ObjectBinding[File] = Bindings.createObjectBinding(
    () => Paths.get(sources.value).toFile.getParentFile,
    sources
  )

  def currentReference(): Option[ProjectReference] =
    Option.when(State.meta.isNotNull.get()) {
      ProjectReference(
        State.meta.value.uuid,
        State.meta.value.name,
        State.sources.value,
      )
    }

  def currentProjectConfig(): Option[ProjectConfiguration] =
    currentReference().map { reference =>
      ProjectConfiguration(reference, None, Nil)
    }

}
