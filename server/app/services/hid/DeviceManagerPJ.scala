
package flow
package hid

import purejavahidapi._

import collection.JavaConverters._
import collection.mutable.ListBuffer
import collection.mutable.HashMap

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

/** Wrapper for device polling, override equality checking of HidDeviceInfo */
class MyHidDeviceInfo(val info:HidDeviceInfo) {
  override def equals(other:Any) = other match {
    case b:MyHidDeviceInfo =>
      (info.getProductId == b.info.getProductId && info.getVendorId == b.info.getVendorId && info.getSerialNumberString == b.info.getSerialNumberString)
    case _ => false
  }
  override def hashCode = {
    val s = info.getSerialNumberString
    41*(41+info.getProductId) + info.getVendorId + (if(s == null) 0 else s.hashCode)
  }
}


/** DeviceConnectionEvent case classes **/
sealed trait DeviceConnectionEvent { def device:MyHidDeviceInfo; def index:Int }
case class DeviceAttached(device:MyHidDeviceInfo, index:Int) extends DeviceConnectionEvent
case class DeviceDetached(device:MyHidDeviceInfo, index:Int) extends DeviceConnectionEvent


/** Device Manager handles connection events of HidDevices */
object DeviceManager {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  // map of connected devices keyed on product string
  private val connectedDevices = HashMap[String,ListBuffer[MyHidDeviceInfo]]()
  
  // create a stream actor for connection events
  // using a broadcast hub to allow sending connection events to multiple consumers
  private var eventStreamActor = None:Option[ActorRef]
  private val connectionEventSource = Source.actorRef[DeviceConnectionEvent](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => eventStreamActor = Some(a) )
  val connectionEvents = connectionEventSource.toMat(BroadcastHub.sink)(Keep.right).run() 
  connectionEvents.runWith(Sink.ignore)  // default consumer keeps stream running

  def getDevices() = {
    this.synchronized {
      connectedDevices.values
    }
  }

  def getRegisteredDevices() = {
    this.synchronized {
      connectedDevices.filter { case (k,v) => Device.registeredDevices.contains(k) }.values
    }
  }


  def getDeviceInfo(name:String, index:Int=0) = {
    this.synchronized {
      val ds = connectedDevices.getOrElseUpdate(name, ListBuffer[MyHidDeviceInfo]())
      if(index < ds.length){
        val di = ds(index)
        Some(di.info)
      } else None
    }
  }

  private def getAttachedDevices():List[HidDeviceInfo] = PureJavaHidApi.enumerateDevices.asScala.toList
  private def getAttachedDevicesByName(product:String):List[HidDeviceInfo] = getAttachedDevices.filter( _.getProductString == product )
  private def getAttachedDevicesByManufacturer(manufacturer:String):List[HidDeviceInfo] = getAttachedDevices.filter( _.getManufacturerString == manufacturer )

  // for polling device list to generate device connection Events
  private var poller:Option[Cancellable] = None
  private var lastDeviceList = List[MyHidDeviceInfo]()
  
  def startPolling(){
    import concurrent.duration._
    import concurrent.ExecutionContext.Implicits.global
    if(poller.isDefined) return
    poller = Some( system.scheduler.schedule(0 seconds, 1000 millis)(poll) )
  }

  def stopPolling(){
    poller.foreach(_.cancel)
    poller = None
  }

  private def poll(){
    this.synchronized {
      val deviceList = getAttachedDevices().map(new MyHidDeviceInfo(_))
      val removed = lastDeviceList.diff(deviceList)
      val added = deviceList.diff(lastDeviceList)
      val updated = deviceList.intersect(lastDeviceList)

      lastDeviceList = deviceList
      removed.foreach{ case d => detach(d) } // TODO how does device index change, or not change.
      updated.foreach{ case d => update(d) }
      added.foreach{ case d => attach(d) }

      if(removed.length + added.length > 0) 
        controllers.WebsocketActor.sendDeviceList
    }
  }

  private def attach(d:MyHidDeviceInfo){
    println(s"Attached: ${d.info.getProductString}")
    val ds = connectedDevices.getOrElseUpdate(d.info.getProductString, ListBuffer[MyHidDeviceInfo]())
    val index = ds.length // TODO hmm index is more complicated than this
    ds += d
    eventStreamActor.foreach( _ ! DeviceAttached(d, index)) // Maybe don't pass deviceinfo in events, as they could potentially be stale, require call back into DM to get current info
  }

  private def update(d:MyHidDeviceInfo){
    val ds = connectedDevices(d.info.getProductString)
    val index = ds.indexOf(d)   // will match old info
    ds(index) = d               // replace with new info
  }

  private def detach(d:MyHidDeviceInfo){
    println(s"Detached: ${d.info.getProductString}")
    val index = connectedDevices(d.info.getProductString).indexOf(d)
    val di = connectedDevices(d.info.getProductString).remove(index)
    eventStreamActor.foreach( _ ! DeviceDetached(di, index))
  }

}