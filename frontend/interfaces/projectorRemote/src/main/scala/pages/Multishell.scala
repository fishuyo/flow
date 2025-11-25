package client
package pages

import components._

import com.raquo.laminar.api.L.{*, given}

object Multishell {

  def apply() = {
    div(
      h1("multishell"),
      XTerm()
    )
  }
}
