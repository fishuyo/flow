package multishell.service

import multishell.protocol.Message
import multishell.protocol.{ShellInput, ShellError}
import org.apache.pekko.actor._
import org.apache.pekko.http.scaladsl.model.ws._
import scala.concurrent.ExecutionContext

object MultishellWSActor {
  def props(out: ActorRef)(implicit ec: ExecutionContext) = 
    Props(new MultishellWSActor(out))
}

class MultishellWSActor(out: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  private val shellProcess = new ShellProcess(out)
  
  println("[MultishellWSActor] Actor created")

  def receive = {
    case TextMessage.Strict(msg) if msg == "keepalive" => 
      // Ignore keepalive messages
      println("[MultishellWSActor] Received keepalive message")
      ()
    
    case TextMessage.Strict(msg) => 
      println(s"[MultishellWSActor] Received TextMessage: $msg")
      try {
        val message = upickle.default.read[Message](msg)
        println(s"[MultishellWSActor] Parsed message: $message")
        message match {
          case ShellInput(command) =>
            println(s"[MultishellWSActor] ShellInput received, command: '$command'")
            shellProcess.sendCommand(command)
          case other =>
            println(s"[MultishellWSActor] Unexpected message type: $other")
        }
      } catch {
        case e: Exception => 
          println(s"[MultishellWSActor] Error parsing message: $e")
          e.printStackTrace()
          val errorMsg = upickle.default.write(ShellError(s"Error parsing message: ${e.getMessage}"), indent = 0, escapeUnicode = true)
          out ! TextMessage(errorMsg)
      }

    case msg => 
      println(s"[MultishellWSActor] Unhandled message type: ${msg.getClass.getName}, content: $msg")
  }

  override def postStop(): Unit = {
    println("[MultishellWSActor] Actor stopping, cleaning up shell process")
    shellProcess.stop()
    super.postStop()
  }
}

