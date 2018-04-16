
package flow
package hid

class UnknownDevice(name:String, index:Int) extends DeviceIO(name, index) {
  // lazy val productString = name
  val sourceElements = List()
}