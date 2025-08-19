package client
package components

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object CollapsibleList {
  // render header item with a button to toggle the list view
  def apply(header: HtmlElement, items: List[HtmlElement]) = {
    val isExpanded = Var(false)
    
    div(
      cls := "collapsible-list shadow-md rounded-lg overflow-hidden",
      div(
        cls := "collapsible-header flex items-center gap-2 p-3 hover:bg-opacity-10 transition-colors cursor-pointer",
        onClick.mapTo(!isExpanded.now()) --> isExpanded.writer,
        div(
          cls := "toggle-button flex-shrink-0 w-6 h-6 flex items-center justify-center transition-colors",
          child.text <-- isExpanded.signal.map(if (_) "▼" else "▶")
        ),
        div(
          cls := "header-content flex-1",
          header
        )
      ),
      div(
        cls := "collapsible-content",
        display <-- isExpanded.signal.map(if (_) "block" else "none"),
        children <-- isExpanded.signal.map { expanded =>
          if (expanded) {
            items.zipWithIndex.map { case (item, index) =>
              div(
                cls := s"list-item pl-8 pr-3 py-2 border-b border-opacity-20 border-current last:border-b-0 hover:bg-gray-200 transition-colors",
                item
              )
            }
          } else List.empty
        }
      )
    )
  }
}



