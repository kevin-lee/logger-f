package loggerf.core.syntax

import loggerf.LogMessage
import loggerf.LogMessage.{LeveledMessage, NotIgnorable}
import loggerf.SourceLocationMacro
import loggerf.core.syntax.ExtraSyntax.Prefix

import scala.reflect.macros.blackbox

/** Macro implementations for the log message constructors of [[LogMessageSyntax]] and [[ExtraSyntax]] on Scala 2.
  *
  * On Scala 2 a method whose last parameter list is implicit cannot be followed by another argument list, so
  * `info("x")` against `def info(implicit sourceLocation: SourceLocation)` would pass `"x"` as the implicit
  * argument. Each constructor is therefore a macro which expands at the call site to the message object with the
  * call site's `SourceLocation` baked in.
  *
  * Runs on the JVM at macro expansion time only and is unreachable at run time, so it is safe on Scala.js and
  * Scala Native.
  *
  * @author Kevin Lee
  * @since 2026-09-20
  */
object LogMessageSyntaxMacro {

  type R = (String => LogMessage with NotIgnorable) with LeveledMessage.Leveled

  def debug(c: blackbox.Context): c.Expr[R] = {
    import c.universe._
    leveled(c)(q"_root_.loggerf.Level.debug")
  }

  def debugWithThrowable(c: blackbox.Context)(throwable: c.Expr[Throwable]): c.Expr[R] = {
    import c.universe._
    leveledWithThrowable(c)(q"_root_.loggerf.Level.debug", throwable)
  }

  def debugWithPrefix(c: blackbox.Context)(prefix: c.Expr[Prefix]): c.Expr[R] = {
    import c.universe._
    leveledWithPrefix(c)(q"_root_.loggerf.Level.debug", prefix)
  }

  def info(c: blackbox.Context): c.Expr[R] = {
    import c.universe._
    leveled(c)(q"_root_.loggerf.Level.info")
  }

  def infoWithThrowable(c: blackbox.Context)(throwable: c.Expr[Throwable]): c.Expr[R] = {
    import c.universe._
    leveledWithThrowable(c)(q"_root_.loggerf.Level.info", throwable)
  }

  def infoWithPrefix(c: blackbox.Context)(prefix: c.Expr[Prefix]): c.Expr[R] = {
    import c.universe._
    leveledWithPrefix(c)(q"_root_.loggerf.Level.info", prefix)
  }

  def warn(c: blackbox.Context): c.Expr[R] = {
    import c.universe._
    leveled(c)(q"_root_.loggerf.Level.warn")
  }

  def warnWithThrowable(c: blackbox.Context)(throwable: c.Expr[Throwable]): c.Expr[R] = {
    import c.universe._
    leveledWithThrowable(c)(q"_root_.loggerf.Level.warn", throwable)
  }

  def warnWithPrefix(c: blackbox.Context)(prefix: c.Expr[Prefix]): c.Expr[R] = {
    import c.universe._
    leveledWithPrefix(c)(q"_root_.loggerf.Level.warn", prefix)
  }

  def error(c: blackbox.Context): c.Expr[R] = {
    import c.universe._
    leveled(c)(q"_root_.loggerf.Level.error")
  }

  def errorWithThrowable(c: blackbox.Context)(throwable: c.Expr[Throwable]): c.Expr[R] = {
    import c.universe._
    leveledWithThrowable(c)(q"_root_.loggerf.Level.error", throwable)
  }

  def errorWithPrefix(c: blackbox.Context)(prefix: c.Expr[Prefix]): c.Expr[R] = {
    import c.universe._
    leveledWithPrefix(c)(q"_root_.loggerf.Level.error", prefix)
  }

  private def leveled(c: blackbox.Context)(level: c.Tree): c.Expr[R] = {
    import c.universe._
    val location = SourceLocationMacro.locationTree(c)
    c.Expr[R](
      q"_root_.loggerf.LogMessage.LeveledMessage.StringToLeveledMessage($level, $location)"
    )
  }

  private def leveledWithThrowable(c: blackbox.Context)(level: c.Tree, throwable: c.Expr[Throwable]): c.Expr[R] = {
    import c.universe._
    val location = SourceLocationMacro.locationTree(c)
    c.Expr[R](
      q"_root_.loggerf.LogMessage.LeveledMessage.StringToLeveledMessageWithThrowable($level, $throwable, $location)"
    )
  }

  private def leveledWithPrefix(c: blackbox.Context)(level: c.Tree, prefix: c.Expr[Prefix]): c.Expr[R] = {
    import c.universe._
    val location = SourceLocationMacro.locationTree(c)
    c.Expr[R](
      q"_root_.loggerf.LogMessage.LeveledMessage.PreprocessedStringToLeveledMessage($level, $prefix.value, $location)"
    )
  }
}
