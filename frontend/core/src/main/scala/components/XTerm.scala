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
          val options = js.Dynamic.literal(
            theme = js.Dynamic.literal(
              background = "#1a1a1a",
              foreground = "#00ff00",
              cursor = "#00ff00"
            ),
            fontSize = 14,
            fontFamily = "'Courier New', monospace",
            cursorBlink = true,
            cursorStyle = "block"
          ).asInstanceOf[ITerminalOptions & ITerminalInitOnlyOptions]
          val terminal = new Terminal(options)
          
          // Open terminal in container
          terminal.open(container)
          
          terminalRef.set(Some(terminal))
          dom.console.log("[XTerm] Terminal initialized")
          
          // Connect to WebSocket
          MultishellSocket.connect()
          
          // Handle terminal input (user typing)
          // xterm.js handles displaying user input automatically
          // We only send complete lines (when Enter is pressed) to the shell
          terminal.onData((data: String, _: Unit) => {
            dom.console.log(s"[XTerm] User input received: '${data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
            
            // Check if this is a complete line (Enter was pressed)
            if (data.contains("\r") || data.contains("\n")) {
              // Extract the command (remove newline characters)
              val command = data.replaceAll("\r\n", "").replaceAll("\n", "").replaceAll("\r", "")
              dom.console.log(s"[XTerm] Sending complete command to shell: '$command'")
              
              // Only send non-empty commands
              if (command.nonEmpty) {
                MultishellSocket.sendCommand(command)
              } else {
                // Empty line - just send newline to shell (for commands that read until empty line)
                MultishellSocket.sendCommand("")
              }
            }
            // For individual characters (not Enter), xterm.js already displays them
            // We don't send them to the shell - the shell will receive the complete line when Enter is pressed
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
            dom.console.log(s"[XTerm] Received message from stream: $message")
            message match {
              case ShellOutput(text) =>
                dom.console.log(s"[XTerm] ShellOutput: $text")
                terminal.write(text)
                terminal.write("\r\n")
              case ShellError(text) =>
                dom.console.log(s"[XTerm] ShellError: $text")
                terminal.write(s"\u001b[31m$text\u001b[0m") // Red color
                terminal.write("\r\n")
              case ShellExit(code) =>
                dom.console.log(s"[XTerm] ShellExit: $code")
                terminal.writeln(s"\u001b[33m[Process exited with code: $code]\u001b[0m")
              case _ =>
                dom.console.log(s"[XTerm] Unknown message type: $message")
            }
          }(ctx.owner)
          
          // Handle window resize (terminal will auto-fit on next render)
          val resizeHandler: js.Function1[dom.Event, Unit] = { _ =>
            // Terminal will handle resize automatically
          }
          dom.window.addEventListener("resize", resizeHandler)
          
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
