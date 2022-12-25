package loggerf.core

import effectie.core.FxCtor
import loggerf.LogMessage
import loggerf.LogMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2020-04-10
  */
trait Log[F[*]] {

  implicit def EF: FxCtor[F]
  def map0[A, B](fa: F[A])(f: A => B): F[B]

  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LogMessage.LeveledMessage(message, level) =>
          flatMap0(EF.effectOf(canLog.getLogger(level)(message)))(_ => EF.pureOf(a))
      }
    }

  def log_[A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable): F[Unit] =
    map0(log(fa)(toLeveledMessage))(_ => ())

  def logS(message: String)(toLeveledMessage: String => LogMessage with NotIgnorable): F[String] =
    toLeveledMessage(message) match {
      case LogMessage.LeveledMessage(msg, level) =>
        map0(EF.effectOf(canLog.getLogger(level)(msg)))(_ => message)
    }

  def logS_(message: String)(toLeveledMessage: String => LogMessage with NotIgnorable): F[Unit] =
    toLeveledMessage(message) match {
      case LogMessage.LeveledMessage(msg, level) =>
        EF.effectOf(canLog.getLogger(level)(msg))
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
            flatMap0(EF.effectOf(canLog.getLogger(level)(message)))(_ => EF.pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LogMessage.LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message)))(_ => EF.pureOf(Some(a)))

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
            flatMap0(EF.effectOf(canLog.getLogger(level)(message)))(_ => EF.pureOf(Left(l)))

          case LogMessage.Ignore =>
            EF.pureOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LogMessage.LeveledMessage(message, level) =>
            flatMap0(EF.effectOf(canLog.getLogger(level)(message)))(_ => EF.pureOf(Right(r)))

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
