package multishell.service

import org.apache.pekko.actor.ActorRef
import scala.concurrent.{ExecutionContext, Future}
import java.io.{BufferedReader, InputStreamReader}
import scala.util.{Try, Success, Failure}
import multishell.protocol._

class ShellProcess(outputActor: ActorRef)(implicit ec: ExecutionContext) {
  private var process: Option[Any] = None

  def start(): Unit = {
    if (process.isEmpty) {
      println("[ShellProcess] Starting shell process...")
      try {
        // Try bash first, fall back to sh
        val shell = if (os.exists(os.Path("/bin/bash"))) "bash" else "sh"
        println(s"[ShellProcess] Using shell: $shell")
        val proc = os.proc(shell).spawn(
          stdin = os.Pipe,
          stdout = os.Pipe,
          stderr = os.Pipe
        )
        process = Some(proc)
        println("[ShellProcess] Shell process spawned successfully")
        
        // Access stdin/stdout/stderr using reflection-like approach
        val stdinField = proc.getClass.getMethod("stdin")
        val stdoutField = proc.getClass.getMethod("stdout")
        val stderrField = proc.getClass.getMethod("stderr")
        println("[ShellProcess] Got stream fields via reflection")
        
        // Start reading stdout
        println("[ShellProcess] Starting stdout reader")
        readStream(stdoutField.invoke(proc).asInstanceOf[java.io.InputStream], isError = false)
        // Start reading stderr
        println("[ShellProcess] Starting stderr reader")
        readStream(stderrField.invoke(proc).asInstanceOf[java.io.InputStream], isError = true)
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

  def sendCommand(command: String): Unit = {
    println(s"[ShellProcess] sendCommand called with: '$command'")
    process match {
      case Some(proc) =>
        println("[ShellProcess] Process exists, sending command...")
        try {
          val stdinField = proc.getClass.getMethod("stdin")
          val stdin = stdinField.invoke(proc).asInstanceOf[java.io.OutputStream]
          println(s"[ShellProcess] Got stdin stream: $stdin")
          val writer = new java.io.OutputStreamWriter(stdin)
          val commandWithNewline = command + "\n"
          println(s"[ShellProcess] Writing command to stdin: '$commandWithNewline'")
          writer.write(commandWithNewline)
          writer.flush()
          println("[ShellProcess] Command written and flushed successfully")
        } catch {
          case e: Exception =>
            println(s"[ShellProcess] Exception sending command: $e")
            e.printStackTrace()
            sendError(s"Failed to send command: ${e.getMessage}")
        }
      case None =>
        println("[ShellProcess] No process exists, starting new one...")
        start()
        // Wait a bit for process to start, then send command
        Future {
          Thread.sleep(200)
          println("[ShellProcess] Retrying sendCommand after process start...")
          sendCommand(command)
        }
    }
  }

  private def readStream(stream: java.io.InputStream, isError: Boolean): Unit = {
    val streamType = if (isError) "stderr" else "stdout"
    println(s"[ShellProcess] Starting $streamType reader thread")
    Future {
      val reader = new BufferedReader(new InputStreamReader(stream))
      var line: String = null
      try {
        println(s"[ShellProcess] $streamType reader: waiting for input...")
        while ({ line = reader.readLine(); line != null }) {
          println(s"[ShellProcess] $streamType reader: read line: '$line'")
          if (isError) {
            println(s"[ShellProcess] Sending error line: '$line'")
            sendError(line)
          } else {
            println(s"[ShellProcess] Sending output line: '$line'")
            sendOutput(line)
          }
        }
        println(s"[ShellProcess] $streamType reader: stream ended (line is null)")
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

  private def monitorExit(proc: Any): Unit = {
    println("[ShellProcess] Starting exit monitor")
    Future {
      try {
        println("[ShellProcess] Waiting for process to exit...")
        val waitForMethod = proc.getClass.getMethod("waitFor")
        val exitCode = waitForMethod.invoke(proc).asInstanceOf[Int]
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
    println(s"[ShellProcess] sendOutput: '$text'")
    try {
      val message = upickle.default.write(ShellOutput(text))
      println(s"[ShellProcess] Serialized output message: $message")
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
      println("[ShellProcess] Output message sent to actor")
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
      println(s"[ShellProcess] Serialized error message: $message")
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
      println("[ShellProcess] Error message sent to actor")
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
      println(s"[ShellProcess] Serialized exit message: $message")
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
      println("[ShellProcess] Exit message sent to actor")
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendExit: $e")
        e.printStackTrace()
    }
  }

  def stop(): Unit = {
    process.foreach { proc =>
      try {
        val destroyMethod = proc.getClass.getMethod("destroy")
        destroyMethod.invoke(proc)
        process = None
      } catch {
        case e: Exception =>
          sendError(s"Failed to stop shell: ${e.getMessage}")
      }
    }
  }
}

