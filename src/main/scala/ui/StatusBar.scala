package ui

import scalafx.beans.binding.{Bindings, StringBinding}
import scalafx.scene.control.Separator
import scalafx.scene.layout.HBox
import scalafx.scene.text.Text
import app.State
import fileparser.lsx.Meta

object StatusBar {
  lazy val statusBar: HBox = {
    val modLabel = new Text {
      text = "Mod:"
    }
    val modName = new Text {
      text <== metaStringLens(_.name)
    }
    val authorLabel = new Text {
      text = "Author:"
    }
    val author = new Text{
      text <== metaStringLens(_.author)
    }
    val sourcesLabel = new Text {
      text = "Sources:"
    }
    val sources = new Text{
      text <== State.sources
    }
    val hbox = new HBox {
      children =
        modLabel ::
          modName ::
          new Separator() ::
          authorLabel ::
          author ::
          new Separator() ::
          sourcesLabel ::
          sources ::
          Nil
      spacing = 1.0
      visible <== State.meta.isNotNull
    }
    hbox
  }
  def metaStringLens(f: Meta => String): StringBinding = Bindings.createStringBinding(
    () => Option(app.State.meta.value).map(f).getOrElse(""),
    State.meta,
  )
}
