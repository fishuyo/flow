package flow

import hid._

import collection.mutable.HashMap

/**
  * Helper object for accessing registered deviceIO from DeviceManager
  */
object Device {
  val registeredDevices = HashMap[String, (Int) => IO]()
  
  register("PLAYSTATION(R)3 Controller", new PS3Controller(_))
  register("Joy-Con (L)", new JoyconL(_))
  register("Joy-Con (R)", new JoyconR(_))

  // def apply(info:HidDeviceInfo, index:Int):Device = apply(info.getProductString, index)
  def apply(name:String, index:Int=0):IO = registeredDevices.getOrElse(name, (i:Int) => new UnknownDevice(name,i))(index)

  // TODO make abstract joystick device? how else can this work?
  // Because it will need to work without knowing which device will be connected, and then become that device
  // maybe some kind of wrapping class that listens for nth connecting joystick, or checks with DM 
  def joystick(index:Int = 0):IO = new PS3Controller(index)

  // mechanism to register and implement devices at runtime..
  // def register(d:Device) = registeredDevices(d.name)
  def register(name:String, construct:(Int) => IO) = registeredDevices(name) = construct
}
