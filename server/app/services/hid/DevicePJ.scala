
package flow
package hid

import com.fishuyo.seer.actor._

import spire.math.UByte

import purejavahidapi._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import collection.mutable.HashMap

sealed trait SourceElement { def name:String }
case class Button(name:String, index:Int, mask:Int) extends SourceElement //rename Bitmask
case class ButtonEx(name:String, index:Int, value:Int) extends SourceElement //rename Value
case class Analog(name:String, index:Int) extends SourceElement
case class AnalogSigned(name:String, index:Int) extends SourceElement

sealed trait SinkElement { def name:String }
case class Bitmask(name:String, index:Int, mask:Int ) extends SinkElement
// case class Bit(name:String, index:Int) extends SinkElement
case class BByte(name:String, index:Int) extends SinkElement
case class FFloat(name:String, index:Int) extends SinkElement

sealed trait DeviceType
case object Unknown extends DeviceType 
sealed trait Joystick extends DeviceType 
case object AnalogJoystick extends Joystick
case object DualAnalogJoystick extends Joystick 


object Device {
  val registeredDevices = HashMap[String, (Int) => Device]()
  
  register("PLAYSTATION(R)3 Controller", new PS3Controller(_))
  register("Joy-Con (L)", new JoyconL(_))
  register("Joy-Con (R)", new JoyconR(_))

  def apply(info:HidDeviceInfo, index:Int):Device = apply(info.getProductString, index)
  def apply(name:String, index:Int=0):Device = registeredDevices.getOrElse(name, (i:Int) => new UnknownDevice(name,i))(index)

  // TODO make abstract joystick device? how else can this work?
  // Because it will need to work without knowing which device will be connected, and then become that device
  // maybe some kind of wrapping class that listens for nth connecting joystick, or checks with DM 
  def joystick(index:Int = 0):Device = new PS3Controller(index)

  // mechanism to register and implement devices at runtime..
  // def register(d:Device) = registeredDevices(d.name)
  def register(name:String, construct:(Int) => Device) = registeredDevices(name) = construct
}

abstract class Device(val index:Int) extends IO {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  import concurrent.ExecutionContext.Implicits.global

  
  def productString:String
  val sourceElements:List[SourceElement]
  val sinkElements:List[SinkElement] = List()
  val outputBuffer:Array[Byte] = Array[Byte]()
  val deviceType:DeviceType = Unknown

  // Create a actor Source for device byte stream
  // using a broadcast hub to allow sending bytes to multiple consumers
  var byteStreamActor:Option[ActorRef] = None
  val byteStreamSource = Source.actorRef[Array[Byte]](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => byteStreamActor = Some(a) )
  
  // materialize byteStream BroadcastHub which drops old messages  
  val byteStream: Source[Array[Byte],akka.NotUsed] = byteStreamSource.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
  //.watchTermination()((_, f) => {f.onComplete {  // for debugging
    // case t => println(t)
  // }; akka.NotUsed })

  // option to hold open hiddevice
  var openDevice:Option[HidDevice] = None

  // Materialize stream to handle connection events for device with matching productString and index
  DeviceManager.connectionEvents.filter { case e =>
      e.index == index && 
      e.device.info.getProductString == productString
    }.runForeach {
    case DeviceAttached(dev,idx) => open(dev.info) //open and materialize mapping streams? only if mappings not empty???
    case DeviceDetached(dev,idx) => close() //close and close streams
  }

  openIfConnected()

  def openIfConnected(){  // TODO: Needs to be synchronized with poller I think, maybe move opening logic into DM, also to handle say multiple instances of open device?
    val option = DeviceManager.getDeviceInfo(productString, index)
    option.foreach( open(_) )
  }

  def open(devInfo:HidDeviceInfo){
    // open hid device and set input report listener to forward bytes to byteStreamActor
    if(openDevice.isDefined) return
    // println(s"Device opened: $productString")
    val dev = PureJavaHidApi.openDevice(devInfo)
    dev.setInputReportListener( new InputReportListener(){
      override def onInputReport(source:HidDevice, id:Byte, data:Array[Byte], len:Int){
        byteStreamActor.foreach(_ ! data)
      }
    })
    openDevice = Some(dev)
  }

  def close() = {
    // println(s"Device closed: $productString")
    // openDevice.foreach(_.close) // never returns.. :(
    openDevice = None
  }

  def debugPrint() = byteStream.runForeach(msg => println(msg.mkString(" ")))

  override def sources:Map[String,Source[Float,akka.NotUsed]] = {
    sourceElements.map { 
      case Button(name,i,mask) =>
        name -> byteStream.map { case bytes => 
          if ((UByte(bytes(i)) & UByte(mask)) > UByte(0)) 1.0f else 0.0f
        }.via(destutter)
      case ButtonEx(name,i,value) =>
        name -> byteStream.map { case bytes => 
          if (UByte(bytes(i)) == UByte(value)) 1.0f else 0.0f
        }.via(destutter)
      case Analog(name,i) =>
        name -> byteStream.map { case bytes => 
          UByte(bytes(i)).toFloat / 255
        }.via(destutter)
      case AnalogSigned(name,i) =>
        name -> byteStream.map { case bytes => 
          bytes(i).toFloat / 128
        }.via(destutter)
    }.toMap
  }


  val outputStreamSink:Sink[(SinkElement,Float),akka.NotUsed] = Sink.foreach( (t:(SinkElement,Float)) => {
    val f = t._2
    t._1 match {
      case Bitmask(name, idx, mask) => 
        val b = outputBuffer(idx)
        if(f == 1f) outputBuffer(idx) = (b | mask).toByte
        else outputBuffer(idx) = (b & ~mask).toByte
      case BByte(name, idx) => outputBuffer(idx) = f.toByte
      case FFloat(name, idx) => outputBuffer(idx) = (f*255).toByte
    }
    openDevice.foreach(_.setOutputReport(0, outputBuffer, outputBuffer.length))
  }).mapMaterializedValue{ case _ => akka.NotUsed}

  val outputStream:Sink[(SinkElement,Float), akka.NotUsed] = MergeHub.source[(SinkElement,Float)].to(outputStreamSink).run()

  override def sinks:Map[String,Sink[Float,akka.NotUsed]] = {
    sinkElements.map { case e =>
      e.name -> Flow[Float].map((e,_)).to(outputStream)
    }.toMap
  }

}



