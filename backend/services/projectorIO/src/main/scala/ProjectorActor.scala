
package projector

import org.apache.pekko.actor._
import ProjectorCommand._
import ProjectorResponse._

object ProjectorActor {
  def props(p:Projector) = Props(new ProjectorActor(p))
  case object Connect
}

class ProjectorActor(p:Projector) extends Actor {
  import ProjectorActor._

  def receive = {
    case Connect => 
      p.connect()
      sender() ! ProjectorResponse(p.id, p.status, p.response)
    case cmd:ProjectorCommand => 
      p.run(cmd)
      p.readResponse()
      p.parseResponse()
      sender() ! ProjectorResponse(p.id, p.status, p.response)
  }

  override def postStop() = {
    // println(s"disconnect projector: ${p.name}")
    try { p.disconnect() } catch { case e:Exception => println(e) }
    super.postStop()
  }
}