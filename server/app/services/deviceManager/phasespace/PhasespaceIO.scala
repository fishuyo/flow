

package flow

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import com.fishuyo.seer.spatial.Vec3
import phasespace.Glove

import concurrent.ExecutionContext.Implicits.global

class PhasespaceIO extends IO {

  def state = Phasespace.source
  def markers = state.map(_.markers)
  def marker(id:Int) = state.map(_.markers(id))
  def leftGlove = state.map(_.leftGlove)
  def rightGlove = state.map(_.rightGlove)
  def headPosition = state.map(_.headPosition)

  override def sources:Map[String,Source[Any,akka.NotUsed]] = Map(
    "state" -> state,
    "markers" -> markers,
    "leftGlove" ->  leftGlove,
    "rightGlove" -> rightGlove
  )
}


class PhasespaceState {
  val markers = Array.fill(phasespace.Phasespace.maxMarkerCount)(Vec3())
  val rightGlove = new Glove(0)
  val leftGlove = new Glove(8)
  val headPosition = Vec3()
}

object Phasespace {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  var streamActor:Option[ActorRef] = None
  private val streamSource = Source.actorRef[PhasespaceState](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => streamActor = Some(a) )
  
  // materialize BroadcastHub for dynamic usage as source, which drops previous frame
  val source:Source[PhasespaceState,akka.NotUsed] = streamSource.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
  .watchTermination()((_, f) => {f.onComplete {  // for debugging
    case t => println(s"Phasespace source terminated: $t")
  }; akka.NotUsed })

  
  val state = new PhasespaceState
  var connected = false

  private var scheduled:Option[Cancellable] = None

  connect()
  
  def connect(){
    if( connected ) return
    phasespace.Phasespace.connect("192.168.0.99")
    // phasespace.Phasespace.openPlaybackFile("../phasespaceJVM/core/gloves.txt")
    scheduleUpdate()
    connected = true
  }

  def disconnect(){
    if(!connected) return
    stopUpdate()
    phasespace.Phasespace.disconnect()
    connected = false
  }

  def update(){
    phasespace.Phasespace.update()
    // phasespace.Phasespace.updatePlay()
    phasespace.Phasespace.getMarkers(state.markers)
    state.leftGlove.update(0.002f)
    state.rightGlove.update(0.002f)
    state.headPosition.set(state.markers(17)) //
    
    streamActor.foreach(_ ! state)
  }

  def scheduleUpdate(){
    import concurrent.duration._
    import concurrent.ExecutionContext.Implicits.global
    if(scheduled.isDefined) return
    scheduled = Some( system.scheduler.schedule(2 seconds, 2 millis)(update) )
  }

  def stopUpdate(){
    scheduled.foreach(_.cancel)
    scheduled = None
  }



}