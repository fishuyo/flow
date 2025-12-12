# Multishell Extension Plan: Multiple Instances & Remote Host Mirroring

## Overview

Extend the multishell service to support:
1. **Multiple shell instances** per WebSocket connection
2. **Remote host support** via SSH
3. **Command mirroring** - broadcast commands to multiple shells simultaneously
4. **Cluster management** - configure and manage groups of remote hosts
5. **Web UI** for managing hosts, instances, and viewing multiple terminals

## Current Architecture

```
WebSocket Connection
  └─> MultishellWSActor
       └─> ShellProcess (single local PTY)
```

## Target Architecture

```
WebSocket Connection
  └─> MultishellWSActor
       └─> ShellManager
            ├─> ShellProcess(id: "local-1", config: LocalShell)
            │     └─> PTY: /bin/bash -i
            │
            ├─> ShellProcess(id: "remote-1", config: RemoteShell(host1))
            │     └─> PTY: ssh -t user@host1 bash -i
            │
            ├─> ShellProcess(id: "remote-2", config: RemoteShell(host2))
            │     └─> PTY: ssh -t user@host2 bash -i
            │
            └─> ShellProcess(id: "remote-3", config: RemoteShell(host3))
                  └─> PTY: ssh -t user@host3 bash -i
```

## Changes Required

### 1. Protocol Extensions (`protocol/multishell/shared/src/main/scala/Protocol.scala`)

**Add instance identification to all messages:**
```scala
sealed trait Message derives ReadWriter

// Instance management
final case class CreateShell(instanceId: String, host: Option[HostConfig]) extends Message
final case class DestroyShell(instanceId: String) extends Message
final case class ListShells() extends Message
final case class ShellsList(instances: List[ShellInstanceInfo]) extends Message

// Instance-specific messages (add instanceId)
final case class ShellInput(instanceId: String, data: String) extends Message
final case class ShellResize(instanceId: String, rows: Int, cols: Int) extends Message
final case class ShellOutput(instanceId: String, text: String) extends Message
final case class ShellError(instanceId: String, text: String) extends Message
final case class ShellExit(instanceId: String, code: Int) extends Message

// Broadcast messages (no instanceId = broadcast to all)
final case class BroadcastInput(data: String) extends Message
final case class BroadcastResize(rows: Int, cols: Int) extends Message

// Host configuration
final case class HostConfig(
  hostname: String,
  username: String,
  port: Int = 22,
  sshOptions: List[String] = List.empty,  // Custom SSH options (e.g., "-i /path/to/key")
  shell: String = "bash -i",  // Remote shell command to execute
  displayName: Option[String] = None  // Optional friendly name for UI
) derives ReadWriter

// Note: SSH authentication handled by system SSH:
// - Uses ~/.ssh/config if available
// - Uses SSH keys from ~/.ssh/
// - Uses SSH agent if running
// - Can specify key via sshOptions: List("-i", "/path/to/key")

final case class ShellInstanceInfo(
  instanceId: String,
  hostname: String,
  isLocal: Boolean,
  status: ShellStatus,
  createdAt: Long
) derives ReadWriter

sealed trait ShellStatus derives ReadWriter
case object ShellRunning extends ShellStatus
case object ShellStopped extends ShellStatus
case object ShellError extends ShellStatus
```

**Backward compatibility:** Support messages without `instanceId` for single-instance mode (default to "default" instance).

### 2. Backend: Shell Process Abstraction

**Refactor ShellProcess to support both local and remote via command:**
```scala
// ShellProcess.scala - refactor to accept command configuration
class ShellProcess(
  val instanceId: String,
  outputActor: ActorRef,
  shellConfig: ShellConfig  // NEW: configures local vs remote
)(implicit ec: ExecutionContext) {
  
  // ShellConfig determines the command to run
  sealed trait ShellConfig
  case object LocalShell extends ShellConfig  // Uses /bin/bash or /bin/sh
  case class RemoteShell(hostConfig: HostConfig) extends ShellConfig  // Uses ssh command
  
  // Build command array based on config
  private def buildCommand(): Array[String] = {
    shellConfig match {
      case LocalShell =>
        val shell = if (java.io.File("/bin/bash").exists()) "/bin/bash" else "/bin/sh"
        Array(shell, "-i")
        
      case RemoteShell(hostConfig) =>
        // Build SSH command: ssh user@host -t bash -i
        val sshCmd = Array(
          "ssh",
          "-t",  // Force pseudo-terminal allocation
          "-o", "StrictHostKeyChecking=no",  // Auto-accept host keys (or make configurable)
          s"${hostConfig.username}@${hostConfig.hostname}",
          "bash", "-i"  // Or use hostConfig.shell if specified
        )
        sshCmd
    }
  }
  
  // Rest of implementation stays the same - just use buildCommand() instead of hardcoded shell
}
```

**Simplified approach:** No separate RemoteShellProcess class needed! Just configure the command differently.

### 3. Backend: Shell Manager

**Create ShellManager to manage multiple instances:**
```scala
// ShellManager.scala
class ShellManager(outputActor: ActorRef)(implicit ec: ExecutionContext) {
  private var shells: Map[String, ShellProcess] = Map.empty
  
  def createShell(instanceId: String, hostConfig: Option[HostConfig]): ShellProcess = {
    val shellConfig = hostConfig match {
      case Some(config) => ShellConfig.RemoteShell(config)
      case None => ShellConfig.LocalShell
    }
    val shell = new ShellProcess(instanceId, outputActor, shellConfig)
    shells = shells + (instanceId -> shell)
    shell.start()  // Start the shell process
    shell
  }
  
  def getShell(instanceId: String): Option[ShellProcess] = shells.get(instanceId)
  
  def destroyShell(instanceId: String): Unit = {
    shells.get(instanceId).foreach(_.stop())
    shells = shells - instanceId
  }
  
  def listShells(): List[ShellInstanceInfo] = {
    shells.values.map(_.getInfo()).toList
  }
  
  def broadcastInput(data: String): Unit = {
    shells.values.foreach(_.sendInput(data))
  }
  
  def broadcastResize(rows: Int, cols: Int): Unit = {
    shells.values.foreach(_.resizeTerminal(rows, cols))
  }
  
  def stopAll(): Unit = {
    shells.values.foreach(_.stop())
    shells = Map.empty
  }
}
```

### 4. Backend: MultishellWSActor Updates

**Update to use ShellManager:**
```scala
class MultishellWSActor(out: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  private val shellManager = new ShellManager(out)
  
  def receive = {
    case TextMessage.Strict(msg) =>
      val message = upickle.default.read[Message](msg)
      message match {
        case CreateShell(instanceId, hostConfig) =>
          shellManager.createShell(instanceId, hostConfig)
          // Send confirmation
          
        case DestroyShell(instanceId) =>
          shellManager.destroyShell(instanceId)
          
        case ListShells() =>
          val instances = shellManager.listShells()
          out ! TextMessage(upickle.default.write(ShellsList(instances)))
          
        case ShellInput(instanceId, data) =>
          shellManager.getShell(instanceId).foreach(_.sendInput(data))
          
        case BroadcastInput(data) =>
          shellManager.broadcastInput(data)
          
        case ShellResize(instanceId, rows, cols) =>
          shellManager.getShell(instanceId).foreach(_.resizeTerminal(rows, cols))
          
        case BroadcastResize(rows, cols) =>
          shellManager.broadcastResize(rows, cols)
          
        // Backward compatibility: messages without instanceId
        case oldMsg: ShellInput if oldMsg.instanceId == null =>
          // Handle as broadcast or default instance
      }
  }
}
```

### 5. Backend: SSH Implementation (Simplified)

**No SSH library needed!** Use system SSH command via PTY:
- SSH command: `ssh -t user@hostname bash -i`
- PTY handles the SSH connection just like a local shell
- Input/output forwarding works automatically
- SSH authentication handled by system SSH (uses ~/.ssh/config, keys, etc.)
- Can pass SSH options via HostConfig if needed

**HostConfig simplified:**
```scala
final case class HostConfig(
  hostname: String,
  username: String,
  port: Int = 22,
  sshOptions: List[String] = List.empty,  // Custom SSH options if needed
  shell: String = "bash -i"  // Remote shell command
) derives ReadWriter
```

**Benefits:**
- No Java SSH library dependency
- Uses system SSH (supports all auth methods: keys, agent, config, etc.)
- Simpler implementation
- Leverages existing SSH infrastructure

### 6. Frontend: Protocol Updates

**Update frontend to handle instance IDs:**
```scala
// MultishellSocket.scala - add instance management methods
object MultishellSocket {
  def createShell(instanceId: String, hostConfig: Option[HostConfig]): Unit
  def destroyShell(instanceId: String): Unit
  def listShells(): Unit
  def sendInput(instanceId: String, data: String): Unit
  def broadcastInput(data: String): Unit
  def sendResize(instanceId: String, rows: Int, cols: Int): Unit
  def broadcastResize(rows: Int, cols: Int): Unit
}
```

### 7. Frontend: Multi-Terminal UI

**Create new components:**
```scala
// MultiTerminal.scala - container for multiple terminals
object MultiTerminal {
  def apply() = {
    val instances = Var[List[ShellInstance]](List.empty)
    val selectedInstance = Var[Option[String]](None)
    
    div(
      // Instance list sidebar
      InstanceList(instances, selectedInstance),
      // Terminal tabs/view
      TerminalTabs(instances, selectedInstance),
      // Host configuration panel
      HostConfigPanel()
    )
  }
}

// InstanceList.scala - shows list of shell instances
// TerminalTabs.scala - shows terminals in tabs or split view
// HostConfigPanel.scala - UI for adding/editing remote hosts
```

**Features:**
- Sidebar showing all shell instances (local + remote)
- Tabbed or split-pane view for multiple terminals
- Host configuration dialog (add/edit/delete remote hosts)
- Broadcast mode toggle (send to all vs selected)
- Instance status indicators (connected/disconnected/error)
- Ability to create/destroy instances dynamically

### 8. API Routes for Host Management

**Add REST endpoints:**
```scala
// MultishellService.scala - add routes
val route = {
  pathPrefix("multishell") {
    path("ws") => handleWebSocketMessages(wsFlow) ~
    path("hosts") {
      get => complete(listHosts()) ~
      post => entity(as[HostConfig]) { config => complete(createHost(config)) }
    } ~
    path("hosts" / Segment) { hostId =>
      get => complete(getHost(hostId)) ~
      put => entity(as[HostConfig]) { config => complete(updateHost(hostId, config)) } ~
      delete => complete(deleteHost(hostId))
    }
  }
}
```

## Implementation Phases

### Phase 1: Multi-Instance Support (Local Only)
1. ✅ Extend protocol with instance IDs
2. ✅ Refactor ShellProcess to trait
3. ✅ Create ShellManager
4. ✅ Update MultishellWSActor to use ShellManager
5. ✅ Update frontend to support instance IDs
6. ✅ Test with multiple local instances

### Phase 2: Remote Host Support
1. ✅ Add HostConfig to protocol
2. ✅ Refactor ShellProcess to accept ShellConfig
3. ✅ Build SSH command from HostConfig
4. ✅ Update ShellManager to support remote shells
5. ✅ Add host management API routes
6. ✅ Test SSH connections (using system SSH)

### Phase 3: Command Mirroring
1. ✅ Add BroadcastInput/BroadcastResize messages
2. ✅ Implement broadcast methods in ShellManager
3. ✅ Add broadcast UI controls
4. ✅ Test command mirroring across instances

### Phase 4: Web UI Enhancements
1. ✅ Create MultiTerminal component
2. ✅ Add host configuration UI
3. ✅ Add instance management UI
4. ✅ Add status indicators and error handling
5. ✅ Polish UX for cluster management

## Migration Strategy

**Backward Compatibility:**
- Support messages without `instanceId` (default to "default" instance)
- Single-instance mode works as before
- Gradually migrate to multi-instance API

**Data Model:**
- Host configurations stored in memory (can add persistence later)
- Shell instances are ephemeral (created/destroyed per session)
- Consider adding persistence for host configs (database/file)

## Security Considerations

1. **SSH Credentials:**
   - Never store passwords in plain text
   - Support key-based auth (preferred)
   - Consider credential vault integration

2. **Access Control:**
   - Validate host configurations
   - Rate limit shell creation
   - Monitor for abuse

3. **Network Security:**
   - Validate SSH connections
   - Handle connection failures gracefully
   - Timeout handling for stuck connections

## Testing Strategy

1. **Unit Tests:**
   - ShellManager instance management
   - Protocol serialization/deserialization
   - RemoteShellProcess SSH connection

2. **Integration Tests:**
   - Multiple instances per connection
   - Command broadcasting
   - Host configuration management

3. **E2E Tests:**
   - Web UI flow for creating/managing instances
   - Command mirroring across multiple hosts
   - Error handling and recovery

## Future Enhancements

1. **Persistence:**
   - Save host configurations to database
   - Session history/playback
   - Command logging per instance

2. **Advanced Features:**
   - Shell groups/tags for organization
   - Scheduled commands
   - Output diff/comparison between instances
   - File transfer support

3. **Performance:**
   - Connection pooling for SSH
   - Output buffering/batching
   - Compression for large outputs
