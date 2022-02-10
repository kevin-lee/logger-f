package loggerf.core

import effectie.core.FxCtor
import effectie.syntax.all.{effectOf, pureOf}
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, NotIgnorable}
import loggerf.logger.CanLog
import loggerf.syntax.getLogger

/**
 * @author Kevin Lee
 * @since 2022-02-09
 */
trait Log[F[*]] {

  given EF: FxCtor[F]
  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => effectOf(a))
      }
    }

  def logPure[A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => pureOf(a))
      }
    }

  def log[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): F[Option[A]] =
    flatMap0(foa) {
      case None    =>
        ifEmpty match {
          case LeveledMessage.Ignore =>
            pureOf(None)

          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => effectOf(Some(a)))

          case LeveledMessage.Ignore =>
            effectOf(Some(a))
        }
    }

  def logPure[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): F[Option[A]] =
    flatMap0(foa) {
      case None    =>
        ifEmpty match {
          case LeveledMessage.Ignore =>
            pureOf(None)

          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => pureOf(Some(a)))

          case LeveledMessage.Ignore =>
            pureOf(Some(a))
        }
    }

  def log[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): F[Either[A, B]] =
    flatMap0(feab) {
      case Left(l)  =>
        leftToMessage(l) match {
          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => effectOf(Left(l)))

          case LeveledMessage.Ignore =>
            effectOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => effectOf(Right(r)))

          case LeveledMessage.Ignore =>
            effectOf(Right(r))
        }
    }

  def logPure[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): F[Either[A, B]] =
    flatMap0(feab) {
      case Left(l)  =>
        leftToMessage(l) match {
          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => pureOf(Left(l)))

          case LeveledMessage.Ignore =>
            pureOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage.LogMessage(message, level) =>
            flatMap0(effectOf(getLogger(canLog, level)(message)))(_ => pureOf(Right(r)))

          case LeveledMessage.Ignore =>
            pureOf(Right(r))
        }
    }

}

object Log {

  def apply[F[_]: Log]: Log[F] = summon[Log[F]]

}
