

package flow

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import com.fishuyo.seer.spatial.Vec3
import com.fishuyo.seer.spatial.Mat4

import concurrent.ExecutionContext.Implicits.global

import java.nio.ByteBuffer
import java.nio.IntBuffer

import org.lwjgl.openvr._

class OpenVRIO extends IO {

  OpenVR.connect
  def state = OpenVR.source
  def active = state.map(_.devices.filter(_.isValid == true))
  def hmd = state.map(_.devices.filter(_.isValid == true).filter(_.deviceType == VRDeviceType.HMD))
  def controllers = state.map(_.devices.filter(_.isValid == true).filter(_.deviceType == VRDeviceType.Controller))
  def trackers = state.map(_.devices.filter(_.isValid == true).filter(_.deviceType == VRDeviceType.Generic))


  override def sources:Map[String,Source[Any,akka.NotUsed]] = Map(
    "state" -> state
  )
}

object VRDeviceType extends Enumeration {
  val HMD, Controller, BaseStation, Generic, Unknown = Value
}
object VRDeviceRole extends Enumeration {
  val Head, LeftHand, RightHand, Unknown = Value
}

class VRDevice(val index:Int) {
  val pose = Pose()
  val mat = Mat4()
  val vel = Vec3()
  val angVel = Vec3()
  var isConnected = false
  var isValid = false
  var deviceType = VRDeviceType.Unknown
  var deviceRole = VRDeviceRole.Unknown
  var buttons:Long = 0

  def getButton(b:Int) = (buttons & (1l << b)) != 0
  
  def setButton(b:Int, down:Boolean){
    if(down) buttons |= (1l << b)
    else buttons ^= (1l << b)
  }
}

class OpenVRState {
  val devices = (0 until VR.k_unMaxTrackedDeviceCount).map(new VRDevice(_)).toArray
}

object OpenVR {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()
  
  private var streamActor:Option[ActorRef] = None
  private val streamSource = Source.actorRef[OpenVRState](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => streamActor = Some(a) )
  
  // materialize BroadcastHub for dynamic usage as source, which drops previous frame
  val source:Source[OpenVRState,akka.NotUsed] = streamSource.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
  .watchTermination()((_, f) => {f.onComplete {  // for debugging
    case t => println(s"OpenVR source terminated: $t")
  }; akka.NotUsed })

  
  val state = new OpenVRState
  var connected = false

  private var scheduled:Option[Cancellable] = None

  private val devicePoses = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount)
  private val deviceGamePoses = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount)
  private val event = VREvent.create()

  // connect()
  
  def connect(){
    if(connected) return
    val error = ByteBuffer.allocateDirect(4).asIntBuffer
    val token = VR.VR_InitInternal(error, VR.EVRApplicationType_VRApplication_Overlay)
    checkInitError(error)    
    org.lwjgl.openvr.OpenVR.create(token)
    
    // VR.VR_GetGenericInterface(VR.IVRCompositor_Version, error)
    // checkInitError(error)
    
    // VR.VR_GetGenericInterface(VR.IVRRenderModels_Version, error)
    // checkInitError(error)
    startUpdate()
    connected = true
  }

  def disconnect(){
    if(!connected) return
    stopUpdate()
    VR.VR_ShutdownInternal()
    connected = false
  }

  def startUpdate(){
    import concurrent.duration._
    import concurrent.ExecutionContext.Implicits.global
    if(scheduled.isDefined) return
    scheduled = Some(system.scheduler.schedule(0 seconds, 15 millis)(pollEvents))
  }

  def stopUpdate(){
    scheduled.foreach(_.cancel)
    scheduled = None
  }

  def pollEvents(){

    // VRCompositor.VRCompositor_WaitGetPoses(devicePoses, deviceGamePoses)
    VRSystem.VRSystem_GetDeviceToAbsoluteTrackingPose(VR.ETrackingUniverseOrigin_TrackingUniverseStanding, 0f, devicePoses)

    for(id <- 0 until VR.k_unMaxTrackedDeviceCount){
      val pose = devicePoses.get(id)
      val device = state.devices(id)
      hmdMat34ToMatrix4(pose.mDeviceToAbsoluteTracking(), device.mat)
      device.pose.pos.set(device.mat(12),device.mat(13),device.mat(14))
      device.pose.quat.fromMatrix(device.mat)
      device.vel.set(pose.vVelocity().v(0), pose.vVelocity().v(1), pose.vVelocity().v(2))
      device.angVel.set(pose.vAngularVelocity().v(0), pose.vAngularVelocity().v(1), pose.vAngularVelocity().v(2))
      device.isConnected = pose.bDeviceIsConnected()
      device.isValid = pose.bPoseIsValid()
      if(device.isConnected && device.deviceType == VRDeviceType.Unknown) updateDevice(id)
      if(device.isValid) println(device.vel)
    }

    while (VRSystem.VRSystem_PollNextEvent(event)) {
      val index = event.trackedDeviceIndex()
      // if (index < 0 || index > VR.k_unMaxTrackedDeviceCount) continue;                  
      var button:Int = 0
      
      event.eventType() match {
        case VR.EVREventType_VREvent_TrackedDeviceActivated =>            
          updateDevice(index)            
        case VR.EVREventType_VREvent_TrackedDeviceDeactivated =>
          state.devices(index).isConnected = false
        case VR.EVREventType_VREvent_ButtonPress =>
          button = event.data().controller().button()
          state.devices(index).setButton(button, true)
        case VR.EVREventType_VREvent_ButtonUnpress =>           
          button = event.data().controller().button()
          state.devices(index).setButton(button, false)
        case _ => ()
      }
    }

    streamActor.foreach(_ ! state)
  }

  def updateDevice(index:Int){
    val dc = VRSystem.VRSystem_GetTrackedDeviceClass(index)
    val d = state.devices(index)
    dc match {
      case VR.ETrackedDeviceClass_TrackedDeviceClass_HMD => d.deviceType = VRDeviceType.HMD
      case VR.ETrackedDeviceClass_TrackedDeviceClass_Controller => d.deviceType = VRDeviceType.Controller
      case VR.ETrackedDeviceClass_TrackedDeviceClass_TrackingReference => d.deviceType = VRDeviceType.BaseStation
      case VR.ETrackedDeviceClass_TrackedDeviceClass_GenericTracker => d.deviceType = VRDeviceType.Generic
      case _ => d.deviceType = VRDeviceType.Unknown
    }
  }



  def hmdMat34ToMatrix4(hmd:HmdMatrix34, mat:Mat4){
    val m = hmd.m();
    
    mat(0) = m.get(0);
    mat(1) = m.get(4);
    mat(2) = m.get(8);
    mat(3) = 0;
    
    mat(4) = m.get(1);
    mat(5) = m.get(5);
    mat(6) = m.get(9);
    mat(7) = 0;
    
    mat(8) = m.get(2);
    mat(9) = m.get(6);
    mat(10) = m.get(10);
    mat(11) = 0;
    
    mat(12) = m.get(3);
    mat(13) = m.get(7);
    mat(14) = m.get(11);
    mat(15) = 1;
  }

  def checkInitError(errorBuffer:IntBuffer) {
    if (errorBuffer.get(0) != VR.EVRInitError_VRInitError_None) {
      val error = errorBuffer.get(0)
      println("VR Initialization error: " + VR.VR_GetVRInitErrorAsEnglishDescription(error))
    }

  }



}


