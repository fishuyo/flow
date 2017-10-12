package controllers

import flow._
import flow.protocol._
import flow.protocol.Message.format

import flow.hid.DeviceManager

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import akka.actor._


object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))
}

class WebsocketActor(out: ActorRef) extends Actor {

  def receive = {
    case msg:String if msg == "keepalive" => ()
    case msg:String if msg == "sendDeviceList" => sendDeviceList()
    case msg:String if msg == "sendAppList" => sendAppList()
    case msg:String => 
      println(msg)
      val message = Json.parse(msg).as[Message]
      message match {
        case ClientHandshake() => 
          sendDeviceList()
          sendAppList()
          sendMappingList()

        case Run(mapping) => MappingManager.run(mapping)
        case Stop(mapping) => MappingManager.stop(mapping)
        case Save(mapping) => MappingManager.save(mapping)
      }

    case msg => println(msg)
  }

  def sendDeviceList() = {
    // val devices = DeviceManager.devices.values
    val devices = DeviceManager.getRegisteredDevices.values
    val seq = devices.map { case ds => Device(ds.head.device.getProduct(), ds.length, ds.head.elements.map(_.name) ) }.toSeq
    out ! Json.toJson(DeviceList(seq)).toString
  }

  def sendAppList() = {
    val apps = AppManager.apps.values.map(_.config).toSeq
    out ! Json.toJson(AppList(apps)).toString
  }

  def sendMappingList() = {
    MappingManager.readMappingsDir() // XXX modifying state, probs not safe
    val ms = MappingManager.mappings.values.toSeq 
    out ! Json.toJson(MappingList(ms)).toString
  }

}
