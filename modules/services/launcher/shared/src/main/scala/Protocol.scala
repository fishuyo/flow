
package flow
package launcher
package protocol


import upickle.default.ReadWriter

sealed trait Message derives ReadWriter

final case class Handshake(msg:String="hi") extends Message
final case class Output(text:String) extends Message
final case class Command(text:String) extends Message
