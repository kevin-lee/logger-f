package loggerf.core

import effectie.core.FxCtor
import effectie.syntax.all.{effectOf, pureOf}
import loggerf.LeveledMessage
import loggerf.Ignore
import loggerf.logger.CanLog

/**
 * @author Kevin Lee
 * @since 2022-02-09
 */
trait Log[F[*]] {

  given EF: FxCtor[F]
  def flatMap0[A, B](fa: F[A])(f: A => F[B]): F[B]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage(message, level) =>
          flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => effectOf(a))
      }
    }

  def logPure[A](fa: F[A])(toLeveledMessage: A => LeveledMessage): F[A] =
    flatMap0(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage(message, level) =>
          flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => pureOf(a))
      }
    }

  def log[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignore.type,
    toLeveledMessage: A => LeveledMessage | Ignore.type
  ): F[Option[A]] =
    flatMap0(foa) {
      case None    =>
        ifEmpty match {
          case Ignore =>
            pureOf(None)

          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => effectOf(Some(a)))

          case Ignore =>
            effectOf(Some(a))
        }
    }

  def logPure[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignore.type,
    toLeveledMessage: A => LeveledMessage | Ignore.type
  ): F[Option[A]] =
    flatMap0(foa) {
      case None    =>
        ifEmpty match {
          case Ignore =>
            pureOf(None)

          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => pureOf(None))
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => pureOf(Some(a)))

          case Ignore =>
            pureOf(Some(a))
        }
    }

  def log[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignore.type,
    rightToMessage: B => LeveledMessage | Ignore.type
  ): F[Either[A, B]] =
    flatMap0(feab) {
      case Left(l)  =>
        leftToMessage(l) match {
          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => effectOf(Left(l)))

          case Ignore =>
            effectOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => effectOf(Right(r)))

          case Ignore =>
            effectOf(Right(r))
        }
    }

  def logPure[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignore.type,
    rightToMessage: B => LeveledMessage | Ignore.type
  ): F[Either[A, B]] =
    flatMap0(feab) {
      case Left(l)  =>
        leftToMessage(l) match {
          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => pureOf(Left(l)))

          case Ignore =>
            pureOf(Left(l))
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage(message, level) =>
            flatMap0(effectOf(canLog.getLogger(level)(message)))(_ => pureOf(Right(r)))

          case Ignore =>
            pureOf(Right(r))
        }
    }

}

object Log {

  def apply[F[_]: Log]: Log[F] = summon[Log[F]]

}
