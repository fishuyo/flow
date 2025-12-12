package multishell.service

import multishell.protocol.{Message => ProtocolMessage, _}
import org.apache.pekko.actor._
import org.apache.pekko.http.scaladsl.model.ws._
import scala.concurrent.ExecutionContext

object MultishellWSActor {
  def props(out: ActorRef)(implicit ec: ExecutionContext) = 
    Props(new MultishellWSActor(out))
}

class MultishellWSActor(out: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  private val shellManager = new ShellManager(out)
  
  println("[MultishellWSActor] Actor created")

  def receive = {
    case TextMessage.Strict(msg) if msg == "keepalive" => 
      // Ignore keepalive messages
      println("[MultishellWSActor] Received keepalive message")
      ()
    
    case TextMessage.Strict(msg) => 
      println(s"[MultishellWSActor] Received TextMessage: $msg")
      try {
        val message = upickle.default.read[ProtocolMessage](msg)
        println(s"[MultishellWSActor] Parsed message: $message")
        message match {
          // Instance management
          case m: CreateShell =>
            println(s"[MultishellWSActor] CreateShell received: instanceId=${m.instanceId}, hostConfig=${m.hostConfig}")
            shellManager.createShell(m.instanceId, m.hostConfig)
          
          case m: DestroyShell =>
            println(s"[MultishellWSActor] DestroyShell received: instanceId=${m.instanceId}")
            shellManager.destroyShell(m.instanceId)
          
          case _: ListShells =>
            println("[MultishellWSActor] ListShells received")
            val instances = shellManager.listShells()
            val response = upickle.default.write(ShellsList(instances))
            out ! TextMessage(response)
          
          // Instance-specific messages
          case m: ShellInput =>
            println(s"[MultishellWSActor] ShellInput received: instanceId=${m.instanceId}, data='${m.data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
            shellManager.getShell(m.instanceId) match {
              case Some(shell) =>
                if (!shell.processExists) {
                  shell.start(24, 80)  // Default size, will be updated by resize message
                }
                shell.sendInput(m.data)
              case None =>
                // Auto-create shell instance if it doesn't exist (for reconnection scenarios)
                println(s"[MultishellWSActor] Shell instance '${m.instanceId}' not found, auto-creating...")
                shellManager.createShell(m.instanceId, None)
                // Retry after a brief delay
                scala.concurrent.Future {
                  Thread.sleep(200)
                  shellManager.getShell(m.instanceId).foreach { shell =>
                    shell.start(24, 80)
                    shell.sendInput(m.data)
                  }
                }(scala.concurrent.ExecutionContext.global)
            }
          
          case m: ShellResize =>
            println(s"[MultishellWSActor] ShellResize received: instanceId=${m.instanceId}, ${m.rows}x${m.cols}")
            shellManager.getShell(m.instanceId) match {
              case Some(shell) =>
                if (!shell.processExists) {
                  println(s"[MultishellWSActor] Starting shell process for instance ${m.instanceId} with size ${m.rows}x${m.cols}")
                  shell.start(m.rows, m.cols)
                } else {
                  shell.resizeTerminal(m.rows, m.cols)
                }
              case None =>
                // Auto-create shell instance if it doesn't exist (for reconnection scenarios)
                println(s"[MultishellWSActor] Shell instance '${m.instanceId}' not found, auto-creating...")
                shellManager.createShell(m.instanceId, None)
                // Retry after a brief delay
                scala.concurrent.Future {
                  Thread.sleep(300) // Increased delay
                  shellManager.getShell(m.instanceId).foreach { shell =>
                    println(s"[MultishellWSActor] Starting shell process for instance ${m.instanceId} with size ${m.rows}x${m.cols}")
                    shell.start(m.rows, m.cols)
                  }
                }(scala.concurrent.ExecutionContext.global)
            }
          
          // Broadcast messages
          case m: BroadcastInput =>
            println(s"[MultishellWSActor] BroadcastInput received: data='${m.data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
            shellManager.broadcastInput(m.data)
          
          case m: BroadcastResize =>
            println(s"[MultishellWSActor] BroadcastResize received: ${m.rows}x${m.cols}")
            shellManager.broadcastResize(m.rows, m.cols)
          
          case other =>
            println(s"[MultishellWSActor] Unexpected message type: $other")
        }
      } catch {
        case e: Exception => 
          println(s"[MultishellWSActor] Error parsing message: $e")
          e.printStackTrace()
          // For error messages, we need an instanceId - use a default one
          val errorMsg = upickle.default.write(ShellError("unknown", s"Error parsing message: ${e.getMessage}"), indent = 0, escapeUnicode = true)
          out ! TextMessage(errorMsg)
      }

    case msg => 
      println(s"[MultishellWSActor] Unhandled message type: ${msg.getClass.getName}, content: $msg")
  }

  override def postStop(): Unit = {
    println("[MultishellWSActor] Actor stopping, cleaning up all shell processes")
    shellManager.stopAll()
    super.postStop()
  }
}

