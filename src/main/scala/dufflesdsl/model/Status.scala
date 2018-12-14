package dufflesdsl.model

sealed trait Status
object Status {
  def apply(code: Int) = code match {
    case 0   => Ok
    case 1   => FileDiff
    case 2   => FileNotFound
    case 255 => ErrSSH
    case _   => ErrUnknown
  }
}
case object Ok extends Status
case object FileDiff extends Status
case object FileNotFound extends Status
case object ErrSSH extends Status
case object ErrUnknown extends Status
