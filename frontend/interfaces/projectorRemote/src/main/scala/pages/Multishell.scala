package client
package pages

import components._

import com.raquo.laminar.api.L.{*, given}

object Multishell {

  def apply() = {
    // Define shell instances - 13 instances for 2-column grid
    val shellInstances = List(
      "host1",
      "host2",
      "host3",
      "host4",
      "host5",
      "host6",
      "host7",
      "host8"
      // "host9",
      // "host10",
      // "host11",
      // "host12",
      // "host13"
    )

    div(
      cls := "h-screen w-screen flex flex-col bg-gray-900 m-0 p-0 overflow-hidden",
      styleAttr := "margin: 0; padding: 0;",
      // Grid container - 2 columns, auto rows, no gaps, no padding
      // Use auto-rows with minmax to ensure equal height rows
      div(
        cls := "flex-1 grid grid-cols-2 auto-rows-fr overflow-hidden",
        styleAttr := "gap: 0; margin: 0; padding: 0; grid-auto-rows: 1fr;",
        // Generate XTerm components for each instance
        shellInstances.map { instanceId =>
          div(
            cls := "flex flex-col bg-gray-900 overflow-hidden",
            styleAttr := "margin: 0; padding: 0; border: none;",
            // Terminal header - minimal, no padding
            div(
              cls := "bg-gray-800 flex items-center justify-between",
              styleAttr := "padding: 0; margin: 0; border: none; height: 16px;",
              span(
                cls := "text-xs font-mono text-green-400",
                styleAttr := "padding-left: 4px;",
                instanceId
              ),
              span(
                cls := "text-xs text-gray-500",
                styleAttr := "padding-right: 4px;",
                "●"
              )
            ),
            // Terminal container - no padding
            div(
              cls := "flex-1 min-h-0",
              styleAttr := "margin: 0; padding: 0;",
              XTerm(instanceId)
            )
          )
        }
      )
    )
  }
}
