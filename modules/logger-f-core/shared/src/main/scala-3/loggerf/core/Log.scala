package loggerf.core

import effectie.core.FxCtor
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.logger.CanLog

import scala.annotation.implicitNotFound

/** @author Kevin Lee
  * @since 2022-02-09
  */
@implicitNotFound(
  """
  Could not find an implicit Log[${F}].
  If you add [cats](https://typelevel.org/cats) library to your project,
  you can automatically get `Log[${F}]` instance provided by logger-f.

  Add
  ---
    "org.typelevel" %% "cats-core" % CATS_VERSION
  ---
  to build.sbt.

  Or you can use your own instance of `Log[${F}]` by importing yours.
  -----
  If it doesn't solve, it's probably because of missing an Fx[${F}] instance.

  You can simply import the Fx[${F}] instance of your effect library.
  Please check out the message of @implicitNotFound annotation on `effectie.core.Fx`.
  Or please check out the following document.

    https://logger-f.kevinly.dev/docs/cats/import/#instances-for-effectie

    NOTE: You only need it once in the main method. In all other places,
    you can simply use the context bound with `Fx`.

    e.g.)
      def foo[F[*]: Fx: Log]: F[A] =
        ...
        fa.log(a => info(s"The result is $a)"))
        ...
  -----
  If this doesn't solve, you probably need a `CanLog` instance.
  To create it, please check out the following document.

    https://logger-f.kevinly.dev/docs/cats/import#canlog-logger

    NOTE: You only need it once in the main method. In all other places,
    you should not see it unless you want to use a different logger per each type.
  -----
  """
)
trait Log[F[*]] {

  given EF: FxCtor[F]

  def map0[A, B](fa: F[A])(f: A => B): F[B]

  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage | Ignore.type): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage(message, None, level) =>
          flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(a))

        case LeveledMessage(message, Some(throwable), level) =>
          flatMap0(EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(message())))(_ => EF.pureOf(a))

        case Ignore =>
          EF.pureOf(a)
      }
    }

  def log_[A](fa: F[A])(toLeveledMessage: A => LeveledMessage | Ignore.type): F[Unit] =
    map0(log(fa)(toLeveledMessage))(_ => ())

  def logS(message: => String)(toLeveledMessage: (String => LeveledMessage) with LeveledMessage.Leveled): F[String] =
    toLeveledMessage.toLazyInput(message) match {
      case LeveledMessage(msg, None, level) =>
        lazy val messageString = msg()
        map0(EF.effectOf(canLog.getLogger(level)(messageString)))(_ => messageString)

      case LeveledMessage(msg, Some(throwable), level) =>
        lazy val messageString = msg()
        map0(EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(messageString)))(_ => messageString)
    }

  def logS_(message: => String)(toLeveledMessage: (String => LeveledMessage) with LeveledMessage.Leveled): F[Unit] =
    toLeveledMessage.toLazyInput(message) match {
      case LeveledMessage(msg, None, level) =>
        EF.effectOf(canLog.getLogger(level)(msg()))

      case LeveledMessage(msg, Some(throwable), level) =>
        EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(msg()))
    }

  def log[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignore.type,
    toLeveledMessage: A => LeveledMessage | Ignore.type,
  ): F[Option[A]] =
    flatMap0(foa) {
      case None =>
        ifEmpty match {
          case Ignore =>
            EF.pureOf(None)

          case LeveledMessage(message, None, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(None))

          case LeveledMessage(message, Some(throwable), level) =>
            flatMap0(EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(message())))(_ => EF.pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage(message, None, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Some(a)))

          case LeveledMessage(message, Some(throwable), level) =>
            flatMap0(EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(message())))(_ => EF.pureOf(Some(a)))

          case Ignore =>
            EF.pureOf(Some(a))
        }
    }

  def log_[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignore.type,
    toLeveledMessage: A => LeveledMessage | Ignore.type,
  ): F[Unit] =
    map0(log(foa)(ifEmpty, toLeveledMessage))(_ => ())

  def log[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignore.type,
    rightToMessage: B => LeveledMessage | Ignore.type,
  ): F[Either[A, B]] =
    flatMap0(feab) {
      case Left(l) =>
        leftToMessage(l) match {
          case LeveledMessage(message, None, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Left(l)))

          case LeveledMessage(message, Some(throwable), level) =>
            flatMap0(EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(message())))(_ => EF.pureOf(Left(l)))

          case Ignore =>
            EF.pureOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage(message, None, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Right(r)))

          case LeveledMessage(message, Some(throwable), level) =>
            flatMap0(EF.effectOf(canLog.getLoggerWithThrowable(level)(throwable)(message())))(_ => EF.pureOf(Right(r)))

          case Ignore =>
            EF.pureOf(Right(r))
        }
    }

  def log_[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignore.type,
    rightToMessage: B => LeveledMessage | Ignore.type,
  ): F[Unit] =
    map0(log(feab)(leftToMessage, rightToMessage))(_ => ())

}
object Log extends orphan.OrphanCats {

  def apply[F[*]: Log]: Log[F] = summon[Log[F]]

  given logF[F[*], M[*[*]]: CatsMonad](using EF0: FxCtor[F], canLog0: CanLog, MF0: M[F]): Log[F] with {
    override val EF: FxCtor[F]  = EF0
    override val canLog: CanLog = canLog0

    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    private val MF: cats.Monad[F] = MF0.asInstanceOf[cats.Monad[F]] // scalafix:ok DisableSyntax.asInstanceOf

    inline override def map0[A, B](fa: F[A])(f: A => B): F[B] = MF.map(fa)(f)

    inline override def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B] = MF.flatMap(fa)(f)
  }

}
