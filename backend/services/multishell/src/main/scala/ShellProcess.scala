package multishell.service

import org.apache.pekko.actor.ActorRef
import scala.concurrent.{ExecutionContext, Future}
import java.io.{InputStream, OutputStream}
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import com.pty4j.WinSize
import scala.jdk.CollectionConverters._
import multishell.protocol._

// Shell configuration
sealed trait ShellConfig
case object LocalShell extends ShellConfig
final case class RemoteShell(hostConfig: HostConfig) extends ShellConfig

class ShellProcess(instanceId: String, config: ShellConfig, outputActor: ActorRef)(implicit ec: ExecutionContext) {
  private var process: Option[PtyProcess] = None
  private var currentRows: Int = 24
  private var currentCols: Int = 80
  
  // Expose process for checking if it exists
  def processExists: Boolean = process.isDefined
  
  def getInfo(): ShellInstanceInfo = {
    val host = config match {
      case LocalShell => "localhost"
      case RemoteShell(hostConfig) => hostConfig.host
    }
    val status = if (process.isDefined) ShellStatusRunning else ShellStatusStopped
    ShellInstanceInfo(instanceId, host, status)
  }

  def start(rows: Int = 24, cols: Int = 80): Unit = {
    if (process.isEmpty) {
      println(s"[ShellProcess:$instanceId] Starting PTY shell process (${rows}x${cols})...")
      try {
        currentRows = rows
        currentCols = cols
        
        // Build command based on configuration
        val command = buildCommand()
        println(s"[ShellProcess:$instanceId] Command: ${command.mkString(" ")}")
        
        val environment = System.getenv().asScala.toMap.asJava
        val builder = new PtyProcessBuilder()
          .setCommand(command)
          .setDirectory(System.getProperty("user.home"))
          .setEnvironment(environment)
        
        // Start the PTY process
        val proc: PtyProcess = builder.start()
        
        process = Some(proc)
        println(s"[ShellProcess:$instanceId] PTY shell process spawned successfully")
        
        // Set initial terminal size after a brief delay to ensure PTY is ready
        // The PTY needs a moment to fully initialize before we can set window size
        scala.concurrent.Future {
          Thread.sleep(200) // Increased delay for PTY initialization
          try {
            proc.setWinSize(new WinSize(cols, rows))
            println(s"[ShellProcess:$instanceId] Initial terminal size set to ${rows}x${cols}")
          } catch {
            case e: Exception =>
              println(s"[ShellProcess:$instanceId] Warning: Failed to set initial window size: $e")
              // Don't fail the entire process if window size setting fails
              // The resize can be retried later when the terminal is resized
          }
        }(ec)
        
        // Start reading stdout (raw bytes, preserves ANSI codes)
        println(s"[ShellProcess:$instanceId] Starting stdout reader")
        readStream(proc.getInputStream(), isError = false)
        
        // Monitor process exit (non-blocking)
        println(s"[ShellProcess:$instanceId] Starting exit monitor")
        monitorExit(proc)
        
        println(s"[ShellProcess:$instanceId] Shell process started and configured")
      } catch {
        case e: Exception =>
          println(s"[ShellProcess:$instanceId] Exception starting shell: $e")
          e.printStackTrace()
          sendError(s"Failed to start shell: ${e.getMessage}")
      }
    } else {
      println(s"[ShellProcess:$instanceId] Shell process already exists, not starting new one")
    }
  }
  
  private def buildCommand(): Array[String] = {
    config match {
      case LocalShell =>
        // Local shell: /bin/bash -i or /bin/sh -i
        val shell = if (java.io.File("/bin/bash").exists()) "/bin/bash" else "/bin/sh"
        Array(shell, "-i")
      
      case RemoteShell(hostConfig) =>
        // Remote shell: ssh -t user@host bash -i
        val sshCommand = scala.collection.mutable.ArrayBuffer[String]("ssh", "-t")
        
        // Add identity file if specified
        hostConfig.identityFile.foreach { identity =>
          sshCommand += "-i"
          sshCommand += identity
        }
        
        // Add port if specified
        hostConfig.port.foreach { port =>
          sshCommand += "-p"
          sshCommand += port.toString
        }
        
        // Build user@host string
        val userHost = hostConfig.user match {
          case Some(user) => s"$user@${hostConfig.host}"
          case None => hostConfig.host
        }
        sshCommand += userHost
        
        // Remote shell command
        sshCommand += "bash"
        sshCommand += "-i"
        
        sshCommand.toArray
    }
  }

  def sendInput(data: String): Unit = {
    println(s"[ShellProcess:$instanceId] sendInput called with: '${data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
    process match {
      case Some(proc: PtyProcess) =>
        println(s"[ShellProcess:$instanceId] Process exists, sending input...")
        try {
          val outputStream = proc.getOutputStream()
          val writer = new java.io.OutputStreamWriter(outputStream, "UTF-8")
          writer.write(data)
          writer.flush()
          println(s"[ShellProcess:$instanceId] Input written and flushed successfully")
        } catch {
          case e: Exception =>
            println(s"[ShellProcess:$instanceId] Exception sending input: $e")
            e.printStackTrace()
            sendError(s"Failed to send input: ${e.getMessage}")
        }
      case None =>
        println(s"[ShellProcess:$instanceId] No process exists, starting new one...")
        start(currentRows, currentCols)
        // Wait a bit for process to start, then send input
        Future {
          Thread.sleep(200)
          println(s"[ShellProcess:$instanceId] Retrying sendInput after process start...")
          sendInput(data)
        }
    }
  }

  def resizeTerminal(rows: Int, cols: Int): Unit = {
    println(s"[ShellProcess:$instanceId] Resizing terminal to ${rows}x${cols}")
    process match {
      case Some(proc: PtyProcess) =>
        try {
          // Check if process is still alive before resizing
          try {
            proc.exitValue() // This will throw if process is still running
            // Process has exited, can't resize
            println(s"[ShellProcess:$instanceId] Process has exited, cannot resize")
            return
          } catch {
            case _: IllegalThreadStateException =>
              // Process is still running, proceed with resize
          }
          
          proc.setWinSize(new WinSize(cols, rows))  // Note: WinSize takes (width, height) which is (cols, rows)
          currentRows = rows
          currentCols = cols
          println(s"[ShellProcess:$instanceId] Terminal resized successfully to ${rows}x${cols}")
        } catch {
          case e: Exception =>
            println(s"[ShellProcess:$instanceId] Exception resizing terminal: $e")
            // Don't send error for resize failures - it's not critical
            // The terminal will work fine even if resize fails
            e.printStackTrace()
        }
      case None =>
        println(s"[ShellProcess:$instanceId] No process exists, storing size for when process starts")
        currentRows = rows
        currentCols = cols
    }
  }

  private def readStream(stream: InputStream, isError: Boolean): Unit = {
    val streamType = if (isError) "stderr" else "stdout"
    println(s"[ShellProcess:$instanceId] Starting $streamType reader thread")
    Future {
      val buffer = new Array[Byte](4096)
      try {
        println(s"[ShellProcess:$instanceId] $streamType reader: waiting for input...")
        var bytesRead = 0
        while ({ bytesRead = stream.read(buffer); bytesRead != -1 }) {
          if (bytesRead > 0) {
            // Convert bytes to string preserving ANSI escape codes
            val text = new String(buffer, 0, bytesRead, "UTF-8")
            println(s"[ShellProcess:$instanceId] $streamType reader: read ${bytesRead} bytes")
            if (isError) {
              sendError(text)
            } else {
              sendOutput(text)
            }
          }
        }
        println(s"[ShellProcess:$instanceId] $streamType reader: stream ended (bytesRead == -1)")
      } catch {
        case e: Exception =>
          println(s"[ShellProcess:$instanceId] $streamType reader: Exception: $e")
          e.printStackTrace()
          // Stream closed, process likely terminated
          if (!isError) {
            sendError(s"Stream closed: ${e.getMessage}")
          }
      }
    }
  }

  private def monitorExit(proc: PtyProcess): Unit = {
    println(s"[ShellProcess:$instanceId] Starting exit monitor")
    Future {
      try {
        // Don't wait indefinitely - use a timeout approach
        // The process will be cleaned up by stop() if needed
        val exitCode = proc.waitFor()
        println(s"[ShellProcess:$instanceId] Process exited with code: $exitCode")
        sendExit(exitCode)
        process = None
      } catch {
        case e: InterruptedException =>
          println(s"[ShellProcess:$instanceId] Exit monitor interrupted (process being stopped)")
          process = None
        case e: Exception =>
          println(s"[ShellProcess:$instanceId] Exception in monitorExit: $e")
          e.printStackTrace()
          process = None
      }
    }
  }

  private def sendOutput(text: String): Unit = {
    // Don't log every output to avoid spam, only log occasionally
    try {
      val message = upickle.default.write(ShellOutput(instanceId, text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess:$instanceId] Exception in sendOutput: $e")
        e.printStackTrace()
    }
  }

  private def sendError(text: String): Unit = {
    println(s"[ShellProcess:$instanceId] sendError: '$text'")
    try {
      val message = upickle.default.write(ShellError(instanceId, text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess:$instanceId] Exception in sendError: $e")
        e.printStackTrace()
    }
  }

  private def sendExit(code: Int): Unit = {
    println(s"[ShellProcess:$instanceId] sendExit: code=$code")
    try {
      val message = upickle.default.write(ShellExit(instanceId, code))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess:$instanceId] Exception in sendExit: $e")
        e.printStackTrace()
    }
  }

  def stop(): Unit = {
    process.foreach { proc =>
      try {
        println(s"[ShellProcess:$instanceId] Stopping process...")
        // Force destroy the process
        proc.destroyForcibly()
        // Wait a short time for it to exit, then give up
        try {
          val exited = proc.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)
          if (exited) {
            println(s"[ShellProcess:$instanceId] Process exited cleanly")
          } else {
            println(s"[ShellProcess:$instanceId] Process did not exit within timeout, forcing destroy")
            proc.destroyForcibly()
          }
        } catch {
          case _: InterruptedException =>
            println(s"[ShellProcess:$instanceId] Wait interrupted, process destroyed")
        }
        process = None
        println(s"[ShellProcess:$instanceId] Process stopped")
      } catch {
        case e: Exception =>
          println(s"[ShellProcess:$instanceId] Exception stopping process: $e")
          process = None
          // Don't send error on stop - process is being cleaned up
      }
    }
  }
}
