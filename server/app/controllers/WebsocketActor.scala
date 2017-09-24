package controllers

import flow.protocol._
import flow.protocol.Message.format

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import akka.actor._

import com.fishuyo.seer.interface._
import com.fishuyo.seer.dynamic._
import com.fishuyo.seer.dynamic.ScriptLoaderActor._
import com.fishuyo.seer.hid.DeviceManager

object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))
}

class WebsocketActor(out: ActorRef) extends Actor {

  val script = ScriptManager()  // TODO: need multiple script actors for each mapping script running

  def receive = {
    case msg:String if msg == "keepalive" => ()
    case msg:String => 
      println(msg)
      val message = Json.parse(msg).as[Message]
      message match {
        case ClientHandshake() => 
          sendDeviceList()
          sendMappingList()
        case Run(Mapping(name, code, modified, running)) =>
          script ! Code(FlowScriptWrapper(code)); script ! Reload

      }

    case msg => println(msg)
  }

  def sendDeviceList() = {
    val devices = DeviceManager.devices.values
    val seq = devices.map { case ds => Device(ds.head.device.getProduct(), ds.length, Seq("button1","button2")) }.toSeq
    out ! Json.toJson(DeviceList(seq)).toString
  }

  def sendMappingList() = {
    val ms = MappingList(Seq(Mapping("mapping1", """println("mapping1")"""), Mapping("mapping2", """println("mapping2")""")))
    out ! Json.toJson(ms).toString
  }

}
