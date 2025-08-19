package client
package components

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object ButtonGroup {
  // Simple version with just labels and callbacks
  def apply(
    labels: List[String],
    onClicks: List[() => Unit] = List.empty
  ) = {
    div(
      cls := "inline-flex rounded-full shadow-md cursor-pointer overflow-hidden",
      labels.zipWithIndex.map { case (label, index) =>
        span(
          cls := "btn px-4 py-2 first:rounded-l-full last:rounded-r-full border-r-1 border-solid transition-all duration-150 bg-white text-gray-700 hover:bg-gray-200 active:scale-95 active:bg-gray-300 relative overflow-hidden",
          label,
          onClick.mapToUnit --> Observer[Unit](_ => {
            onClicks.lift(index).foreach(_())
          }),
          // Simple click animation
          onClick.stopPropagation --> Observer.empty,
          onClick --> Observer[org.scalajs.dom.MouseEvent](event => {
            // Add a temporary class for animation
            val element = event.target.asInstanceOf[org.scalajs.dom.Element]
            element.classList.add("animate-pulse")
            org.scalajs.dom.window.setTimeout(() => {
              element.classList.remove("animate-pulse")
            }, 150)
          })
        )
      }
    )
  }

  // Toggle group - only one button can be selected at a time
  def toggle(
    labels: List[String],
    onSelectionChange: Observer[Int] = Observer.empty,
    selectedIndex: Var[Int] = Var(0)
  ) = {
    div(
      cls := "inline-flex rounded-full shadow-md cursor-pointer overflow-hidden",
      labels.zipWithIndex.map { case (label, index) =>
        span(
          cls := s"btn px-4 py-2 first:rounded-l-full last:rounded-r-full border-r-1 border-solid transition-all duration-150 ${
            if (index == selectedIndex.now()) "bg-blue-500 text-white" else "bg-white text-gray-700 hover:bg-gray-200"
          } active:scale-95 relative overflow-hidden",
          label,
          onClick.mapToUnit --> Observer[Unit](_ => {
            selectedIndex.set(index)
            onSelectionChange.onNext(index)
          }),
          // Simple click animation
          onClick --> Observer[org.scalajs.dom.MouseEvent](event => {
            // Add a temporary class for animation
            val element = event.target.asInstanceOf[org.scalajs.dom.Element]
            element.classList.add("animate-pulse")
            org.scalajs.dom.window.setTimeout(() => {
              element.classList.remove("animate-pulse")
            }, 150)
          })
        )
      }
    )
  }
}

// onClick.stopPropagation --> Observer.empty, // Prevent header toggle when clicking actions




