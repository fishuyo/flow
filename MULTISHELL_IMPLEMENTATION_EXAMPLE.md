# Multishell Implementation Example: System SSH Approach

## Simplified ShellProcess Refactoring

### Current ShellProcess (to be refactored)

```scala
class ShellProcess(outputActor: ActorRef)(implicit ec: ExecutionContext) {
  def start(rows: Int = 24, cols: Int = 80): Unit = {
    val shell = if (java.io.File("/bin/bash").exists()) "/bin/bash" else "/bin/sh"
    val command = Array(shell, "-i")
    val builder = new PtyProcessBuilder()
      .setCommand(command)
      .setDirectory(System.getProperty("user.home"))
      .setEnvironment(System.getenv().asScala.toMap.asJava)
    val proc = builder.start()
    // ... rest of implementation
  }
}
```

### Refactored ShellProcess (supports local & remote)

```scala
package multishell.service

import org.apache.pekko.actor.ActorRef
import scala.concurrent.ExecutionContext
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import com.pty4j.WinSize
import scala.jdk.CollectionConverters._
import multishell.protocol._

sealed trait ShellConfig
case object LocalShell extends ShellConfig
case class RemoteShell(hostConfig: HostConfig) extends ShellConfig

class ShellProcess(
  val instanceId: String,
  outputActor: ActorRef,
  shellConfig: ShellConfig
)(implicit ec: ExecutionContext) {
  
  private var process: Option[PtyProcess] = None
  private var currentRows: Int = 24
  private var currentCols: Int = 80
  
  def processExists: Boolean = process.isDefined
  
  private def buildCommand(): Array[String] = {
    shellConfig match {
      case LocalShell =>
        val shell = if (java.io.File("/bin/bash").exists()) "/bin/bash" else "/bin/sh"
        Array(shell, "-i")
        
      case RemoteShell(hostConfig) =>
        // Build SSH command: ssh -t [options] user@host shell
        val baseCmd = List("ssh", "-t")
        
        // Add port if not default
        val portOpt = if (hostConfig.port != 22) List("-p", hostConfig.port.toString) else List.empty
        
        // Add custom SSH options
        val customOpts = hostConfig.sshOptions.flatMap { opt =>
          if (opt.startsWith("-")) List(opt) else List.empty
        }
        
        // Build user@host
        val userHost = s"${hostConfig.username}@${hostConfig.hostname}"
        
        // Remote shell command (split by spaces)
        val remoteShell = hostConfig.shell.split("\\s+").toList
        
        // Combine: ssh -t [-p port] [custom opts] user@host shell
        (baseCmd ++ portOpt ++ customOpts ++ List(userHost) ++ remoteShell).toArray
    }
  }
  
  def start(rows: Int = 24, cols: Int = 80): Unit = {
    if (process.isEmpty) {
      println(s"[ShellProcess] Starting shell process for instance: $instanceId")
      try {
        currentRows = rows
        currentCols = cols
        
        val command = buildCommand()
        val commandStr = command.mkString(" ")
        println(s"[ShellProcess] Command: $commandStr")
        
        val environment = System.getenv().asScala.toMap.asJava
        val builder = new PtyProcessBuilder()
          .setCommand(command)
          .setDirectory(System.getProperty("user.home"))
          .setEnvironment(environment)
        
        val proc: PtyProcess = builder.start()
        proc.setWinSize(new WinSize(cols, rows))
        
        process = Some(proc)
        println(s"[ShellProcess] Shell process started: $instanceId")
        
        readStream(proc.getInputStream(), isError = false)
        monitorExit(proc)
        
      } catch {
        case e: Exception =>
          println(s"[ShellProcess] Exception starting shell: $e")
          e.printStackTrace()
          sendError(s"Failed to start shell: ${e.getMessage}")
      }
    }
  }
  
  // Rest of methods (sendInput, resizeTerminal, etc.) stay the same
  // Just need to update sendOutput/sendError to include instanceId
  
  private def sendOutput(text: String): Unit = {
    try {
      val message = upickle.default.write(ShellOutput(instanceId, text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendOutput: $e")
        e.printStackTrace()
    }
  }
  
  private def sendError(text: String): Unit = {
    try {
      val message = upickle.default.write(ShellError(instanceId, text))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendError: $e")
        e.printStackTrace()
    }
  }
  
  private def sendExit(code: Int): Unit = {
    try {
      val message = upickle.default.write(ShellExit(instanceId, code))
      outputActor ! org.apache.pekko.http.scaladsl.model.ws.TextMessage(message)
    } catch {
      case e: Exception =>
        println(s"[ShellProcess] Exception in sendExit: $e")
        e.printStackTrace()
    }
  }
  
  // ... rest of implementation (readStream, monitorExit, etc.)
}
```

## Example Usage

### Creating Local Shell
```scala
val localShell = new ShellProcess("local-1", outputActor, LocalShell)
localShell.start(24, 80)
// Command: /bin/bash -i
```

### Creating Remote Shell
```scala
val hostConfig = HostConfig(
  hostname = "server.example.com",
  username = "admin",
  port = 22,
  sshOptions = List("-i", "/path/to/key"),  // Optional
  shell = "bash -i"
)
val remoteShell = new ShellProcess("remote-1", outputActor, RemoteShell(hostConfig))
remoteShell.start(24, 80)
// Command: ssh -t -i /path/to/key admin@server.example.com bash -i
```

### Using SSH Config
```scala
// If ~/.ssh/config has:
// Host myserver
//   HostName server.example.com
//   User admin
//   IdentityFile /path/to/key
//
// Then use:
val hostConfig = HostConfig(
  hostname = "myserver",  // Use SSH config alias
  username = "admin",     // Can be overridden by SSH config
  sshOptions = List.empty  // SSH config handles auth
)
// Command: ssh -t myserver bash -i
```

## Benefits of This Approach

1. **No SSH Library Dependency:** Uses system SSH, no Java SSH library needed
2. **Leverages Existing Infrastructure:** Uses ~/.ssh/config, SSH keys, SSH agent
3. **Simpler Implementation:** Same PTY code for local and remote
4. **Flexible:** Can pass any SSH options via HostConfig
5. **Secure:** SSH handles authentication (keys, agent, config)
6. **Works Everywhere:** Any system with SSH installed

## SSH Authentication Options

Users can configure SSH access via:

1. **SSH Config File (~/.ssh/config):**
   ```
   Host myserver
     HostName server.example.com
     User admin
     IdentityFile ~/.ssh/id_rsa
     Port 2222
   ```
   Then use `hostname = "myserver"` in HostConfig

2. **SSH Keys:** Place keys in ~/.ssh/, SSH will use them automatically

3. **SSH Agent:** If ssh-agent is running, keys are used automatically

4. **Custom Options:** Via `sshOptions` in HostConfig:
   ```scala
   HostConfig(
     hostname = "server.example.com",
     username = "admin",
     sshOptions = List("-i", "/custom/path/to/key", "-o", "StrictHostKeyChecking=no")
   )
   ```

## Error Handling

SSH connection failures will appear as:
- `ShellError(instanceId, "Connection refused")`
- `ShellError(instanceId, "Permission denied")`
- `ShellExit(instanceId, 255)` (SSH exit code for connection failure)

Frontend can display these errors and allow retry.
