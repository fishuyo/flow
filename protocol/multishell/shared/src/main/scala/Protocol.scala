package multishell
package protocol

import upickle.default.ReadWriter

sealed trait Message derives ReadWriter

// Instance management
final case class CreateShell(instanceId: String, hostConfig: Option[HostConfig]) extends Message
final case class DestroyShell(instanceId: String) extends Message
final case class ListShells() extends Message
final case class ShellsList(instances: List[ShellInstanceInfo]) extends Message

// Instance-specific messages (instanceId required)
final case class ShellInput(instanceId: String, data: String) extends Message  // Raw input data (characters/bytes)
final case class ShellResize(instanceId: String, rows: Int, cols: Int) extends Message  // Terminal size change
final case class ShellOutput(instanceId: String, text: String) extends Message  // Output text (preserves ANSI codes)
final case class ShellError(instanceId: String, text: String) extends Message
final case class ShellExit(instanceId: String, code: Int) extends Message

// Broadcast messages (no instanceId = broadcast to all)
final case class BroadcastInput(data: String) extends Message
final case class BroadcastResize(rows: Int, cols: Int) extends Message

// Host configuration for remote shells
final case class HostConfig(
  host: String,
  user: Option[String] = None,
  port: Option[Int] = None,
  identityFile: Option[String] = None
) derives ReadWriter

// Shell instance information
final case class ShellInstanceInfo(
  instanceId: String,
  host: String,
  status: ShellStatus
) derives ReadWriter

sealed trait ShellStatus derives ReadWriter
case object ShellStatusRunning extends ShellStatus
case object ShellStatusStopped extends ShellStatus

