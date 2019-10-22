
package flow
package protocol

import julienrf.json.derived

sealed trait Message

case class ClientHandshake() extends Message

case class IOPort(name:String, `type`:String) extends Message
case class IOConfig(name:String, sources:Seq[IOPort], sinks:Seq[IOPort]) extends Message

case class Device(io:IOConfig, count:Int) extends Message
case class DeviceList(devices:Seq[Device]) extends Message

// case class OSCConfig(address:String, sinkPort:Int) extends Message
case class AppConfig(io:IOConfig, defaultMappings:Seq[String]=Seq()) extends Message
case class AppList(apps:Seq[AppConfig]) extends Message

case class MappingError(line:Int, message:String) extends Message
case class Mapping(name:String, code:String, modified:Boolean=false, running:Boolean=false, errors:Seq[MappingError]=Seq()) extends Message
case class MappingList(mappings:Seq[Mapping]) extends Message
// case class MappingTree(name:String, mappings:Seq[Mapping], trees:Seq[MappingTree]) extends Message

case class Run(mapping:Mapping) extends Message
case class Stop(mapping:Mapping) extends Message
case class Save(mapping:Mapping) extends Message
case object StopAll extends Message

case class Success(text:String) extends Message
case class Error(text:String) extends Message
case class Info(text:String) extends Message

case class WatchParameter(name:String, device:String) extends Message
case class Parameter(name:String, device:String, value:Float) extends Message

object Message {
  implicit val ioportFormat = derived.oformat[IOPort]()
  implicit val ioconfigFormat = derived.oformat[IOConfig]()

  implicit val deviceFormat = derived.oformat[Device]()
  
  implicit val mappingErrorFormat = derived.oformat[MappingError]()
  implicit val mappingFormat = derived.oformat[Mapping]()
  // implicit val mappingTreeFormat = derived.oformat[MappingTree]()
  // implicit val oscConfigFormat = derived.oformat[OSCConfig]()
  implicit val appConfigFormat = derived.oformat[AppConfig]()
  implicit val format = derived.oformat[Message]()
}
