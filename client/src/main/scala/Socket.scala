package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import protocol._
import protocol.Message.format


@JSExport
object Socket {
  
  var ws:WebSocket = _

  @JSExport
  def send(data:String){ ws.send(data) }
  
  def send(msg:Message){
    val json = Json.toJson(msg).toString()
    ws.send(json)
  }

  def init() = {

    ws = new WebSocket(getWebsocketUri())
    
    ws.onopen = { (event: Event) =>
      val json = Json.toJson(ClientHandshake()).toString()
      ws.send(json)
      event
    }

    ws.onerror = { (event: ErrorEvent) => () }

    ws.onmessage = { (event: MessageEvent) =>
      println(event.data.toString)
      val wsMsg = Json.parse(event.data.toString).as[Message]

      wsMsg match {
        case DeviceList(ds) => Devices.set(ds)
        case AppList(as) => Apps.set(as)
        case MappingList(ms) => Mappings ++= ms
        
      }
    }

    ws.onclose = { (event: Event) =>
      println("ws close")
      // playground.insertBefore(p("Connection to ws lost. You can try to rejoin manually."), playground.firstChild)
      // joinButton.disabled = false
      // sendButton.disabled = true
    }

    // def writeToArea(text: String): Unit = playground.insertBefore(p(text), playground.firstChild)
  }

  def getWebsocketUri(): String = {
    val wsProtocol = if (document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${document.location.host}/ws"
  }
}