package client
package websocket

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import upickle.default._
import multishell.protocol._

object MultishellSocket {
  private var ws: Option[WebSocket] = None
  private val outputBus = new EventBus[Message]
  private val connectionStatus = Var(false)

  val outputStream: EventStream[Message] = outputBus.events
  val isConnected: Signal[Boolean] = connectionStatus.signal

  def connect(): Unit = {
    if (ws.isEmpty || ws.exists(_.readyState == WebSocket.CLOSED)) {
      val protocol = if (dom.window.location.protocol == "https:") "wss" else "ws"
      val host = dom.window.location.host
      val url = s"$protocol://$host/api/multishell/ws"
      
      dom.console.log(s"[MultishellSocket] Connecting to WebSocket: $url")
      
      try {
        val socket = new WebSocket(url)
        
        socket.onopen = { _ =>
          dom.console.log("[MultishellSocket] WebSocket connected successfully")
          connectionStatus.set(true)
          ws = Some(socket)
        }
        
        socket.onmessage = { event =>
          try {
            val rawMessage = event.data.toString
            dom.console.log(s"[MultishellSocket] Received message: $rawMessage")
            val message = read[Message](rawMessage)
            dom.console.log(s"[MultishellSocket] Parsed message: $message")
            outputBus.emit(message)
          } catch {
            case e: Exception =>
              dom.console.error(s"[MultishellSocket] Error parsing WebSocket message: $e")
              e.printStackTrace()
          }
        }
        
        socket.onerror = { event =>
          dom.console.error(s"[MultishellSocket] WebSocket error: $event")
          connectionStatus.set(false)
        }
        
        socket.onclose = { event =>
          dom.console.log(s"[MultishellSocket] WebSocket closed: code=${event.code}, reason=${event.reason}, wasClean=${event.wasClean}")
          connectionStatus.set(false)
          ws = None
        }
      } catch {
        case e: Exception =>
          dom.console.error(s"[MultishellSocket] Failed to create WebSocket: $e")
          e.printStackTrace()
          connectionStatus.set(false)
      }
    } else {
      dom.console.log(s"[MultishellSocket] WebSocket already exists, state: ${ws.map(_.readyState)}")
    }
  }

  def disconnect(): Unit = {
    ws.foreach(_.close())
    ws = None
    connectionStatus.set(false)
  }

  def sendCommand(command: String): Unit = {
    dom.console.log(s"[MultishellSocket] sendCommand called with: '$command'")
    ws match {
      case Some(socket) =>
        val state = socket.readyState
        dom.console.log(s"[MultishellSocket] WebSocket state: $state (OPEN=${WebSocket.OPEN})")
        if (state == WebSocket.OPEN) {
          try {
            val message = write(ShellInput(command))
            dom.console.log(s"[MultishellSocket] Sending command message: $message")
            socket.send(message)
            dom.console.log(s"[MultishellSocket] Command sent successfully")
          } catch {
            case e: Exception =>
              dom.console.error(s"[MultishellSocket] Error sending command: $e")
              e.printStackTrace()
          }
        } else {
          dom.console.log(s"[MultishellSocket] WebSocket not connected (state=$state), attempting to reconnect...")
          connect()
          // Try again after a short delay
          dom.window.setTimeout(() => sendCommand(command), 500)
        }
      case None =>
        dom.console.log("[MultishellSocket] No WebSocket, connecting first...")
        connect()
        dom.window.setTimeout(() => sendCommand(command), 500)
    }
  }
}

