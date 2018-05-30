package flow
package ijs

import julienrf.json.derived._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.api.Logger

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

object InterfaceWSActor {

  type ¬[A] = A => Nothing
  type ∨[T, U] = ¬[¬[T] with ¬[U]]
  type ¬¬[A] = ¬[¬[A]]
  type |∨|[T, U] = { type λ[X] = ¬¬[X] <:< (T ∨ U) }
  // def size[T : (Int |∨| String)#λ](t : T) = t match {
  //   case i : Int => i
  //   case s : String => s.length
  // }

  // sealed trait Msg
  case class Msg(`type`:String, address:String, typetags:String, parameters:Seq[JsValue]) //extends Msg
  // case class SMsg(`type`:String, address:String, typetags:String, parameters:Seq[String]) extends Msg
  implicit val msgFormat = oformat[Msg]()

  def props(out:ActorRef, name:String, request:String) = Props(new InterfaceWSActor(out,name,request))
}

class InterfaceWSActor(out:ActorRef, name:String, request:String) extends Actor with ActorLogging {

  import InterfaceWSActor._

  val io = Interface(name)
  io.sinkActors += self

  def receive = {
    case msg:String if msg == "keepalive" => ()
    case msg:String => 
      // println(msg)
      val message = Json.parse(msg).as[Msg]
      message match {
        case Msg("osc", addr, tt, params) => 
          // println(s"OSC $addr $tt $params")
          val vs = params.zip(tt).map { 
            case (p,'f') => p.as[Float]
            case (p,'i') => p.as[Int]
            case (p,'s') => p.as[String]
            case (p,t) => println(s"InterfaceWSActor: Unhandled type $t")
          }
          if(vs.length == 1) io.sourceActor.foreach(_ ! (addr.tail, vs.head))
          else io.sourceActor.foreach(_ ! (addr.tail, vs))
        case m => println(m)
      }

    case (name:String, value:Float) => 
      out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
    case (name:String, value:Int) => 
      out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
    case (name:String, value:Seq[Float]) => 
      out ! Json.toJson(Msg("osc", "/"+name, "f"*value.length, value.map(JsNumber(_)))).toString
    case (name:String, value:(Float,Float)) =>
      out ! Json.toJson(Msg("osc", "/"+name, "ff", Seq(JsNumber(value._1), JsNumber(value._2)))).toString
    case m => println(s"InterfaceWSActor unhandled msg: $m")
  }
}
// class InterfaceOSCActor(out:ActorRef, config:OSCConfig, request:String) extends Actor with ActorLogging {
//   import InterfaceOSCActor._

//   val manager = OSCManager()

//   val oscSend = new OSCSend
//   oscSend.connect(config.address, config.inPort)
  
//   println(s"Sending to ${config.address} on ${config.inPort}")
//   val logger = Logger(this.getClass())

//   val handler:PartialFunction[Message,Unit] = {
//     case Message(address, f:Float) => 
//       println(s"to ui: $address $f")
//       out ! Json.stringify(Json.toJson( Msg("osc", address, Seq(f))))
//     case Message(address, s:String) => 
//       println(s"to ui: $address $s")
//       out ! Json.stringify(Json.toJson( StrMsg("osc", address, Seq(s))))
//     case Message(address, f:Int) => 
//       println(s"to ui: $address $f")
//       out ! Json.stringify(Json.toJson( Msg("osc", address, Seq(f.toFloat))))
//     case msg => println(msg)
//   }
//   manager ! Bind(config.outPort, handler)
//   // oscRecv.bind(handler)

//   def receive = {
//     case msg: String =>
//       try {
//         val json = Json.parse(msg)
//         println(json)
//         val address = (json \ "address").as[String]
//         val jsvalue = (json \ "parameters")(0)
//         val option1 = jsvalue.asOpt[Float]
//         var value:Any = "ERROR"
//         option1 match {
//           case Some(v) => value = v
//           case None => 
//             val option2 = jsvalue.asOpt[String]
//             option2 match {
//               case Some(s) => value = s
//               case None => ()
//             }
//         }
//        logger.info(request+ " " +address+ " " + value) 
//        oscSend.send(address, value)
//       } catch { case e:Exception => println(e) }
//   }

//   override def postStop() = {
//     super.postStop()
//     try{ oscSend.disconnect } catch { case e:Exception => println(e) }
//     // try{ oscRecv.unbind(handler) } catch { case e:Exception => println(e) }
//     manager ! Unbind(config.outPort,handler)
//   }
// }
