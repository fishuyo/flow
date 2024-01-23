package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

// import com.github.plokhotnyuk.jsoniter_scala.core._
// import julienrf.json.derived._
// import play.api.libs.json._
// import play.api.libs.functional.syntax._

import protocol._
// import protocol.Message._


@JSExportTopLevel("Socket")
object Socket {
  
  var ws:WebSocket = _

  @JSExport
  def send(data:String) = { println(data); ws.send(data) }
  
  def send(msg:Message) = {
    val json = upickle.default.write(msg)

    // val json = writeToString(msg)
    // println(json)
    ws.send(json)
  }

  def init() = {

    ws = new WebSocket(getWebsocketUri())
    
    ws.onopen = { (event: Event) =>
      val json = upickle.default.write(ClientHandshake())
      // val json = writeToString(ClientHandshake())
      // val json = "handshake"
      // println(json)
      ws.send(json)
      event
    }

    ws.onerror = { (event: Event) => () }

    ws.onmessage = { (event: MessageEvent) =>
      // println(event.data.toString)
      val wsMsg = upickle.default.read[Message](event.data.toString)
      // val wsMsg = readFromString[Message](event.data.toString)

      wsMsg match {
        case DeviceList(ds) => Devices.set(ds)
        case AppList(as) => Apps.set(as)
        case MappingList(ms) => Mappings ++= ms
        case m:Mapping => Mappings(m.name) = m
        case x => println(s"Websocket Match failed, got: $x")
      }
    }

    ws.onclose = { (event: Event) =>
      println("ws close")
    }

  }

  def getWebsocketUri(): String = {
    val wsProtocol = if (document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${document.location.host}/ws"
  }
}