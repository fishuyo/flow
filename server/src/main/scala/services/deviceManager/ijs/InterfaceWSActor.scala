package flow
package ijs

import org.apache.pekko.actor._
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._
import org.apache.pekko.http.scaladsl.model.ws._

// import seer.osc._
import de.sciss.osc.Message

import upickle.default.{ReadWriter, macroRW}
import upickle.default.ReadWriter.join

// type Value = Float | String
case class Value(val f: Option[Float]=None, val s: Option[String]=None)
object Value {
  implicit val rw: ReadWriter[Value] = upickle.default.readwriter[String].bimap[Value](
    x => 
      if(x.f.isDefined) s"${x.f}"
      // else if(x.i.isDefined) s"${x.i}"
      else s"${x.s}",
    str => {
      var v:Option[Value] = None
      if(!v.isDefined){
        try{ 
          val f = str.toFloat
          v = Some(Value(Some(f), None))
        } catch {
          case e:Exception => 
        }
      }
      if(!v.isDefined){
        v = Some(Value(None, Some(str)))
      } 
      v.get
    }
  )
}

case class IjsOscMessage(`type`:String, address:String, typetags:String, parameters:Seq[Value])
object IjsOscMessage{
  implicit val rw: ReadWriter[IjsOscMessage] = macroRW
}

object InterfaceWSActor {
  def props(out:ActorRef, name:String="test", request:String="") = Props(new InterfaceWSActor(out,name,request))
}

class InterfaceWSActor(out:ActorRef, name:String="test", request:String="") extends Actor with ActorLogging {

  import InterfaceWSActor._

  val io = Interface(name)
  io.sinkActors += self  //TODO remove on ws close

  val index = io.sinkActors.size - 1
  io.sync(index)


  def receive = {
    case TextMessage.Strict(input) if input == "keepalive" => 
      // println("keep alive received.")
    
    case TextMessage.Strict(input) => 
      // println(s"$input")
      val message = upickle.default.read[IjsOscMessage](input)
      message match {
        case IjsOscMessage("osc", addr, tt, params) => 
          println(s"OSC $addr $tt $params")
          val vs = params.zip(tt).map { 
            case (p,'f') => p.f.get
            // case (p,'i') => p.i.get
            case (p,'s') =>
              var s = "" 
              if(p.s.isDefined) s = p.s.get
              // else if(p.i.isDefined) s = p.i.get.toString
              else if(p.f.isDefined) s = p.f.get.toString
              s
            case (p,t) => println(s"InterfaceWebsocketActor: Unhandled type ($p,$t)")
          }
          if(vs.length == 1) io.sourceActor.foreach(_ ! (addr.tail, vs.head))
          else io.sourceActor.foreach(_ ! (addr.tail, vs))
      }
      

    // case (name:String, value:Float) => 
    //   out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
    // case (name:String, value:Double) => 
    //   out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
    // case (name:String, value:Int) => 
    //   out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
    // case (name:String, value:Seq[Float]) => 
    //   out ! Json.toJson(Msg("osc", "/"+name, "f"*value.length, value.map(JsNumber(_)))).toString
    // case (name:String, value:(Float,Float)) =>
    //   out ! Json.toJson(Msg("osc", "/"+name, "ff", Seq(JsNumber(value._1), JsNumber(value._2)))).toString
    // case (name:String, value:String) =>
    //   out ! Json.toJson(Msg("osc", "/"+name, "s", Seq(JsString(value)))).toString

    case m => println(s"InterfaceWebsocketActor unhandled msg: $m")
  }

}


