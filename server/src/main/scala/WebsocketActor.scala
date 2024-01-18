
package flow

import protocol._
import protocol.Message.format

import hid.DeviceManager

import com.github.plokhotnyuk.jsoniter_scala.core._
// import julienrf.json.derived._
// import play.api.libs.json._
// import play.api.libs.functional.syntax._

import org.apache.pekko._
import org.apache.pekko.actor._
import org.apache.pekko.http.scaladsl.model.ws._


object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))

  def sendAppList() = System().actorSelection("pekko://application/user/client.*/flowActor") ! "sendAppList"
  def sendDeviceList() = System().actorSelection("pekko://application/user/client.*/flowActor") ! "sendDeviceList"
  def sendMapping(m:Mapping) = System().actorSelection("pekko://application/user/client.*/flowActor") ! m
}

class WebsocketActor(out: ActorRef) extends Actor {

  def receive = {
    // case x => println(s"WebsocketActor got message: $x")
    case TextMessage.Strict(msg) if msg == "keepalive" => ()
    case TextMessage.Strict(msg) if msg == "sendDeviceList" => sendDeviceList()
    case TextMessage.Strict(msg) if msg == "sendAppList" => sendAppList()
    case TextMessage.Strict(msg) => 
      println(msg)
      try {
        val message = readFromString[protocol.Message](msg)
        message match {
          case ClientHandshake(msg) => 
            sendDeviceList()
            sendAppList()
            sendMappingList()

          case Run(mapping) => MappingManager.run(mapping)
          case Stop(mapping) => MappingManager.stop(mapping)
          case Save(mapping) => MappingManager.save(mapping)
          case StopAll => 
            MappingManager.stopAll()
            AppManager.closeAll()
          case m => println(m)
        }
      } catch {
        case e:Exception => println(e)
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
    out ! writeToString(DeviceList(seq))
  }

  def sendAppList() = {
    val apps = AppManager.getAppList() 
    out ! writeToString(AppList(apps))
  }

  def sendMappingList() = {
    MappingManager.readMappingsDir() // XXX modifying state, probs not safe
    val ms = MappingManager.mappings.values.toSeq 
    out ! writeToString(MappingList(ms))
  }

  def sendMapping(m:Mapping) = {
    out ! writeToString(m)
  }

}
