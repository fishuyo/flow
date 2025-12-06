package multishell
package protocol

import upickle.default.ReadWriter

sealed trait Message derives ReadWriter

final case class ShellInput(data: String) extends Message  // Raw input data (characters/bytes)
final case class ShellResize(rows: Int, cols: Int) extends Message  // Terminal size change
final case class ShellOutput(text: String) extends Message  // Output text (preserves ANSI codes)
final case class ShellError(text: String) extends Message
final case class ShellExit(code: Int) extends Message

