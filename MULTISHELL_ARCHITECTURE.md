# Multishell Architecture: Multi-Instance & Remote Host Support

## Message Flow Diagram

```
Frontend (Browser)
  │
  ├─> WebSocket Connection
  │     │
  │     ├─> CreateShell("local-1", None)
  │     ├─> CreateShell("remote-1", Some(HostConfig("host1.example.com", ...)))
  │     ├─> CreateShell("remote-2", Some(HostConfig("host2.example.com", ...)))
  │     │
  │     ├─> BroadcastInput("ls -la")  ──┐
  │     │                                │
  │     └─> ShellInput("remote-1", "pwd") │
  │                                        │
  Backend (MultishellWSActor)              │
    │                                       │
    └─> ShellManager                        │
          │                                  │
          ├─> ShellProcess("local-1", LocalShell)  │
          │     └─> PTY: /bin/bash -i              │
          │                                          │
          ├─> ShellProcess("remote-1", RemoteShell)│
          │     └─> PTY: ssh -t user@host1 bash -i │
          │                                          │
          └─> ShellProcess("remote-2", RemoteShell) │
                └─> PTY: ssh -t user@host2 bash -i │
                                            │
          BroadcastInput("ls -la") ─────────┘
            ├─> local-1.sendInput("ls -la")
            ├─> remote-1.sendInput("ls -la")
            └─> remote-2.sendInput("ls -la")
```

## Component Structure

```
multishell/
├── protocol/
│   └── shared/src/main/scala/
│       └── Protocol.scala          # Extended with instance IDs & host config
│
└── backend/services/multishell/
    └── src/main/scala/
        ├── MultishellService.scala  # HTTP routes + WebSocket
        ├── MultishellWSActor.scala  # WebSocket handler (updated)
        ├── ShellManager.scala       # NEW: Manages multiple instances
        ├── ShellProcess.scala       # Refactored: supports local & remote via command config
        └── HostManager.scala        # NEW: Manages host configurations (optional)
```

## Data Flow: Command Mirroring

```
User types "ls -la" in UI (broadcast mode ON)
  │
  ├─> Frontend: MultishellSocket.broadcastInput("ls -la")
  │     │
  │     └─> WebSocket.send(BroadcastInput("ls -la"))
  │
  Backend: MultishellWSActor receives BroadcastInput
    │
    └─> ShellManager.broadcastInput("ls -la")
          │
          ├─> ShellProcess("local-1").sendInput("ls -la")
          │     └─> PTY (/bin/bash) receives input
          │           └─> Output: "file1.txt file2.txt ..."
          │                 └─> ShellOutput("local-1", "...")
          │                       └─> WebSocket → Frontend
          │
          ├─> ShellProcess("remote-1").sendInput("ls -la")
          │     └─> PTY (ssh -t user@host1 bash) receives input
          │           └─> SSH forwards to remote bash
          │                 └─> Output: "fileA.txt fileB.txt ..."
          │                       └─> ShellOutput("remote-1", "...")
          │                             └─> WebSocket → Frontend
          │
          └─> ShellProcess("remote-2").sendInput("ls -la")
                └─> PTY (ssh -t user@host2 bash) receives input
                      └─> SSH forwards to remote bash
                            └─> Output: "fileX.txt fileY.txt ..."
                                  └─> ShellOutput("remote-2", "...")
                                        └─> WebSocket → Frontend

Frontend receives multiple ShellOutput messages
  │
  ├─> ShellOutput("local-1", "...") → Terminal "local-1" displays
  ├─> ShellOutput("remote-1", "...") → Terminal "remote-1" displays
  └─> ShellOutput("remote-2", "...") → Terminal "remote-2" displays
```

## Frontend UI Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Multishell - Cluster Management                            │
├──────────────┬──────────────────────────────────────────────┤
│              │  [Tab: local-1] [Tab: remote-1] [Tab: remote-2]│
│  Instances   │  ┌──────────────────────────────────────────┐│
│              │  │                                            ││
│  ✓ local-1   │  │  Terminal Output (selected instance)      ││
│  ✓ remote-1  │  │                                            ││
│  ✓ remote-2  │  │  $ ls -la                                  ││
│              │  │  file1.txt  file2.txt                      ││
│  [+ Add]     │  │                                            ││
│              │  └──────────────────────────────────────────┘│
│  Hosts       │  ┌──────────────────────────────────────────┐│
│              │  │  [Broadcast Mode: ON]  [Resize All]      ││
│  host1.com   │  │                                            ││
│  host2.com   │  │  Command Input: [________________] [Send]││
│              │  └──────────────────────────────────────────┘│
│  [+ Add Host]│                                               │
└──────────────┴──────────────────────────────────────────────┘
```

## Key Design Decisions

### 1. Instance ID Strategy
- **Format:** `"local-{n}"` for local, `"remote-{hostname}-{n}"` for remote
- **Uniqueness:** Enforced by ShellManager
- **Persistence:** Instance IDs are session-scoped (not persisted)

### 2. Backward Compatibility
- Messages without `instanceId` default to `"default"` instance
- Single-instance mode still works
- Gradual migration path for existing code

### 3. SSH Implementation
- **System SSH:** Use `ssh` command-line tool via PTY
- **Benefits:** No Java library dependency, uses existing SSH infrastructure
- **Authentication:** Handled by system SSH (~/.ssh/config, keys, agent)
- **Command:** `ssh -t user@hostname bash -i` (or custom shell)

### 4. Host Configuration Storage
- **Phase 1:** In-memory (per WebSocket session)
- **Phase 2:** Per-user storage (database/file)
- **Phase 3:** Shared host registry (optional)

### 5. Error Handling
- Connection failures: Mark instance as error, allow retry
- SSH auth failures: Return error message, don't create instance
- Network timeouts: Configurable timeout, graceful degradation

## Implementation Checklist

### Backend
- [ ] Extend Protocol.scala with instance IDs and host config
- [ ] Create ShellProcess trait
- [ ] Refactor ShellProcess → LocalShellProcess
- [ ] Create RemoteShellProcess (SSH implementation)
- [ ] Create ShellManager
- [ ] Update MultishellWSActor to use ShellManager
- [ ] Add host management API routes
- [ ] Add SSH library dependency
- [ ] Handle backward compatibility

### Frontend
- [ ] Update Protocol.scala (shared)
- [ ] Extend MultishellSocket with instance management
- [ ] Create MultiTerminal component
- [ ] Create InstanceList component
- [ ] Create TerminalTabs component
- [ ] Create HostConfigPanel component
- [ ] Add broadcast mode UI controls
- [ ] Update XTerm component to support instance IDs

### Testing
- [ ] Unit tests for ShellManager
- [ ] Unit tests for RemoteShellProcess
- [ ] Integration tests for multi-instance
- [ ] E2E tests for command mirroring
- [ ] SSH connection tests
