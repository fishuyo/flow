
package flow
package protocol

import julienrf.json.derived


sealed trait Message

case class ClientHandshake() extends Message

case class Device(name:String, count:Int, elements:Seq[String]) extends Message
case class DeviceList(devices:Seq[Device]) extends Message

case class AppList(apps:Seq[String]) extends Message

case class Mapping(name:String, code:String) extends Message
case class MappingList(mappings:Seq[Mapping]) extends Message
case class Run(mapping:Mapping) extends Message

case class WatchParameter(name:String, device:String) extends Message
case class Parameter(name:String, device:String, value:Float) extends Message

object Message {
  implicit val deviceFormat = derived.oformat[Device]()
  implicit val mappingFormat = derived.oformat[Mapping]()
  implicit val format = derived.oformat[Message]()
}
