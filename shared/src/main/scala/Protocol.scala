
package flow
package protocol

// import julienrf.json.derived
// import com.github.plokhotnyuk.jsoniter_scala.macros._
// import com.github.plokhotnyuk.jsoniter_scala.core._

import upickle.default.ReadWriter

sealed trait Message derives ReadWriter

final case class ClientHandshake(msg:String="hi") extends Message

final case class IOPort(name:String, `type`:String) extends Message
final case class IOConfig(name:String, sources:Seq[IOPort], sinks:Seq[IOPort]) extends Message

final case class Device(io:IOConfig, count:Int) extends Message
final case class DeviceList(devices:Seq[Device]) extends Message

// final case class OSCConfig(address:String, sinkPort:Int) extends Message
final case class AppConfig(io:IOConfig, defaultMappings:Seq[String]=Seq()) extends Message
final case class AppList(apps:Seq[AppConfig]) extends Message

final case class MappingError(line:Int, message:String) extends Message
final case class Mapping(name:String, code:String, modified:Boolean=false, running:Boolean=false, errors:Seq[MappingError]=Seq()) extends Message
final case class MappingList(mappings:Seq[Mapping]) extends Message
// final case class MappingTree(name:String, mappings:Seq[Mapping], trees:Seq[MappingTree]) extends Message

final case class Run(mapping:Mapping) extends Message
final case class Stop(mapping:Mapping) extends Message
final case class Save(mapping:Mapping) extends Message
final case object StopAll extends Message

final case class Success(text:String) extends Message
final case class Error(text:String) extends Message
final case class Info(text:String) extends Message

final case class WatchParameter(name:String, device:String) extends Message
final case class Parameter(name:String, device:String, value:Float) extends Message

// object Message {
//   implicit val handshake: JsonValueCodec[ClientHandshake] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
  
//   implicit val ioportFormat: JsonValueCodec[IOPort] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val ioconfigFormat: JsonValueCodec[IOConfig] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))

//   implicit val deviceFormat: JsonValueCodec[Device] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val deviceListFormat: JsonValueCodec[DeviceList] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
  
//   implicit val mappingErrorFormat: JsonValueCodec[MappingError] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val mappingFormat: JsonValueCodec[Mapping] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val mappingListFormat: JsonValueCodec[MappingList] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//     // implicit val mappingTreeFormat: JsonValueCodec[MappingTree] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//     // implicit val oscConfigFormat: JsonValueCodec[OSCConfig] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val appConfigFormat: JsonValueCodec[AppConfig] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val appListFormat: JsonValueCodec[AppList] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   implicit val format: JsonValueCodec[Message] = JsonCodecMaker.make(CodecMakerConfig.withDiscriminatorFieldName(Some("dtype")))
//   // implicit val format: JsonValueCodec[Message] = JsonCodecMaker.makeWithoutDiscriminator
// }
