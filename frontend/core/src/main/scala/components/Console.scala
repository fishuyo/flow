package client
package components

import com.raquo.laminar.api.L.{*, given}
import websocket.MultishellSocket
import multishell.protocol._
import org.scalajs.dom

object Console {
  def apply() = {
    val outputLines = Var(List[String]())
    val inputValue = Var("")
    val outputElementRef = Var[Option[dom.html.Element]](None)

    def handleSubmit(): Unit = {
      val command = inputValue.now().trim
      dom.console.log(s"[Console] handleSubmit called with command: '$command'")
      if (command.nonEmpty) {
        dom.console.log(
          s"[Console] Sending command to MultishellSocket: '$command'"
        )
        MultishellSocket.sendCommand(command)
        outputLines.update(_ :+ s"> $command")
        inputValue.set("")
      } else {
        dom.console.log("[Console] Empty command, ignoring")
      }
    }

    div(
      cls := "bg-gray-900 text-green-400 p-4 rounded-lg font-mono shadow-lg max-w-xl mx-auto mt-10",
      onMountCallback { ctx =>
        dom.console.log(
          "[Console] Component mounted, connecting to MultishellSocket"
        )
        MultishellSocket.connect()

        // Observe connection status changes
        MultishellSocket.isConnected.foreach { connected =>
          dom.console.log(s"[Console] Connection status changed: $connected")
        }(ctx.owner)

        // Observe WebSocket messages
        MultishellSocket.outputStream.foreach { message =>
          dom.console.log(s"[Console] Received message from stream: $message")
          message match {
            case m: ShellOutput =>
              dom.console.log(s"[Console] ShellOutput: ${m.text}")
              outputLines.update(lines => {
                val newLines = lines :+ m.text
                dom.console.log(
                  s"[Console] Updated outputLines, new length: ${newLines.length}"
                )
                newLines
              })
              // Auto-scroll to bottom after a brief delay to allow DOM update
              dom.window.setTimeout(
                () => {
                  outputElementRef.now().foreach { elem =>
                    elem.scrollTop = elem.scrollHeight.toDouble
                  }
                },
                10
              )
            case m: ShellError =>
              dom.console.log(s"[Console] ShellError: ${m.text}")
              outputLines.update(lines => {
                val newLines = lines :+ s"[ERROR] ${m.text}"
                dom.console.log(
                  s"[Console] Updated outputLines with error, new length: ${newLines.length}"
                )
                newLines
              })
              dom.window.setTimeout(
                () => {
                  outputElementRef.now().foreach { elem =>
                    elem.scrollTop = elem.scrollHeight.toDouble
                  }
                },
                10
              )
            case m: ShellExit =>
              dom.console.log(s"[Console] ShellExit: ${m.code}")
              outputLines.update(lines => {
                val newLines = lines :+ s"[Process exited with code: ${m.code}]"
                dom.console.log(
                  s"[Console] Updated outputLines with exit, new length: ${newLines.length}"
                )
                newLines
              })
              dom.window.setTimeout(
                () => {
                  outputElementRef.now().foreach { elem =>
                    elem.scrollTop = elem.scrollHeight.toDouble
                  }
                },
                10
              )
            case _ =>
              dom.console.log(s"[Console] Unknown message type: $message")
          }
        }(ctx.owner)
      },
      div(
        cls := "h-64 overflow-y-auto mb-4",
        inContext { thisNode =>
          onMountCallback { _ =>
            outputElementRef.set(Some(thisNode.ref))
          }
        },
        children <-- outputLines.signal.map { lines =>
          dom.console.log(
            s"[Console] Rendering ${lines.length} lines: ${lines.mkString(", ")}"
          )
          lines.map { line =>
            p(
              cls := "mb-1 whitespace-pre-wrap",
              line
            )
          }
        }
      ),
      div(
        cls := "flex items-center",
        span(cls := "text-green-500 mr-2", ">"),
        input(
          cls := "bg-transparent border-none outline-none text-green-400 flex-grow",
          placeholder := "Enter command..",
          typ := "text",
          controlled(
            value <-- inputValue.signal,
            onInput.mapToValue --> inputValue.writer
          ),
          onKeyDown --> { ev =>
            dom.console.log(
              s"[Console] Key pressed: key='${ev.key}', code='${ev.code}'"
            )
            if (ev.key == "Enter" || ev.keyCode == 13) {
              dom.console.log(
                "[Console] Enter key detected, calling handleSubmit"
              )
              handleSubmit()
            }
          }
        )
      )
    )
  }
}
