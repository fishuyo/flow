package flow

import protocol._
import protocol.Message.format

import hid.DeviceManager

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import akka.actor._
import akka.http.scaladsl.model.ws.TextMessage


object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))

  def sendAppList() = System().actorSelection("akka://flow/user/client.*/flowActor") ! "sendAppList"
  def sendDeviceList() = System().actorSelection("akka://flow/user/client.*/flowActor") ! "sendDeviceList"
  def sendMappingList() = System().actorSelection("akka://flow/user/client.*/flowActor") ! "sendMappingList"
  def sendMapping(m:Mapping) = System().actorSelection("akka://flow/user/client.*/flowActor") ! m
  def sendInterfaceList() = System().actorSelection("akka://flow/user/client.*/flowActor") ! "sendInterfaceList"
}

class WebsocketActor(out: ActorRef) extends Actor {

  def receive = {
    case TextMessage.Strict(msg) if msg == "keepalive" => ()
    case "sendDeviceList" => sendDeviceList()
    case "sendAppList" => sendAppList()
    case "sendMappingList" => sendMappingList()
    case "sendInterfaceList" => sendInterfaceList()
    case TextMessage.Strict(msg) => 
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
        case StopAll => 
          MappingManager.stopAll()
          AppManager.closeAll()
      }

    case m:Mapping => sendMapping(m)

    case msg => println(msg)
  }

  def sendDeviceList() = {
    val devices = DeviceManager.getDevices()
    // val devices = DeviceManager.getRegisteredDevices()
    val seq = devices.groupBy(_.info.getProductString).map { case (k,ds) => 
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
    println(seq)
    out ! TextMessage(Json.toJson(DeviceList(seq)).toString)
  }

  def sendAppList() = {
    val apps = AppManager.getAppList() 
    out ! TextMessage(Json.toJson(AppList(apps)).toString)
  }

  def sendMappingList() = {
    // MappingManager.readMappingsDir() // XXX modifying state, probs not safe
    val ms = MappingManager.mappings.values.toSeq 
    out ! TextMessage(Json.toJson(MappingList(ms)).toString)
  }

  def sendMapping(m:Mapping) = {
    out ! TextMessage(Json.toJson(m).toString)
  }

  def sendInterfaceList() = {
    val is = ijs.Interface.interfaces.keys.toSeq 
    out ! TextMessage(Json.toJson(InterfaceList(is)).toString)
  }

}
