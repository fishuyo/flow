package projector

import ProjectorCommand._

class Christie extends Projector {
  port = 3002

  def run(command:ProjectorCommand) = command match {
    case On => writeString("(PWR1)")
    case Off => writeString("(PWR0)")
    case Mute => writeString("(PMT1)")
    case Unmute => writeString("(PMT0)")
    case GetPowerState => writeString("(PWR?)")
    case SelectInput(1) => writeString("(SIN 11)")
    case SelectInput(2) => writeString("(SIN 21)")
    case BlendMode(mode) => writeString(s"(EBL+SLCT $mode)")
    case BlendOff => writeString("(EBL+SLCT 0)")
    case BlendOn => writeString("(EBL+SLCT 2)")
    case WarpMode(mode) => writeString(s"(WRP+SLCT $mode)")
    case WarpOff => writeString("(WRP+SLCT 0)")
    case WarpOn => writeString("(WRP+SLCT 2)")

    case SurroundMode => writeString("(WRP+SLCT 0)(EBL+SLCT 0)(SIN 21)")
    case DesktopMode => writeString("(WRP+SLCT 2)(EBL+SLCT 2)(SIN 11)")

    case Command(cmd) => writeString(cmd)

    case _ => //error("unimplemented")
  }

  def readResponse() = readResponse('(',')')
  def parseResponse():Unit = {
    if(response.length == 0) return
    try {
      val ack = response.split("\\s+")(0)
      val command = ack.split("!")(0)
      val value = ack.split("!").last
      val message = response.split("\\s+").last
      command match {
        case "PWR" => updateStatus(value)
        case _ => 
      }
    } catch { case e:Exception => println("parseResponse: parseError") }
  }

  def updateStatus(value:String):Unit = {
    status = value.toInt match {
      case 0 => ProjectorStatus.Off
      case 1 => ProjectorStatus.On
      case 11 => ProjectorStatus.PoweringUp
      case 10 => ProjectorStatus.PoweringDown
      case _ => status
    }
  }

}