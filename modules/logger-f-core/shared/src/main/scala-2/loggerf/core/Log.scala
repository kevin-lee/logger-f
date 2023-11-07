package loggerf.core

import effectie.core.FxCtor
import loggerf.LogMessage
import loggerf.LogMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.logger.CanLog

import scala.annotation.implicitNotFound

/** @author Kevin Lee
  * @since 2020-04-10
  */
@implicitNotFound(
  """
  Could not find an implicit Log[${F}].
  You can probably find it from the loggerf.instances package from logger-f-cats module.
  ---
    import loggerf.instances.cats._
  ---
  -----
  If this doesn't solve, you probably need a CanLog instance.
  To create it, please check out the following document.

  https://logger-f.kevinly.dev/docs/cats/import#canlog-logger

  -----
  If it doesn't solve, it's probably because of missing an Fx[F] instance.

  You can simply import the Fx[F] instance of your effect library.
  Please check out the message of @implicitNotFound annotation on effectie.core.Fx.

  """
)
trait Log[F[*]] {

  implicit def EF: FxCtor[F]
  def map0[A, B](fa: F[A])(f: A => B): F[B]

  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LogMessage with MaybeIgnorable): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LogMessage.LeveledMessage(message, level) =>
          flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(a))
        case LogMessage.Ignore =>
          EF.pureOf(a)
      }
    }

  def log_[A](fa: F[A])(toLeveledMessage: A => LogMessage with MaybeIgnorable): F[Unit] =
    map0(log(fa)(toLeveledMessage))(_ => ())

  def logS(
    message: => String
  )(toLeveledMessage: (String => LogMessage with NotIgnorable) with LogMessage.LeveledMessage.Leveled): F[String] =
    toLeveledMessage.toLazyInput(message) match {
      case LogMessage.LeveledMessage(msg, level) =>
        map0(EF.effectOf(canLog.getLogger(level)(msg())))(_ => message)
    }

  def logS_(
    message: => String
  )(toLeveledMessage: (String => LogMessage with NotIgnorable) with LogMessage.LeveledMessage.Leveled): F[Unit] =
    toLeveledMessage.toLazyInput(message) match {
      case LogMessage.LeveledMessage(msg, level) =>
        EF.effectOf(canLog.getLogger(level)(msg()))
    }

  def log[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): F[Option[A]] =
    flatMap0(foa) {
      case None =>
        ifEmpty match {
          case LogMessage.Ignore =>
            EF.pureOf(None)

          case LogMessage.LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LogMessage.LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Some(a)))

          case LogMessage.Ignore =>
            EF.pureOf(Some(a))
        }
    }

  def log_[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LogMessage with MaybeIgnorable,
    toLeveledMessage: A => LogMessage with MaybeIgnorable,
  ): F[Unit] =
    map0(log(foa)(ifEmpty, toLeveledMessage))(_ => ())

  def log[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): F[Either[A, B]] =
    flatMap0(feab) {
      case Left(l) =>
        leftToMessage(l) match {
          case LogMessage.LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Left(l)))

          case LogMessage.Ignore =>
            EF.pureOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LogMessage.LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Right(r)))

          case LogMessage.Ignore =>
            EF.pureOf(Right(r))
        }
    }

  def log_[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LogMessage with MaybeIgnorable,
    rightToMessage: B => LogMessage with MaybeIgnorable,
  ): F[Unit] =
    map0(log(feab)(leftToMessage, rightToMessage))(_ => ())

}

object Log {

  def apply[F[*]: Log]: Log[F] = implicitly[Log[F]]

}
