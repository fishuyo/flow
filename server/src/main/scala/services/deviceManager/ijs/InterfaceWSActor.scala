// package flow
// package ijs

// // import julienrf.json.derived._
// // import play.api.libs.json._
// // import play.api.libs.functional.syntax._

// // import play.api.Logger

// import akka.actor._
// import akka.stream._
// import akka.stream.scaladsl._

// object InterfaceWSActor {

//   case class Msg(`type`:String, address:String, typetags:String, parameters:Seq[JsValue])
//   implicit val msgFormat = oformat[Msg]()

//   def props(out:ActorRef, name:String, request:String) = Props(new InterfaceWSActor(out,name,request))
// }

// class InterfaceWSActor(out:ActorRef, name:String, request:String) extends Actor with ActorLogging {

//   import InterfaceWSActor._

//   val io = Interface(name)
//   io.sinkActors += self  //TODO remove on ws close

//   val index = io.sinkActors.size - 1
//   io.sync(index)

//   def receive = {
//     case msg:String if msg == "keepalive" => ()
//     case msg:String => 
//       // println(msg)
//       val message = Json.parse(msg).as[Msg]
//       message match {
//         case Msg("osc", addr, tt, params) => 
//           // println(s"OSC $addr $tt $params")
//           val vs = params.zip(tt).map { 
//             case (p,'f') => p.as[Float]
//             case (p,'i') => p.as[Int]
//             case (p,'s') => p.as[String]
//             case (p,t) => println(s"InterfaceWSActor: Unhandled type $t")
//           }
//           if(vs.length == 1) io.sourceActor.foreach(_ ! (addr.tail, vs.head))
//           else io.sourceActor.foreach(_ ! (addr.tail, vs))
//         case m => println(m)
//       }

//     case (name:String, value:Float) => 
//       out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
//     case (name:String, value:Double) => 
//       out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
//     case (name:String, value:Int) => 
//       out ! Json.toJson(Msg("osc", "/"+name, "f", Seq(JsNumber(value)))).toString
//     case (name:String, value:Seq[Float]) => 
//       out ! Json.toJson(Msg("osc", "/"+name, "f"*value.length, value.map(JsNumber(_)))).toString
//     case (name:String, value:(Float,Float)) =>
//       out ! Json.toJson(Msg("osc", "/"+name, "ff", Seq(JsNumber(value._1), JsNumber(value._2)))).toString
//     case (name:String, value:String) =>
//       out ! Json.toJson(Msg("osc", "/"+name, "s", Seq(JsString(value)))).toString
//     case m => println(s"InterfaceWSActor unhandled msg: $m")
//   }
// }


