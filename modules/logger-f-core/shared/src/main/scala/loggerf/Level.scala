package loggerf

/** @author Kevin Lee
  * @since 2020-04-10
  */
sealed trait Level

object Level {

  case object Debug extends Level
  case object Info extends Level
  case object Warn extends Level
  case object Error extends Level

  def debug: Level = Debug

  def info: Level = Info

  def warn: Level = Warn

  def error: Level = Error
}
