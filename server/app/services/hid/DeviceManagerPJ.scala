
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
  * Helper object for accessing registered deviceIO from DeviceManager
  */
object Device {
  val registeredDevices = HashMap[String, (Int) => Device]()
  
  register("PLAYSTATION(R)3 Controller", new PS3Controller(_))
  register("Joy-Con (L)", new JoyconL(_))
  register("Joy-Con (R)", new JoyconR(_))

  // def apply(info:HidDeviceInfo, index:Int):Device = apply(info.getProductString, index)
  def apply(name:String, index:Int=0):Device = registeredDevices.getOrElse(name, (i:Int) => new UnknownDevice(name,i))(index)

  // TODO make abstract joystick device? how else can this work?
  // Because it will need to work without knowing which device will be connected, and then become that device
  // maybe some kind of wrapping class that listens for nth connecting joystick, or checks with DM 
  def joystick(index:Int = 0):Device = new PS3Controller(index)

  // mechanism to register and implement devices at runtime..
  // def register(d:Device) = registeredDevices(d.name)
  def register(name:String, construct:(Int) => Device) = registeredDevices(name) = construct
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

  // Open DeviceConnection and keep reference, called when first DeviceIO needs it
  def openDeviceConnection(dc:HidDeviceConnection) = this.synchronized {
    val option = getInfo(dc.name, dc.index)
    option.foreach { case di =>
      val dev = PureJavaHidApi.openDevice(di)
      dev.setInputReportListener( new InputReportListener(){
        override def onInputReport(source:HidDevice, id:Byte, data:Array[Byte], len:Int){
          dc.byteStreamActor.foreach(_ ! data)
        }
      })
      dc.openDevice = Some(dev)

      val ds = deviceConnections.getOrElseUpdate(dc.name, ListBuffer[HidDeviceConnection]())
      if(ds.length != dc.index) println("openDeviceConnection: bad index, this shouldn't happen..")
      ds += d
    }
  }

  def getDeviceConnection(name:String, index:Int) = {
    // connectedDevices(name)(index)
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