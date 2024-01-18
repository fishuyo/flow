
package flow
package hid

// import seer.actor._

import spire.math.UByte

import purejavahidapi._

import org.apache.pekko._
import org.apache.pekko.actor._
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._
import concurrent.ExecutionContext.Implicits.global

import collection.mutable.HashMap

/**
  * HidDeviceConnection wraps an HidDevice connection
  * and provides byte streams to and from device
  */
class HidDeviceConnection(val name:Option[String], val deviceType:DeviceType, val index:Int){
  
  implicit val system:ActorSystem = System()
  implicit val materializer:ActorMaterializer = ActorMaterializer()

  var openDevice:Option[HidDevice] = None

  val kill = KillSwitches.shared("HidDeviceConnection") // ???

  // Create a actor Source for device byte stream
  // using a broadcast hub to allow sending bytes to multiple consumers
  var byteStreamActor:Option[ActorRef] = None
  private val byteStreamSource = Source.actorRef[Array[Byte]](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => byteStreamActor = Some(a) )
  
  // materialize BroadcastHub for dynamic usage as source, which drops previous frame
  val source:Source[Array[Byte],NotUsed] = byteStreamSource.via(kill.flow).toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
  .watchTermination()((_, f) => {f.onComplete {  // for debugging
    case t => println(s"Device source terminated: $name $index: $t")
  }; NotUsed })

  // sink sends bytes to open HidDevice
  private val byteStreamSink:Sink[Array[Byte],NotUsed] = Sink.foreach( (bytes:Array[Byte]) => {
    openDevice.foreach(_.setOutputReport(0, bytes, bytes.length))
  }).mapMaterializedValue{ case _ => NotUsed}

  // materialize MergeHub for dynamic usage as sink
  val sink:Sink[Array[Byte],NotUsed] = MergeHub.source[Array[Byte]].via(kill.flow).to(byteStreamSink).run()

  // Materialize stream to handle connection events for device with matching name and index
  // TODO only listen when requested???
  // DeviceManager.connectionEvents.via(kill.flow).filter { case e =>
  //     e.index == index && 
  //     e.device.info.getProductString == name
  //   }.runForeach {
  //   case DeviceAttached(dev,idx) => open() //open and materialize mapping streams? only if mappings not empty???
  //   case DeviceDetached(dev,idx) => close() //close and close streams
  // }

  //open()

  // def open(){
    // open hid device and set input report listener to forward bytes to byteStreamActor
    // if(openDevice.isDefined) return
    // println(s"Device opened: $name")
    // DeviceManager.openDeviceConnection(this)
    // val dev = PureJavaHidApi.openDevice(devInfo)
    // dev.setInputReportListener( new InputReportListener(){
    //   override def onInputReport(source:HidDevice, id:Byte, data:Array[Byte], len:Int){
    //     byteStreamActor.foreach(_ ! data)
    //   }
    // })
    // openDevice = Some(dev)
  // }

  // def close() = {
    // DeviceManager.closeDeviceConnection(this)
    // println(s"Device closed: $name")
    // openDevice.foreach(_.close) // never returns.. :(
    // kill.shutdown
    // openDevice = None
  // }

  // def debugPrint(count:Int=1024) = byteStream.runForeach(msg => println(msg.take(count).mkString(" ")))


}



