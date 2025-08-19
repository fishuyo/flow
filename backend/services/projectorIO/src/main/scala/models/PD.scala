package projector

import ProjectorCommand._

class PD extends Projector {
  port = 1025

  def run(command:ProjectorCommand) = command match {
    case On => writeString(":POWR1\r\n")
    case Off => writeString(":POWR0\r\n")
    case Mute => writeString(":PMUT1\r\n")
    case Unmute => writeString(":PMUT0\r\n")
    case GetPowerState => writeString(":POST?\r\n")
    case Command(cmd) => writeString(cmd)
    case _ => //error("unimplemented")
  }

  def readResponse() = readResponse('%','\r')
  def parseResponse():Unit = {
    if(response.length == 0) return
    try {
      val ack = response.split("\\s+")(0)
      val command = response.split("\\s+")(1)
      val value = response.split("\\s+").last
      command match {
        case "POST" => updateStatus(value)
        case _ => 
      }
    } catch { case e:Exception => println("parseResponse: parseError") }
  }

  def updateStatus(value:String):Unit = {
    status = value.toInt match {
      case 0 => ProjectorStatus.DeepSleep
      case 1 => ProjectorStatus.Off
      case 2 => ProjectorStatus.PoweringUp
      case 3 => ProjectorStatus.On
      case 4 => ProjectorStatus.PoweringDown
      case 5 => ProjectorStatus.CriticalPoweringDown
      case 6 => ProjectorStatus.CriticalOff
      case _ => ProjectorStatus.Unknown
    }
  }

}

