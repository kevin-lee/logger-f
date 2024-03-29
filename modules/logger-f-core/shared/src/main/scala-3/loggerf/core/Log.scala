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
  You can probably find it from the loggerf.instances package from logger-f-cats module.
  ---
    import loggerf.instances.cats.given
    // or
    import loggerf.instances.cats.logF
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

  given EF: FxCtor[F]

  def map0[A, B](fa: F[A])(f: A => B): F[B]

  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage | Ignore.type): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage(message, level) =>
          flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(a))
        case Ignore =>
          EF.pureOf(a)
      }
    }

  def log_[A](fa: F[A])(toLeveledMessage: A => LeveledMessage | Ignore.type): F[Unit] =
    map0(log(fa)(toLeveledMessage))(_ => ())

  def logS(message: => String)(toLeveledMessage: (String => LeveledMessage) with LeveledMessage.Leveled): F[String] =
    toLeveledMessage.toLazyInput(message) match {
      case LeveledMessage(msg, level) =>
        lazy val messageString = msg()
        map0(EF.effectOf(canLog.getLogger(level)(messageString)))(_ => messageString)
    }

  def logS_(message: => String)(toLeveledMessage: (String => LeveledMessage) with LeveledMessage.Leveled): F[Unit] =
    toLeveledMessage.toLazyInput(message) match {
      case LeveledMessage(msg, level) =>
        EF.effectOf(canLog.getLogger(level)(msg()))
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

          case LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Some(a)))

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
          case LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Left(l)))

          case Ignore =>
            EF.pureOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message())))(_ => EF.pureOf(Right(r)))

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

object Log {

  def apply[F[*]: Log]: Log[F] = summon[Log[F]]

}
