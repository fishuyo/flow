
package flow
package protocol

// import julienrf.json.derived
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._


sealed trait Message

case class ClientHandshake(msg:String="") extends Message

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
  implicit val handshake: JsonValueCodec[ClientHandshake] = JsonCodecMaker.makeWithoutDiscriminator
  
  implicit val ioportFormat: JsonValueCodec[IOPort] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val ioconfigFormat: JsonValueCodec[IOConfig] = JsonCodecMaker.makeWithoutDiscriminator

  implicit val deviceFormat: JsonValueCodec[Device] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val deviceListFormat: JsonValueCodec[DeviceList] = JsonCodecMaker.makeWithoutDiscriminator
  
  implicit val mappingErrorFormat: JsonValueCodec[MappingError] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val mappingFormat: JsonValueCodec[Mapping] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val mappingListFormat: JsonValueCodec[MappingList] = JsonCodecMaker.makeWithoutDiscriminator
  // implicit val mappingTreeFormat: JsonValueCodec[MappingTree] = JsonCodecMaker.makeWithoutDiscriminator
  // implicit val oscConfigFormat: JsonValueCodec[OSCConfig] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val appConfigFormat: JsonValueCodec[AppConfig] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val appListFormat: JsonValueCodec[AppList] = JsonCodecMaker.makeWithoutDiscriminator
  implicit val format: JsonValueCodec[Message] = JsonCodecMaker.makeWithoutDiscriminator
}
