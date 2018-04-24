
package flow
package hid

import purejavahidapi._

import collection.JavaConverters._
import collection.mutable.ListBuffer
import collection.mutable.HashMap

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

/** DeviceConnectionEvent case classes **/
sealed trait DeviceConnectionEvent { def device:HidDeviceInfoW; def index:Int }
case class DeviceAttached(device:HidDeviceInfoW, index:Int) extends DeviceConnectionEvent
case class DeviceDetached(device:HidDeviceInfoW, index:Int) extends DeviceConnectionEvent

/** Wrapper for device polling, override equality checking of HidDeviceInfo */
class HidDeviceInfoW(val info:HidDeviceInfo) {
  override def equals(other:Any) = other match {
    case b:HidDeviceInfoW =>
      (info.getProductId == b.info.getProductId && info.getVendorId == b.info.getVendorId && info.getSerialNumberString == b.info.getSerialNumberString)
    case _ => false
  }
  override def hashCode = {
    val s = info.getSerialNumberString
    41*(41+info.getProductId) + info.getVendorId + (if(s == null) 0 else s.hashCode)
  }
}

/** 
  * Device Manager handles connection events of HidDevices 
  */
object DeviceManager {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  // map of connected devices keyed on product string
  private val availableDevices = HashMap[String,ListBuffer[HidDeviceInfoW]]()
  private val deviceConnections = HashMap[String,ListBuffer[HidDeviceConnection]]()
  
  // create a stream actor for connection events
  // using a broadcast hub to allow sending connection events to multiple consumers
  private var eventStreamActor = None:Option[ActorRef]
  private val connectionEventSource = Source.actorRef[DeviceConnectionEvent](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => eventStreamActor = Some(a) )
  val connectionEvents = connectionEventSource.toMat(BroadcastHub.sink)(Keep.right).run() 
  connectionEvents.runWith(Sink.ignore)  // default consumer keeps stream running

  def getDevices() = {
    this.synchronized {
      availableDevices.values
    }
  }

  def getRegisteredDevices() = {
    this.synchronized {
      availableDevices.filter { case (k,v) => Device.registeredDevices.contains(k) }.values
    }
  }

  def getDeviceInfo(name:String, index:Int=0) = this.synchronized {
    getInfo(name,index)
  }
  
  private def getInfo(name:String, index:Int=0) = {
    val ds = availableDevices.getOrElseUpdate(name, ListBuffer[HidDeviceInfoW]())
    if(index < ds.length){
      val di = ds(index)
      Some(di.info)
    } else None
  }

  def getDeviceConnection(name:String, index:Int) = {
    val dcs = deviceConnections.getOrElseUpdate(name, ListBuffer[HidDeviceConnection]())
    dcs.find(_.index == index) match {
      case Some(dc) => dc
      case None =>        
        val dc = new HidDeviceConnection(name, index)
        dcs += dc 
        dc
    }
  }
  
  def openDeviceConnection(dc:HidDeviceConnection) = this.synchronized {
    val option = getInfo(dc.name, dc.index)
    println(option)
    option.foreach { case di =>
      val dev = PureJavaHidApi.openDevice(di)
      dev.setInputReportListener( new InputReportListener(){
        override def onInputReport(source:HidDevice, id:Byte, data:Array[Byte], len:Int){
          dc.byteStreamActor.foreach(_ ! data)
        }
      })
      dc.openDevice = Some(dev)
    }
  }

  def closeDeviceConnection(dc:HidDeviceConnection) = {

  }

  private def getAttachedDevices():List[HidDeviceInfo] = PureJavaHidApi.enumerateDevices.asScala.toList
  private def getAttachedDevicesByName(product:String):List[HidDeviceInfo] = getAttachedDevices.filter( _.getProductString == product )
  private def getAttachedDevicesByManufacturer(manufacturer:String):List[HidDeviceInfo] = getAttachedDevices.filter( _.getManufacturerString == manufacturer )

  // for polling device list to generate device connection Events
  private var poller:Option[Cancellable] = None
  private var lastDeviceList = List[HidDeviceInfoW]()
  
  // start polling for devices at 1 second interval
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
      val deviceList = getAttachedDevices().map(new HidDeviceInfoW(_))
      val removed = lastDeviceList.diff(deviceList)
      val added = deviceList.diff(lastDeviceList)
      val updated = deviceList.intersect(lastDeviceList)

      lastDeviceList = deviceList
      removed.foreach(detach(_)) // TODO how does device index change, or not change.
      updated.foreach(update(_))
      added.foreach(attach(_))

      if(removed.length + added.length > 0) 
        controllers.WebsocketActor.sendDeviceList
    }
  }

  private def attach(d:HidDeviceInfoW){
    println(s"Attached: ${d.info.getProductString}")
    val ds = availableDevices.getOrElseUpdate(d.info.getProductString, ListBuffer[HidDeviceInfoW]())
    val index = ds.length // TODO hmm index is more complicated than this
    ds += d
    eventStreamActor.foreach( _ ! DeviceAttached(d, index)) // Maybe don't pass deviceinfo in events, as they could potentially be stale, require call back into DM to get current info
  }

  private def update(d:HidDeviceInfoW){
    val ds = availableDevices(d.info.getProductString)
    val index = ds.indexOf(d)   // will match old info
    ds(index) = d               // replace with new info
  }

  private def detach(d:HidDeviceInfoW){
    println(s"Detached: ${d.info.getProductString}")
    val index = availableDevices(d.info.getProductString).indexOf(d)
    val di = availableDevices(d.info.getProductString).remove(index)
    eventStreamActor.foreach( _ ! DeviceDetached(di, index))
  }

}