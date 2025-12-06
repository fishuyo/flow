package multishell.service

import org.apache.pekko.actor.ActorRef
import scala.concurrent.{ExecutionContext, Future}
import java.io.{InputStream, OutputStream}
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import com.pty4j.WinSize
import scala.jdk.CollectionConverters._
import multishell.protocol._

class ShellProcess(outputActor: ActorRef)(implicit ec: ExecutionContext) {
  private var process: Option[PtyProcess] = None
  private var currentRows: Int = 24
  private var currentCols: Int = 80
  
  // Expose process for checking if it exists
  def processExists: Boolean = process.isDefined

  def start(rows: Int = 24, cols: Int = 80): Unit = {
    if (process.isEmpty) {
      println(s"[ShellProcess] Starting PTY shell process (${rows}x${cols})...")
      try {
        currentRows = rows
        currentCols = cols
        
        // Try bash first, fall back to sh
        val shell = if (java.io.File("/bin/bash").exists()) "/bin/bash" else "/bin/sh"
        println(s"[ShellProcess] Using shell: $shell")
        
        // Create PTY process builder with interactive shell
        val command = Array(shell, "-i")  // -i for interactive mode
        val environment = System.getenv().asScala.toMap.asJava
        val builder = new PtyProcessBuilder()
          .setCommand(command)
          .setDirectory(System.getProperty("user.home"))
          .setEnvironment(environment)
        
        // Start the PTY process
        val proc: PtyProcess = builder.start()
        
        // Set initial terminal size
        proc.setWinSize(new WinSize(cols, rows))
        
        process = Some(proc)
        println("[ShellProcess] PTY shell process spawned successfully")
        
        // Start reading stdout (raw bytes, preserves ANSI codes)
        println("[ShellProcess] Starting stdout reader")
        readStream(proc.getInputStream(), isError = false)
        
        // Monitor process exit
        println("[ShellProcess] Starting exit monitor")
        monitorExit(proc)
        
        println("[ShellProcess] Shell process started and configured")
      } catch {
        case e: Exception =>
          println(s"[ShellProcess] Exception starting shell: $e")
          e.printStackTrace()
          sendError(s"Failed to start shell: ${e.getMessage}")
      }
    } else {
      println("[ShellProcess] Shell process already exists, not starting new one")
    }
  }

  def sendInput(data: String): Unit = {
    println(s"[ShellProcess] sendInput called with: '${data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
    process match {
      case Some(proc: PtyProcess) =>
        println("[ShellProcess] Process exists, sending input...")
        try {
          val outputStream = proc.getOutputStream()
          val writer = new java.io.OutputStreamWriter(outputStream, "UTF-8")
          writer.write(data)
          writer.flush()
          println("[ShellProcess] Input written and flushed successfully")
        } catch {
          case e: Exception =>
            println(s"[ShellProcess] Exception sending input: $e")
            e.printStackTrace()
            sendError(s"Failed to send input: ${e.getMessage}")
        }
      case None =>
        println("[ShellProcess] No process exists, starting new one...")
        start(currentRows, currentCols)
        // Wait a bit for process to start, then send input
        Future {
          Thread.sleep(200)
          println("[ShellProcess] Retrying sendInput after process start...")
          sendInput(data)
        }
    }
  }

  def resizeTerminal(rows: Int, cols: Int): Unit = {
    println(s"[ShellProcess] Resizing terminal to ${rows}x${cols}")
    process match {
      case Some(proc: PtyProcess) =>
        try {
          proc.setWinSize(new WinSize(cols, rows))  // Note: WinSize takes (width, height) which is (cols, rows)
          currentRows = rows
          currentCols = cols
          println(s"[ShellProcess] Terminal resized successfully to ${rows}x${cols}")
        } catch {
          case e: Exception =>
            println(s"[ShellProcess] Exception resizing terminal: $e")
            e.printStackTrace()
            sendError(s"Failed to resize terminal: ${e.getMessage}")
        }
      case None =>
        println("[ShellProcess] No process exists, storing size for when process starts")
        currentRows = rows
        currentCols = cols
    }
  }

  private def readStream(stream: InputStream, isError: Boolean): Unit = {
    val streamType = if (isError) "stderr" else "stdout"
    println(s"[ShellProcess] Starting $streamType reader thread")
    Future {
      val buffer = new Array[Byte](4096)
      try {
        println(s"[ShellProcess] $streamType reader: waiting for input...")
        var bytesRead = 0
        while ({ bytesRead = stream.read(buffer); bytesRead != -1 }) {
          if (bytesRead > 0) {
            // Convert bytes to string preserving ANSI escape codes
            val text = new String(buffer, 0, bytesRead, "UTF-8")
            println(s"[ShellProcess] $streamType reader: read ${bytesRead} bytes")
            if (isError) {
              sendError(text)
            } else {
              sendOutput(text)
            }
          }
        }
        println(s"[ShellProcess] $streamType reader: stream ended (bytesRead == -1)")
      } catch {
        case e: Exception =>
          println(s"[ShellProcess] $streamType reader: Exception: $e")
          e.printStackTrace()
          // Stream closed, process likely terminated
          if (!isError) {
            sendError(s"Stream closed: ${e.getMessage}")
          }
      }
    }
  }

  private def monitorExit(proc: PtyProcess): Unit = {
    println("[ShellProcess] Starting exit monitor")
    Future {
      try {
        println("[ShellProcess] Waiting for process to exit...")
        val exitCode = proc.waitFor()
        println(s"[ShellProcess] Process exited with code: $exitCode")
        sendExit(exitCode)
        process = None
      } catch {
        case e: Exception =>
          println(s"[ShellProcess] Exception in monitorExit: $e")
          e.printStackTrace()
      }
    }
  }

  private def sendOutput(text: String): Unit = {
    // Don't log every output to avoid spam, only log occasionally
    try {
      val message = upickle.default.write(ShellOutput(text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendOutput: $e")
        e.printStackTrace()
    }
  }

  private def sendError(text: String): Unit = {
    println(s"[ShellProcess] sendError: '$text'")
    try {
      val message = upickle.default.write(ShellError(text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendError: $e")
        e.printStackTrace()
    }
  }

  private def sendExit(code: Int): Unit = {
    println(s"[ShellProcess] sendExit: code=$code")
    try {
      val message = upickle.default.write(ShellExit(code))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendExit: $e")
        e.printStackTrace()
    }
  }

  def stop(): Unit = {
    process.foreach { proc =>
      try {
        proc.destroy()
        process = None
        println("[ShellProcess] Process stopped")
      } catch {
        case e: Exception =>
          println(s"[ShellProcess] Exception stopping process: $e")
          sendError(s"Failed to stop shell: ${e.getMessage}")
      }
    }
  }
}
