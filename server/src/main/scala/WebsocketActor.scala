
package flow

import protocol._
import protocol.Message._

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
    // case TextMessage.Strict(msg) if msg == "handshake" =>
      // sendDeviceList()
      // sendAppList()
      // sendMappingList()
    case TextMessage.Strict(msg) if msg == "sendDeviceList" => sendDeviceList()
    case TextMessage.Strict(msg) if msg == "sendAppList" => sendAppList()
    case TextMessage.Strict(msg) => 
      println(msg)
      try {
        val message = upickle.default.read[protocol.Message](msg)
        // val message = readFromString[protocol.Message](msg)
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
      var name = di.getProductString
      // if(name == null) name = "???"
      val d = flow.Device(name, -1) // temporary device, negative index
      protocol.Device(
        IOConfig(
          name,
          d.sources.map{ case (n,s) => IOPort(n,"")}.toSeq,
          d.sinks.map{ case (n,s) => IOPort(n,"")}.toSeq
        ),
        ds.length
      ) 
    }.toSeq
    // println(seq)
    val json = upickle.default.write(DeviceList(seq))
    // val json = writeToString(DeviceList(seq))
    // println(json)
    out ! TextMessage(json)
  }

  def sendAppList() = {
    val apps = AppManager.getAppList() 
    println(apps)
    val json = upickle.default.write(AppList(apps))
    // val json = writeToString(AppList(apps))
    out ! TextMessage(json)
  }

  def sendMappingList() = {
    MappingManager.readMappingsDir() // XXX modifying state, probs not safe
    val ms = MappingManager.mappings.values.toSeq 
    // println(ms)
    val json = upickle.default.write(MappingList(ms))
    // val json = writeToString(MappingList(ms))
    out ! TextMessage(json)
  }

  def sendMapping(m:Mapping) = {
    val json = upickle.default.write(m)
    // val json = writeToString(m)
    out ! TextMessage(json)
  }

}
