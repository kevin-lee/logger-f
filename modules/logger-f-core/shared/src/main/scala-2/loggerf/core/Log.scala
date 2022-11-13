package loggerf.core

import effectie.core.FxCtor
import loggerf.LogMessage
import loggerf.LogMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.logger.CanLog

/** @author Kevin Lee
  * @since 2020-04-10
  */
trait Log[F[*]] {

  implicit val EF: FxCtor[F]
  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LogMessage with NotIgnorable): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LogMessage.LeveledMessage(message, level) =>
          flatMap0(EF.effectOf(canLog.getLogger(level)(message)))(_ => EF.pureOf(a))
      }
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

}

object Log {

  def apply[F[*]: Log]: Log[F] = implicitly[Log[F]]

}
