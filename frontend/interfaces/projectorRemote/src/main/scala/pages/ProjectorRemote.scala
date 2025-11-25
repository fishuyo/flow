package client
package pages

import components._

import com.raquo.laminar.api.L.{*, given}

case class Projector(name: String, actions: List[String])

object ProjectorRemote {

  val pds = ('a' to 'z').map(c => Projector(s"PD $c", List("On", "Off"))).toList
  val christies =
    ('a' to 'z').map(c => Projector(s"Christies $c", List("On", "Off"))).toList
  val projectors = Map(
    "pds" -> pds,
    "christies" -> christies
  )

  def apply() = {
    div(
      cls := "bg-white-800 p-4",
      div(),
      CollapsibleList(
        header = div(
          cls := "flex items-center justify-between w-full",
          span("PDs"),
          // span(
          ButtonGroup(List("On", "Off")),
          // span(cls := "ml-10"),
          Badge("8")
          // )
        ),
        items = pds.map(p =>
          div(
            cls := "flex items-center justify-between w-full",
            span(p.name),
            ButtonGroup(p.actions)
          )
        )
      )
    )
  }
}
