package client

import com.raquo.laminar.api.L.{*, given}

import org.scalajs.dom

@main
def main(): Unit = {
  renderOnDomContentLoaded(
    dom.document.getElementById("app"), 
    app //Main.appElement()
  )
}

val app: Div = div(
  // p(childrenOf[Page].mkString(" ")),
  child <-- Routes.splitter.signal
)

 