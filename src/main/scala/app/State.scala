package app

import app.Config.{AppConfiguration, ProjectConfiguration, ProjectReference}
import fileparser.lsx.Meta
import scalafx.beans.binding.{Bindings, ObjectBinding, StringBinding}
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import java.io.File
import java.nio.file.Paths

object State {
  /** always initialized with default values, override on startup */
  object AppState {
    /** only for menu. real state preserved in file */
    val recentProjects: ObservableBuffer[ProjectReference] = ObservableBuffer.empty[ProjectReference]

    def config(): AppConfiguration = AppConfiguration(
      recentProjects = recentProjects.toList,
    )
  }

  object ProjectState {

    val meta: ObjectProperty[Meta] = new ObjectProperty[Meta]
    val name: StringBinding = metaStringLens(_.name)
    val author: StringBinding = metaStringLens(_.author)
    val sources: StringProperty = new StringProperty()
    val sourcesParent: ObjectBinding[File] = Bindings.createObjectBinding(
      () => Paths.get(sources.value).getParent.toFile,
      sources
    )

    def reference(): Option[ProjectReference] =
      Option.when(meta.isNotNull.get() && sources.isNotNull.get()) {
        ProjectReference(
          meta.value.uuid,
          meta.value.name,
          meta.value.folder,
          sources.value,
        )
      }

    def config(): Option[ProjectConfiguration] =
      ProjectState.reference().map { reference =>
        ProjectConfiguration(reference, None, Nil)
      }

    def metaStringLens(f: Meta => String): StringBinding = Bindings.createStringBinding(
      () => Option(State.ProjectState.meta.value).map(f).getOrElse(""),
      State.ProjectState.meta,
    )

    def isCurrent(reference: ProjectReference): Boolean = reference.sources == sources()
  }

}
