
package flow

import org.apache.pekko._
import org.apache.pekko.actor._
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._

import de.sciss.osc.Message

import collection.mutable.HashMap

class OSCSink extends OSCSend with IO {
  var prefix = ""
  override def sink(name:String) = Some(Sink.foreach(send(prefix + "/" + name, _:Any)).mapMaterializedValue(x => NotUsed))
  def sink = Sink.foreach(send(_:Message))
}


class OSCSource extends IO {
  var prefix = ""
  val sourceActors = HashMap[String,ActorRef]()
  override def source(name:String) = Some(
    Source.actorRef[Any](bufferSize = 0, OverflowStrategy.fail)
      .mapMaterializedValue( (a:ActorRef) => { sourceActors("/"+name) = a; NotUsed } )
  )
  
  val handler:OSC.OSCHandler = {
    case (Message(name:String, value:Any), addr) => 
      sourceActors.get(name).foreach(_ ! value)
    case (Message(name:String), addr) => 
      sourceActors.get(name).foreach(_ ! None)
    case msg => println(s"Unhandled msg in OSCIO: $msg")  
  }

  def listen(port:Int) = {
    OSCManager() ! OSCManagerActor.Unbind(port, handler) //XXX find better way to make this work
    OSCManager() ! OSCManagerActor.Bind(port, handler)
  }
}