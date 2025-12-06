package client
package components

import com.raquo.laminar.api.L.{*, given}
import websocket.MultishellSocket
import multishell.protocol._
import org.scalajs.dom
import typings.xterm.mod.*
import scala.scalajs.js

object XTerm {
  def apply() = {
    val terminalRef = Var[Option[Terminal]](None)

    div(
      cls := "bg-gray-900 text-green-400 p-4 rounded-lg font-mono shadow-lg max-w-xl mx-auto mt-10",
      idAttr := "xterm-container",
      styleAttr := "height: 400px; width: 100%;",
      inContext { thisNode =>
        onMountCallback { ctx =>
          dom.console.log("[XTerm] Component mounted, initializing xterm")

          // Get container element directly from thisNode
          val container = thisNode.ref.asInstanceOf[dom.HTMLElement]
          // Create and configure xterm terminal
          val options = js.Dynamic
            .literal(
              theme = js.Dynamic.literal(
                // background = "#1a1a1a",
                // foreground = "#00ff00",
                // cursor = "#00ff00"
              ),
              fontSize = 14,
              fontFamily = "'Courier New', monospace",
              cursorBlink = true,
              cursorStyle = "block"
            )
            .asInstanceOf[ITerminalOptions & ITerminalInitOnlyOptions]
          val terminal = new Terminal(options)

          // Open terminal in container
          terminal.open(container)

          terminalRef.set(Some(terminal))
          dom.console.log("[XTerm] Terminal initialized")

          // Connect to WebSocket
          MultishellSocket.connect()

          // Get initial terminal size and send resize message
          val initialCols = terminal.cols
          val initialRows = terminal.rows
          dom.console.log(
            s"[XTerm] Initial terminal size: ${initialRows}x${initialCols}"
          )

          // Send initial resize to start PTY with correct size
          MultishellSocket.sendResize(initialRows.toInt, initialCols.toInt)

          // Handle terminal input (raw mode - send all characters)
          // xterm.js handles displaying user input automatically
          // We send ALL input directly to the PTY for full interactivity
          terminal.onData((data: String, _: Unit) => {
            // Send all input data directly to backend (raw mode)
            MultishellSocket.sendInput(data)
          })

          // Handle terminal resize events
          terminal.onResize((size: typings.xterm.anon.Cols, _: Unit) => {
            val rows = size.rows.toInt
            val cols = size.cols.toInt
            dom.console.log(s"[XTerm] Terminal resized to: ${rows}x${cols}")
            MultishellSocket.sendResize(rows, cols)
          })

          // Observe connection status changes
          MultishellSocket.isConnected.foreach { connected =>
            dom.console.log(s"[XTerm] Connection status changed: $connected")
            if (connected) {
              terminal.writeln("[Connected to shell]")
            } else {
              terminal.writeln("[Disconnected from shell]")
            }
          }(ctx.owner)

          // Observe WebSocket messages and write to terminal
          MultishellSocket.outputStream.foreach { message =>
            message match {
              case ShellOutput(text) =>
                // Write raw output (preserves ANSI escape codes)
                // PTY handles newlines, so we don't add them manually
                terminal.write(text)
              case ShellError(text) =>
                // Write error output (PTY may send errors as regular output, but we can color them)
                terminal.write(text)
              case ShellExit(code) =>
                dom.console.log(s"[XTerm] ShellExit: $code")
                terminal.writeln(
                  s"\u001b[33m[Process exited with code: $code]\u001b[0m"
                )
              case _ =>
                dom.console.log(s"[XTerm] Unknown message type: $message")
            }
          }(ctx.owner)

          // Store references for cleanup
          terminalRef.set(Some(terminal))

          // Cleanup will be handled by onUnmountCallback below
        }
      },
      onUnmountCallback { _ =>
        dom.console.log("[XTerm] Component unmounting, cleaning up")
        terminalRef.now().foreach { terminal =>
          terminal.dispose()
        }
        MultishellSocket.disconnect()
      }
    )
  }
}
