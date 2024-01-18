package flow

import seer.math.Vec2

import collection.mutable.Map
import collection.mutable.ListBuffer

import java.util.Observer
import java.util.Observable

import com.alderstone.multitouch.mac.touchpad.TouchpadObservable
import com.alderstone.multitouch.mac.touchpad.{Finger => TFinger}
import com.alderstone.multitouch.mac.touchpad.FingerState

import org.apache.pekko._
import org.apache.pekko.actor._
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._

import concurrent.ExecutionContext.Implicits.global

case class Finger(id:Int, pos:Vec2, vel:Vec2, size:Float, angle:Float)

class TrackpadState {
  var fingers = ListBuffer[Finger]()
  var pos = Vec2()
  var vel = Vec2()
  var size = 0f
  var angle = 0f

  def count = fingers.length
}

object Trackpad extends Observer {

  implicit val system:ActorSystem = System()
  implicit val materializer:ActorMaterializer = ActorMaterializer()

  var streamActor:Option[ActorRef] = None
  private val streamSource = Source.actorRef[TrackpadState](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => streamActor = Some(a) )
  
  // materialize BroadcastHub for dynamic usage as source, which drops previous frame
  val source:Source[TrackpadState,NotUsed] = streamSource.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
  .watchTermination()((_, f) => {f.onComplete {  // for debugging
    case t => println(s"Trackpad source terminated: $t")
  }; NotUsed })

  
  // val callbacks = new ListBuffer[(TrackpadState)=>Unit]()
  val state = new TrackpadState
  var connected = false

  connect()
  
  def connect():Unit = {
    if( connected ) return
    val tpo = TouchpadObservable.getInstance()
    tpo.addObserver(this)
    connected = true
  }

  def disconnect():Unit = {
    if(!connected) return
    val tpo = TouchpadObservable.getInstance()
    tpo.deleteObserver(this)
    connected = false
  }

  // Touchpad Multitouch update event handler, 
  // called on single MT Finger event
  def update(obj:Observable, arg:Object) = {
    
    val f = arg.asInstanceOf[TFinger]
    
    val x = f.getX()
    val y = f.getY()
    val dx = f.getXVelocity()
    val dy = f.getYVelocity()

    val id:Int = f.getID() 
    val fstate:FingerState = f.getState()
    val fsize = f.getSize()
    val angRad = f.getAngleInRadians()
    val angDeg:Int = f.getAngle() //in Degrees
    val majorAxis = f.getMajorAxis()
    val minorAxis = f.getMinorAxis()

    val frame:Int = f.getFrame()
    val timestamp:Double = f.getTimestamp()

    // val ts = (timestamp * 1000000).toLong
    val finger = Finger(id,Vec2(x,y),Vec2(dx,dy),fsize,angRad)

    var indx = state.fingers.indexWhere( _.id == id )
    // println(fstate + " " + indx)

    if(fstate == FingerState.PRESSED){
      if(indx == -1){
        state.fingers += finger
        indx = state.fingers.length - 1
      }else state.fingers(indx) = finger 
    } else if(fstate == FingerState.RELEASED){
      state.fingers = state.fingers.filterNot( _.id == id )  
    }


    // if(indx < 0) indx = 10 // fix because finger only added when pressed

    // fstate match {
    //   case FingerState.HOVER => status(indx)() = "hover"
    //   case FingerState.TAP => status(indx)() = "tap"
    //   case FingerState.PRESSED => status(indx)() = "pressed"
    //   case FingerState.PRESSING => status(indx)() = "pressing"        
    //   case FingerState.RELEASING => status(indx)() = "releasing"          
    //   case FingerState.RELEASED => status(indx)() = "released"
    //   case FingerState.UNKNOWN =>
    //   case FingerState.UNKNOWN_1 => 
    //   case s => () //println(s + " " + indx)
    // }

    val sumpos = Vec2()
    val sumvel = Vec2()
    var sumsize = 0f
    var sumangle = 0f
    state.fingers.foreach { case f =>
      sumpos += f.pos
      sumvel += f.vel
      sumsize += f.size
      sumangle += angDeg
    }
    if(state.count > 0){
      state.pos = sumpos / state.count
      state.vel = sumvel / state.count
      state.size = sumsize / state.count
      state.angle = sumangle / state.count
      // xy() = state.pos
      // vel() = state.vel
      // size() = state.size
      // angle() = sumangle / state.count
    }

    streamActor.foreach(_ ! state)
    // try{
    //   callbacks.foreach( _(state) )
    // } catch { case e:Exception => println(e) }

  } 

  // def clear() = { 
  //   callbacks.clear()
  // }

  // def bind(f:(TrackpadState)=>Unit) = callbacks += f


}


