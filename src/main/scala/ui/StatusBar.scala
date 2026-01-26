package ui

import app.State
import scalafx.scene.control.Separator
import scalafx.scene.layout.HBox
import scalafx.scene.text.Text

object StatusBar {
  lazy val statusBar: HBox = {
    val modLabel = new Text {
      text = "Mod:"
    }
    val modName = new Text {
      text <== app.State.ProjectState.name
    }
    val authorLabel = new Text {
      text = "Author:"
    }
    val author = new Text {
      text <== app.State.ProjectState.author
    }
    val sourcesLabel = new Text {
      text = "Sources:"
    }
    val sources = new Text {
      text <== State.ProjectState.sources
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
      visible <== State.ProjectState.meta.isNotNull
    }
    hbox
  }

}
