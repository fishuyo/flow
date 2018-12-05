

package flow

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import com.fishuyo.seer.spatial.Vec3

import concurrent.ExecutionContext.Implicits.global

import java.nio.ByteBuffer
import java.nio.IntBuffer

import org.lwjgl.openvr._

// class OpenVRIO extends IO {

//   def state = OpenVR.source

//   override def sources:Map[String,Source[Any,akka.NotUsed]] = Map(
//     "state" -> state
//   )
// }


// class PhasespaceState {
//   val markers = Array.fill(phasespace.Phasespace.maxMarkerCount)(Vec3())
//   val rightGlove = new Glove(0)
//   val leftGlove = new Glove(8)
//   val headPosition = Vec3()
// }

object OpenVR {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  // var streamActor:Option[ActorRef] = None
  // private val streamSource = Source.actorRef[PhasespaceState](bufferSize = 0, OverflowStrategy.fail)
                                    // .mapMaterializedValue( (a:ActorRef) => streamActor = Some(a) )
  
  // materialize BroadcastHub for dynamic usage as source, which drops previous frame
  // val source:Source[PhasespaceState,akka.NotUsed] = streamSource.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
  // .watchTermination()((_, f) => {f.onComplete {  // for debugging
    // case t => println(s"Phasespace source terminated: $t")
  // }; akka.NotUsed })

  
  // val state = new PhasespaceState
  // var connected = false

  // private var scheduled:Option[Cancellable] = None

  // connect()
  
  def connect(){
    val error = ByteBuffer.allocateDirect(4).asIntBuffer
    val token = VR.VR_InitInternal(error, VR.EVRApplicationType_VRApplication_Other)
    checkInitError(error)    
    org.lwjgl.openvr.OpenVR.create(token)
    
    // VR.VR_GetGenericInterface(VR.IVRCompositor_Version, error)
    // checkInitError(error)
    
    // VR.VR_GetGenericInterface(VR.IVRRenderModels_Version, error)
    // checkInitError(error)
  }

  def disconnect(){}

  def checkInitError(errorBuffer:IntBuffer) {
    if (errorBuffer.get(0) != VR.EVRInitError_VRInitError_None) {
      val error = errorBuffer.get(0)
      println("VR Initialization error: " + VR.VR_GetVRInitErrorAsEnglishDescription(error))
    }

  }

}