
package flow
package hid

import com.fishuyo.seer.actor._

import spire.math.UByte

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import collection.mutable.HashMap

/**
  * SourceElement trait for case classes representing 
  * input elements/types of an hid device
  */
sealed trait SourceElement { def name:String }
case class Button(name:String, index:Int, mask:Int) extends SourceElement //rename Bitmask
case class ButtonEx(name:String, index:Int, value:Int) extends SourceElement //rename Value
case class Analog(name:String, index:Int) extends SourceElement
case class AnalogSigned(name:String, index:Int) extends SourceElement
// ButtonBitMask(index = 0, mask = 0xFF)
// ButtonEquals(index = 1, value = 0x128)
// RawByteValue(index = 1)
// Analog
// case class MapFunction[T](index:Int, func: Byte=>T )


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

abstract class DeviceIO(val name:String, val index:Int) extends IO {

  import concurrent.ExecutionContext.Implicits.global
  
  val device:HidDeviceConnection = DeviceManager.getDeviceConnection(name,index)
  // def productString:String
  val sourceElements:List[SourceElement]
  val sinkElements:List[SinkElement] = List()
  val outputBuffer:Array[Byte] = Array[Byte]()
  val deviceType:DeviceType = Unknown

  val kill = KillSwitches.shared("device")

  def open() = {}
  def close() = {
    // println(s"Device closed: $productString")
    // openDevice.foreach(_.close) // never returns.. :(
    kill.shutdown
    // openDevice = None
  }

  // def debugPrint(count:Int=1024) = byteStream.runForeach(msg => println(msg.take(count).mkString(" ")))

  override def sources:Map[String,Source[Float,akka.NotUsed]] = {
    sourceElements.map { 
      case Button(name,i,mask) =>
        name -> device.source.map { case bytes => 
          if ((UByte(bytes(i)) & UByte(mask)) > UByte(0)) 1.0f else 0.0f
        }.via(destutter)
      case ButtonEx(name,i,value) =>
        name -> device.source.map { case bytes => 
          if (UByte(bytes(i)) == UByte(value)) 1.0f else 0.0f
        }.via(destutter)
      case Analog(name,i) =>
        name -> device.source.map { case bytes => 
          UByte(bytes(i)).toFloat / 255
        }.via(destutter)
      case AnalogSigned(name,i) =>
        name -> device.source.map { case bytes => 
          bytes(i).toFloat / 128
        }.via(destutter)
    }.toMap
  }


  // val outputStreamSink:Sink[(SinkElement,Float),akka.NotUsed] = Sink.foreach( (t:(SinkElement,Float)) => {
  //   val f = t._2
  //   t._1 match {
  //     case Bitmask(name, idx, mask) => 
  //       val b = outputBuffer(idx)
  //       if(f == 1f) outputBuffer(idx) = (b | mask).toByte
  //       else outputBuffer(idx) = (b & ~mask).toByte
  //     case BByte(name, idx) => outputBuffer(idx) = f.toByte
  //     case FFloat(name, idx) => outputBuffer(idx) = (f*255).toByte
  //   }
  //   openDevice.foreach(_.setOutputReport(0, outputBuffer, outputBuffer.length))
  // }).mapMaterializedValue{ case _ => akka.NotUsed}

  // val outputStream:Sink[(SinkElement,Float), akka.NotUsed] = MergeHub.source[(SinkElement,Float)].via(kill.flow).to(outputStreamSink).run()

  override def sinks:Map[String,Sink[Float,akka.NotUsed]] = {
    sinkElements.map { case e =>
      e.name -> Flow[Float].map((f:Float) => { //(e,_)).to(outputStream)
        e match {
          case Bitmask(name, idx, mask) => 
            val b = outputBuffer(idx)
            if(f == 1f) outputBuffer(idx) = (b | mask).toByte
            else outputBuffer(idx) = (b & ~mask).toByte
          case BByte(name, idx) => outputBuffer(idx) = f.toByte
          case FFloat(name, idx) => outputBuffer(idx) = (f*255).toByte
        }
        outputBuffer
      }).to(device.sink)
    }.toMap
  }

}



