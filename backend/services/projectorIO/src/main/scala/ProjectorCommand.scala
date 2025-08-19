
package projector

import upickle.default._

sealed trait ProjectorCommand derives ReadWriter

object ProjectorCommand {
  case object Noop extends ProjectorCommand
  case object On extends ProjectorCommand
  case object Off extends ProjectorCommand
  case object Mute extends ProjectorCommand
  case object Unmute extends ProjectorCommand

  case class SelectInput(input:Int) extends ProjectorCommand
  case class BlendMode(mode:Int) extends ProjectorCommand
  case object BlendOff extends ProjectorCommand
  case object BlendOn extends ProjectorCommand
  case class WarpMode(mode:Int) extends ProjectorCommand
  case object WarpOff extends ProjectorCommand
  case object WarpOn extends ProjectorCommand

  case class Command(command:String) extends ProjectorCommand

  case object GetPowerState extends ProjectorCommand

  case object SurroundMode extends ProjectorCommand
  case object DesktopMode extends ProjectorCommand

}

// sealed trait ProjectorResponse derives ReadWriter
// object ProjectorResponse {
  // case class Error(message:String) extends ProjectorResponse
  // case class Response(ack:String, name:String, value:String) extends ProjectorResponse
// }

sealed trait ProjectorStatus derives ReadWriter
object ProjectorStatus{
  case object Disconnected extends ProjectorStatus
  case object Connected extends ProjectorStatus
  case object Unknown extends ProjectorStatus
  case object Unreachable extends ProjectorStatus

  case object ParseError extends ProjectorStatus
  case object DeepSleep extends ProjectorStatus
  case object PoweringUp extends ProjectorStatus
  case object PoweringDown extends ProjectorStatus
  case object On extends ProjectorStatus
  case object Off extends ProjectorStatus
  case object CriticalPoweringDown extends ProjectorStatus
  case object CriticalOff extends ProjectorStatus
}
