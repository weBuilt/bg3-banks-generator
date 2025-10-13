package app

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

}
