package multishell
package protocol

import upickle.default.ReadWriter

sealed trait Message derives ReadWriter

final case class ShellInput(command: String) extends Message
final case class ShellOutput(text: String) extends Message
final case class ShellError(text: String) extends Message
final case class ShellExit(code: Int) extends Message

