package app

import fileparser.lsx.Meta
import scalafx.beans.property.{ObjectProperty, StringProperty}

object State {

  val meta: ObjectProperty[Meta] = new ObjectProperty[Meta]
  val sources: StringProperty = new StringProperty()


}
