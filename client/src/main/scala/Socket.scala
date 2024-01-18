package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.github.plokhotnyuk.jsoniter_scala.core._
// import julienrf.json.derived._
// import play.api.libs.json._
// import play.api.libs.functional.syntax._

import protocol._
import protocol.Message.format


@JSExportTopLevel("Socket")
object Socket {
  
  var ws:WebSocket = _

  @JSExport
  def send(data:String) = { println(data); ws.send(data) }
  
  def send(msg:Message) = {
    val json = writeToString(msg)
    // val json = """{"hi":"hello"}"""
    println(json)
    ws.send(json)
  }

  def init() = {

    ws = new WebSocket(getWebsocketUri())
    
    ws.onopen = { (event: Event) =>
      val json = writeToString(ClientHandshake())
      ws.send(json)
      event
    }

    ws.onerror = { (event: Event) => () }

    ws.onmessage = { (event: MessageEvent) =>
      println(event.data.toString)
      val wsMsg = readFromString[Message](event.data.toString)

      wsMsg match {
        // case DeviceList(ds) => Devices.set(ds)
        // case AppList(as) => Apps.set(as)
        // case MappingList(ms) => Mappings ++= ms
        // case m:Mapping => Mappings(m.name) = m
        case _ => 0
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