
package projector

import java.io._
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.InetSocketAddress

import ProjectorCommand._

trait Projector {

  var id = 0
  var name = ""
  var address = ""
  var port = 0
  var status:ProjectorStatus = ProjectorStatus.Disconnected

  var socket:Socket = null
  var in:InputStream = null
  var out:OutputStream = null
  var response:String = ""

  def connect(ip:String):Unit = {
    address = ip
    connect()
  }

  def connect():Unit = {
    if(socket == null || !socket.isConnected() || socket.isClosed()){
      try{
        socket = new Socket();
        socket.setSoTimeout(1000);
        socket.connect(new InetSocketAddress(address, port), 1000);
        out = socket.getOutputStream()
        in = socket.getInputStream()
        status = ProjectorStatus.Connected
        response = ""
      } catch { 
        case e:Exception => 
          status = ProjectorStatus.Unreachable; 
          response = "Connection timed out." //println(s"Connection timed out for $name at $address $port")
      }
    }
  }
  
  def disconnect() = {
    if (socket != null && socket.isConnected()) {
      out.close()
      in.close()
      socket.close()
      status = ProjectorStatus.Disconnected
    }
  }

  def writeString(s:String) = writeBytes(s.getBytes)
  def writeBytes(arr:Array[Byte]) = {
    if (socket != null && socket.isConnected() && !socket.isClosed()) {
      out.write(arr, 0, arr.length)
    }
  }

  

  def readResponse():String
  def readResponse(ack:Char, term:Char):String = {
    if (socket != null && socket.isConnected() && !socket.isClosed()) {
      try{
        var reading = false
        var c = in.read()
        while (c != -1) {
          // println(s"$c $response")
          if (c == ack && !reading) {
            reading = true;
            response = "" //c.toChar.toString
          } else if(reading){
            if(c == term) return response
            response += c.toChar.toString
          }
          c = in.read();
        }
      } catch { 
        case e:SocketTimeoutException => println("Socket timed out in readResponse")
        case e:Exception => println("Exception in readResponse")
      }
    }
    response
  }

  def parseResponse():Unit
  def updateStatus(value:String):Unit
  
  def run(com:ProjectorCommand):Unit

}





class TestProjector extends Projector {
  port = 9999

  override def connect() = {
    response = "connecting.."
    Thread.sleep(1000 + scala.util.Random.nextInt(2000))
    status = ProjectorStatus.Connected
    response = "connected."  
  }
  override def disconnect() = { status = ProjectorStatus.Disconnected; response = "disconnected." }
  override def writeBytes(arr:Array[Byte]):Unit = { }
  override def readResponse(ack:Char, term:Char):String = {
    Thread.sleep(1000 + scala.util.Random.nextInt(2000))
    response = "beep boop!"
    response
  }
  def run(command:ProjectorCommand):Unit = {
    command match {
      case On => status = ProjectorStatus.On
      case Off => status = ProjectorStatus.Off
      case Mute => 
      case Unmute => 
      case GetPowerState => 
      case _ => 
    }
    response = "sent command ${command.getClass.getSimpleName}."
  }

  def readResponse() = readResponse('%','\r')
  def parseResponse():Unit = {}
  def updateStatus(value:String):Unit = {}

}