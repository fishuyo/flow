package client
package components

import com.raquo.laminar.api.L.{*, given}
import websocket.MultishellSocket
import multishell.protocol._
import org.scalajs.dom
import typings.xterm.mod.*
import scala.scalajs.js

object XTerm {
  def apply(instanceId: String = "default") = {
    val terminalRef = Var[Option[Terminal]](None)

    div(
      cls := "bg-gray-900 text-green-400 font-mono h-full w-full flex flex-col relative",
      idAttr := s"xterm-container-$instanceId",
      styleAttr := "min-height: 0; margin: 0; padding: 0; border: none;",
      inContext { thisNode =>
        onMountCallback { ctx =>
          dom.console.log(s"[XTerm:$instanceId] Component mounted, initializing xterm")

          // Get container element directly from thisNode
          val container = thisNode.ref.asInstanceOf[dom.HTMLElement]
          
          // Variable to hold terminal instance
          var terminalOpt: Option[Terminal] = None
          
          // Wait for container to have valid dimensions before initializing
          def initializeTerminal(): Unit = {
            val rect = container.getBoundingClientRect()
            val containerWidth = rect.width
            val containerHeight = rect.height
            
            dom.console.log(s"[XTerm:$instanceId] Container size: ${containerWidth.toInt}x${containerHeight.toInt}")
            
            // xterm.js can work with very small containers - it will just calculate fewer rows/cols
            // The minimum is really just to ensure the container has been laid out by the browser
            // With 13 terminals in 2 columns on a typical screen (e.g., 1920x1080):
            // - Each terminal gets ~50% width = ~960px
            // - Each terminal gets ~7.7% height = ~83px
            // This is plenty for xterm.js to render (can work with as little as ~30px height)
            if (containerWidth <= 0 || containerHeight <= 0) {
              // Container hasn't been laid out yet, retry with requestAnimationFrame
              dom.console.log(s"[XTerm:$instanceId] Container not laid out yet (${containerWidth.toInt}x${containerHeight.toInt}), retrying...")
              dom.window.requestAnimationFrame { _ =>
                initializeTerminal()
              }
              return
            }
            
            // Even if container is very small, xterm.js can handle it
            // It will just calculate fewer rows/cols - minimum is really just > 0
            
            // Adaptive font size based on container height
            // Smaller containers get smaller fonts to fit more content
            val fontSize = if (containerHeight < 80) 8 else if (containerHeight < 120) 9 else 10
            
            // Create and configure xterm terminal
            val options = js.Dynamic
              .literal(
                theme = js.Dynamic.literal(
                  // background = "#1a1a1a",
                  // foreground = "#00ff00",
                  // cursor = "#00ff00"
                ),
                fontSize = fontSize, // Adaptive font size based on container
                fontFamily = "'Courier New', monospace",
                cursorBlink = true,
                cursorStyle = "block",
                allowProposedApi = true // Enable proposed API for better small terminal support
              )
              .asInstanceOf[ITerminalOptions & ITerminalInitOnlyOptions]
            val terminal = new Terminal(options)

            // Open terminal in container
            terminal.open(container)
            
            terminalOpt = Some(terminal)
            terminalRef.set(Some(terminal))
            dom.console.log(s"[XTerm:$instanceId] Terminal initialized")

            // Connect to WebSocket (shared connection - only connects once globally)
            MultishellSocket.connect()

            // Track if we've already created this shell instance
            var shellCreated = false
            
            // Wait a bit for connection to establish, then create shell instance (only once)
            // Stagger creation to avoid overwhelming server with simultaneous requests
            val instanceIndex = instanceId match {
              case s if s.startsWith("host") => 
                try { s.substring(4).toInt - 1 } catch { case _: Exception => 0 }
              case _ => 0
            }
            val creationDelay = 500 + (instanceIndex * 100) // Stagger by 100ms per instance
            
            dom.window.setTimeout(() => {
              if (!shellCreated) {
                shellCreated = true
                dom.console.log(s"[XTerm:$instanceId] Creating shell instance (delay: ${creationDelay}ms)...")
                MultishellSocket.createShell(instanceId, None)
              }
            }, creationDelay)

            // Get initial terminal size and send resize message
            // Wait for shell to be created before sending resize
            dom.window.setTimeout(() => {
              val initialCols = terminal.cols
              val initialRows = terminal.rows
              dom.console.log(
                s"[XTerm:$instanceId] Initial terminal size: ${initialRows}x${initialCols} (container: ${containerWidth.toInt}x${containerHeight.toInt})"
              )

              // Send initial resize to start PTY with correct size
              // Wait a bit more to ensure shell instance is created on backend
              dom.window.setTimeout(() => {
                if (initialRows > 0 && initialCols > 0) {
                  MultishellSocket.sendResize(instanceId, initialRows.toInt, initialCols.toInt)
                  dom.console.log(s"[XTerm:$instanceId] Sent initial resize: ${initialRows}x${initialCols}")
                } else {
                  dom.console.warn(s"[XTerm:$instanceId] Invalid terminal size: ${initialRows}x${initialCols}")
                }
              }, 300) // Wait for shell creation to complete
            }, 200) // Delay to ensure terminal is ready
            
            // Set up window resize listener to handle container size changes
            val resizeHandler: dom.Event => Unit = { _ =>
              if (terminalOpt.isDefined) {
                val term = terminalOpt.get
                val rect = container.getBoundingClientRect()
                // Lower threshold - terminals can work with very small sizes
                if (rect.width >= 20 && rect.height >= 20) {
                  // Force terminal to recalculate size based on current container dimensions
                  term.resize(term.cols, term.rows)
                  val newCols = term.cols.toInt
                  val newRows = term.rows.toInt
                  if (newCols > 0 && newRows > 0) {
                    MultishellSocket.sendResize(instanceId, newRows, newCols)
                  }
                }
              }
            }
            dom.window.addEventListener("resize", resizeHandler)
            
            // Handle terminal input (raw mode - send all characters)
            terminal.onData((data: String, _: Unit) => {
              dom.console.log(s"[XTerm:$instanceId] Terminal input received: '${data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
              MultishellSocket.sendInput(instanceId, data)
            })
            
            // Debug: Verify terminal is ready for input
            dom.console.log(s"[XTerm:$instanceId] Terminal input handler set up")

            // Handle terminal resize events
            terminal.onResize((size: typings.xterm.anon.Cols, _: Unit) => {
              val rows = size.rows.toInt
              val cols = size.cols.toInt
              if (rows > 0 && cols > 0) {
                dom.console.log(s"[XTerm:$instanceId] Terminal resized to: ${rows}x${cols}")
                MultishellSocket.sendResize(instanceId, rows, cols)
              }
            })

            // Observe WebSocket messages and write to terminal (filter by instanceId)
            MultishellSocket.outputStream.foreach { message =>
              message match {
                case m: ShellOutput if m.instanceId == instanceId =>
                  dom.console.log(s"[XTerm:$instanceId] Received ShellOutput: ${m.text.take(50)}...")
                  terminal.write(m.text)
                case m: ShellError if m.instanceId == instanceId =>
                  dom.console.log(s"[XTerm:$instanceId] Received ShellError: ${m.text.take(50)}...")
                  terminal.write(m.text)
                case m: ShellExit if m.instanceId == instanceId =>
                  dom.console.log(s"[XTerm:$instanceId] ShellExit: ${m.code}")
                  terminal.writeln(
                    s"\u001b[33m[Process exited with code: ${m.code}]\u001b[0m"
                  )
                case _ =>
                  // Ignore messages for other instances
                  ()
              }
            }(ctx.owner)
            
            // Debug: Log when input handler is set up
            dom.console.log(s"[XTerm:$instanceId] Input handler configured")
          }
          
          // Start initialization - try immediately, then retry if needed
          // Use requestAnimationFrame to ensure layout is complete
          dom.window.requestAnimationFrame { _ =>
            initializeTerminal()
          }


          // Cleanup will be handled by onUnmountCallback below
        }
      },
      onUnmountCallback { _ =>
        dom.console.log(s"[XTerm:$instanceId] Component unmounting, cleaning up")
        terminalRef.now().foreach { terminal =>
          terminal.dispose()
        }
        // Don't disconnect the socket as other instances might be using it
        // Note: resize handler will be cleaned up automatically when component unmounts
      }
    )
  }
}
