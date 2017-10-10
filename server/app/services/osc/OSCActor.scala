// package flow

// import play.api.Logger
// import play.api.libs.json._
// import akka.actor._

// import de.sciss.osc.Message

// import collection.mutable.HashMap


// object OSCActor {
//   // case class used to represent OSC messages over json
//   case class Msg(`type`:String, address:String, parameters:Seq[Float])
//   case class StrMsg(`type`:String, address:String, parameters:Seq[String])
//   implicit val statFormat = Json.format[Msg]
//   implicit val statStrFormat = Json.format[StrMsg]

//   //
//   // Map to hold and reuse open OSC receivers 
//   // val receivers = HashMap[Int,OSCRecv]()

//   def props(out:ActorRef, manager:ActorRef, config:OSCConfig, request:String) = Props(new OSCActor(out,manager,config, request))
// }

// class OSCActor(out:ActorRef, manager:ActorRef, config:OSCConfig, request:String) extends Actor with ActorLogging {
//   import OSCActor._

//   // XXX race condition on first update.. which is unlikely and mostly harmless..

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
