
package flow
package hid

class UnknownDevice(name:String, index:Int) extends Device(index) {
  lazy val productString = name
  val sourceElements = List()
}