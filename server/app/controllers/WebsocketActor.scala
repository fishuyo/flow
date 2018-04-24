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

  def sendAppList() = System().actorSelection("akka://application/user/client.*/flowActor") ! "sendAppList"
  def sendDeviceList() = System().actorSelection("akka://application/user/client.*/flowActor") ! "sendDeviceList"
  def sendMapping(m:Mapping) = System().actorSelection("akka://application/user/client.*/flowActor") ! m
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

    case m:Mapping => sendMapping(m)

    case msg => println(msg)
  }

  def sendDeviceList() = {
    val devices = DeviceManager.getDevices()
    // val devices = DeviceManager.getRegisteredDevices()
    val seq = devices.collect { case ds if ds.length > 0 => 
      val di = ds.head.info
      val d = flow.Device(di.getProductString, -1) // temporary device, negative index
      protocol.Device(
        IOConfig(
          di.getProductString,
          d.sources.map{ case (n,s) => IOPort(n,"")}.toSeq,
          d.sinks.map{ case (n,s) => IOPort(n,"")}.toSeq
        ),
        ds.length
      ) 
    }.toSeq
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

  def sendMapping(m:Mapping) = {
    out ! Json.toJson(m).toString
  }

}
