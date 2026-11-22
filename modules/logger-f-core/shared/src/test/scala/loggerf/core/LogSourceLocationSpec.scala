package loggerf.core

import cats.syntax.all._
import effectie.core.FxCtor
import hedgehog._
import hedgehog.runner._
import loggerf.core.LogForTesting.{FxCtorForTesting, Identity}
import loggerf.core.syntax.all._
import loggerf.logger.CanLog
import loggerf.{Level, SourceLocation}

/** Verifies that the source location captured by the message constructors reaches `CanLog` through `Log[F]`.
  *
  * @author Kevin Lee
  * @since 2026-09-20
  */
object LogSourceLocationSpec extends Properties {
  override def tests: List[Test] = List(
    example("log(fa)(info) records the info line", testBareConstructor),
    example("log(fa)(a => info(...)) records the info line", testAppliedConstructor),
    example("log(Either) records each branch's own line", testEitherBranches),
    example("logS(message)(warn) records the warn line", testLogS),
    example("log(fa)(debugA) records the debugA line", testDebugA),
    example("throwable constructor records through the throwable overload", testThrowable),
    example("ignore records nothing", testIgnore),
  )

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final class SourceLocationRecorder extends CanLog {
    @volatile private var _records: Vector[(Level, String, Option[SourceLocation])] = // scalafix:ok DisableSyntax.var
      Vector.empty

    def records: Vector[(Level, String, Option[SourceLocation])] = _records

    override def debug(message: => String): Unit = _records = _records :+ ((Level.debug, message, none[SourceLocation]))

    override def debug(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.debug, message, none[SourceLocation]))

    override def info(message: => String): Unit = _records = _records :+ ((Level.info, message, none[SourceLocation]))

    override def info(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.info, message, none[SourceLocation]))

    override def warn(message: => String): Unit = _records = _records :+ ((Level.warn, message, none[SourceLocation]))

    override def warn(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.warn, message, none[SourceLocation]))

    override def error(message: => String): Unit = _records = _records :+ ((Level.error, message, none[SourceLocation]))

    override def error(throwable: Throwable)(message: => String): Unit =
      _records = _records :+ ((Level.error, message, none[SourceLocation]))

    override def debug(sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.debug, message, sourceLocation.some))

    override def debug(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.debug, message, sourceLocation.some))

    override def info(sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.info, message, sourceLocation.some))

    override def info(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.info, message, sourceLocation.some))

    override def warn(sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.warn, message, sourceLocation.some))

    override def warn(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.warn, message, sourceLocation.some))

    override def error(sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.error, message, sourceLocation.some))

    override def error(throwable: Throwable, sourceLocation: SourceLocation)(message: => String): Unit =
      _records = _records :+ ((Level.error, message, sourceLocation.some))
  }

  final case class LogWithRecorder(recorder: SourceLocationRecorder) extends Log[Identity] {
    override implicit val EF: FxCtor[Identity] = FxCtorForTesting

    @inline override def map0[A, B](fa: Identity[A])(f: A => B): Identity[B] = f(fa)

    @inline override def flatMap0[A, B](fa: Identity[A])(f: A => Identity[B]): Identity[B] = f(fa)

    override def canLog: CanLog = recorder
  }

  private def recordedLines(recorder: SourceLocationRecorder): Vector[Option[Int]] =
    recorder.records.map { case (_, _, location) => location.map(_.line) }

  def testBareConstructor: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    // format: off
    val here: SourceLocation = implicitly[SourceLocation]
    val _ = log[Identity, String]("x")(info)
    // format: on
    Result.all(
      List(
        recorder.records.map { case (level, message, _) => (level, message) } ==== Vector((Level.info, "x")),
        recordedLines(recorder) ==== Vector(Some(here.line + 1)),
      )
    )
  }

  def testAppliedConstructor: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    // format: off
    val here: SourceLocation = implicitly[SourceLocation]
    val _ = log[Identity, String]("x")(a => info(s"m $a"))
    // format: on
    Result.all(
      List(
        recorder.records.map { case (level, message, _) => (level, message) } ==== Vector((Level.info, "m x")),
        recordedLines(recorder) ==== Vector(Some(here.line + 1)),
      )
    )
  }

  def testEitherBranches: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    // format: off
    val here: SourceLocation = implicitly[SourceLocation]
    val right = log[Identity, String, String]("r".asRight[String])(
      err => error(s"e $err"),
      r => info(s"r $r"),
    )
    val left = log[Identity, String, String]("l".asLeft[String])(
      err => error(s"e $err"),
      r => info(s"r $r"),
    )
    // format: on
    Result.all(
      List(
        right ==== Right("r"),
        left ==== Left("l"),
        recorder.records.map { case (level, message, _) => (level, message) } ====
          Vector((Level.info, "r r"), (Level.error, "e l")),
        recordedLines(recorder) ==== Vector(Some(here.line + 3), Some(here.line + 6)),
      )
    )
  }

  def testLogS: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    // format: off
    val here: SourceLocation = implicitly[SourceLocation]
    val _ = Log[Identity].logS("x")(warn)
    // format: on
    Result.all(
      List(
        recorder.records.map { case (level, message, _) => (level, message) } ==== Vector((Level.warn, "x")),
        recordedLines(recorder) ==== Vector(Some(here.line + 1)),
      )
    )
  }

  def testDebugA: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    // format: off
    val here: SourceLocation = implicitly[SourceLocation]
    val _ = log[Identity, String]("x")(debugA)
    // format: on
    Result.all(
      List(
        recorder.records.map { case (level, message, _) => (level, message) } ==== Vector((Level.debug, "x")),
        recordedLines(recorder) ==== Vector(Some(here.line + 1)),
      )
    )
  }

  def testThrowable: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    // format: off
    val here: SourceLocation = implicitly[SourceLocation]
    val _ = log[Identity, String]("x")(a => error(new RuntimeException("t"))(a))
    // format: on
    Result.all(
      List(
        recorder.records.map { case (level, message, _) => (level, message) } ==== Vector((Level.error, "x")),
        recordedLines(recorder) ==== Vector(Some(here.line + 1)),
      )
    )
  }

  def testIgnore: Result = {
    val recorder                   = new SourceLocationRecorder
    implicit val lg: Log[Identity] = LogWithRecorder(recorder)
    val _                          = log[Identity, String]("x")(ignoreA)
    recorder.records ==== Vector.empty
  }

}
