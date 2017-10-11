
package flow
package protocol

import julienrf.json.derived

sealed trait Message

case class ClientHandshake() extends Message

case class Device(name:String, count:Int, elements:Seq[String]) extends Message
case class DeviceList(devices:Seq[Device]) extends Message

case class OSCConfig(address:String, sinkPort:Int) extends Message
case class AppConfig(name:String, sources:Seq[String], sinks:Seq[String], defaultMappings:Seq[String]) extends Message
case class AppList(apps:Seq[String]) extends Message

case class Mapping(name:String, code:String, modified:Boolean=false, running:Boolean=false, errors:Boolean=false) extends Message
case class MappingList(mappings:Seq[Mapping]) extends Message
// case class MappingTree(name:String, mappings:Seq[Mapping], trees:Seq[MappingTree]) extends Message

case class Run(mapping:Mapping) extends Message
case class Stop(mapping:Mapping) extends Message
case class Save(mapping:Mapping) extends Message

case class Success(text:String) extends Message
case class Error(text:String) extends Message
case class Info(text:String) extends Message

case class WatchParameter(name:String, device:String) extends Message
case class Parameter(name:String, device:String, value:Float) extends Message

object Message {
  implicit val deviceFormat = derived.oformat[Device]()
  implicit val mappingFormat = derived.oformat[Mapping]()
  // implicit val mappingTreeFormat = derived.oformat[MappingTree]()
  implicit val oscConfigFormat = derived.oformat[OSCConfig]()
  implicit val appConfigFormat = derived.oformat[AppConfig]()
  implicit val format = derived.oformat[Message]()
}
