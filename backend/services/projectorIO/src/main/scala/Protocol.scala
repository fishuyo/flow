
package projector

import upickle.default._


sealed trait Protocol derives ReadWriter
case class RunAll(command:ProjectorCommand) extends Protocol
case class RunGroup(name:String, command:ProjectorCommand) extends Protocol
case class Run(id:Int, command:ProjectorCommand) extends Protocol

case class ProjectorInfo(id:Int, name:String, commands:Seq[ProjectorCommand]) extends Protocol
case class ProjectorGroup(name:String, projectors:Seq[ProjectorInfo]) extends Protocol
case class ProjectorList(groups:Seq[ProjectorGroup]) extends Protocol

case class ProjectorResponse(id:Int, status:ProjectorStatus, message:String) extends Protocol


