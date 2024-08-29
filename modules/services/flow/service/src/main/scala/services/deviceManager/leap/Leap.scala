// package flow
// package leap

// import seer.math._

// import collection.mutable.ListBuffer

// import com.leapmotion.leap._

// import org.apache.pekko._
// import org.apache.pekko.actor._
// import org.apache.pekko.stream._
// import org.apache.pekko.stream.scaladsl._

// import concurrent.ExecutionContext.Implicits.global

// object Leap extends Listener {

//   implicit val system:ActorSystem = System()
//   implicit val materializer:ActorMaterializer = ActorMaterializer()

//   var streamActor:Option[ActorRef] = None
//   private val streamSource = Source.actorRef[Frame](bufferSize = 0, OverflowStrategy.fail)
//                                     .mapMaterializedValue( (a:ActorRef) => streamActor = Some(a) )
  
//   // materialize BroadcastHub for dynamic usage as source, which drops previous frame
//   val source:Source[Frame,NotUsed] = streamSource.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
//   .watchTermination()((_, f) => {f.onComplete {  // for debugging
//     case t => println(s"Leap source terminated: $t")
//   }; NotUsed })

//   var controller:Controller  = _
//   var connected = false

//   connect()

//   def connect():Unit = {
//     if( connected ) return
//     controller = new Controller()
//     controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES)
//     controller.addListener(this)
//     connected = true
//   }
//   def disconnect():Unit = {
//     if(!connected) return
//     controller.removeListener(this)
//     connected = false
//   }

//   override def onInit(c:Controller) = { println("Leap Init..") }
//   override def onConnect(c:Controller) = { println(s"Leap Connected. has focus: ${c.hasFocus}") }
//   override def onDisconnect(c:Controller) = { println("Leap Disconnecting..") }
//   override def onExit(c:Controller) = { println("Leap Done.") }

//   override def onFrame( c:Controller ) = {        
//     val frame = c.frame();
//     streamActor.foreach(_ ! frame)
//   } 

// }