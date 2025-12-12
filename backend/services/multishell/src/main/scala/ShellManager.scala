package multishell.service

import org.apache.pekko.actor.ActorRef
import scala.concurrent.ExecutionContext
import scala.collection.mutable
import multishell.protocol._

class ShellManager(outputActor: ActorRef)(implicit ec: ExecutionContext) {
  private val shells = mutable.Map[String, ShellProcess]()
  
  def createShell(instanceId: String, hostConfig: Option[HostConfig]): Unit = {
    if (shells.contains(instanceId)) {
      // Destroy existing shell before creating new one (handles reconnection)
      println(s"[ShellManager] Shell instance '$instanceId' already exists, destroying old instance...")
      destroyShell(instanceId)
    }
    
    println(s"[ShellManager] Creating shell instance '$instanceId'")
    val config: ShellConfig = hostConfig match {
      case Some(config) => RemoteShell(config)
      case None => LocalShell
    }
    val shellProcess = new ShellProcess(instanceId, config, outputActor)
    shells(instanceId) = shellProcess
    println(s"[ShellManager] Shell instance '$instanceId' created")
  }
  
  def getShell(instanceId: String): Option[ShellProcess] = {
    shells.get(instanceId)
  }
  
  def destroyShell(instanceId: String): Unit = {
    shells.remove(instanceId).foreach { shell =>
      println(s"[ShellManager] Destroying shell instance '$instanceId'")
      shell.stop()
      println(s"[ShellManager] Shell instance '$instanceId' destroyed")
    }
  }
  
  def listShells(): List[ShellInstanceInfo] = {
    shells.values.map(_.getInfo()).toList
  }
  
  def broadcastInput(data: String): Unit = {
    println(s"[ShellManager] Broadcasting input to ${shells.size} shell instances")
    shells.values.foreach { shell =>
      if (!shell.processExists) {
        shell.start(24, 80)  // Default size
      }
      shell.sendInput(data)
    }
  }
  
  def broadcastResize(rows: Int, cols: Int): Unit = {
    println(s"[ShellManager] Broadcasting resize to ${shells.size} shell instances")
    shells.values.foreach { shell =>
      if (!shell.processExists) {
        shell.start(rows, cols)
      } else {
        shell.resizeTerminal(rows, cols)
      }
    }
  }
  
  def stopAll(): Unit = {
    println(s"[ShellManager] Stopping all ${shells.size} shell instances")
    shells.values.foreach(_.stop())
    shells.clear()
  }
  
  private def sendError(instanceId: String, text: String): Unit = {
    try {
      val message = upickle.default.write(ShellError(instanceId, text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellManager] Exception in sendError: $e")
        e.printStackTrace()
    }
  }
}
