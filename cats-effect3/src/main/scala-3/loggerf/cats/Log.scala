package loggerf.cats

import cats.*
import cats.data.{EitherT, OptionT}
import cats.syntax.all.*
import effectie.cats.Effectful.*
import effectie.cats.Fx
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, MaybeIgnorable, NotIgnorable}
import loggerf.logger.CanLog
import loggerf.syntax.*

/** @author Kevin Lee
  * @since 2020-04-10
  */
trait Log[F[_]] {

  given EF0: Fx[F]
  given MF0: Monad[F]

  val canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    MF0.flatMap(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(canLog, level)(message)) *> effectOf(a)
      }
    }

  def logPure[A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    MF0.flatMap(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(canLog, level)(message)) *> pureOf(a)
      }
    }

  def log[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): F[Option[A]] =
    MF0.flatMap(foa) {
      case None    =>
        ifEmpty match {
          case LeveledMessage.Ignore =>
            pureOf(none[A])

          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> pureOf(none[A])
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> effectOf(a.some)

          case LeveledMessage.Ignore =>
            effectOf(a.some)
        }
    }

  def logPure[A](
    foa: F[Option[A]]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): F[Option[A]] =
    MF0.flatMap(foa) {
      case None    =>
        ifEmpty match {
          case LeveledMessage.Ignore =>
            pureOf(none[A])

          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> pureOf(none[A])
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> pureOf(a.some)

          case LeveledMessage.Ignore =>
            pureOf(a.some)
        }
    }

  def log[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): F[Either[A, B]] =
    MF0.flatMap(feab) {
      case Left(l)  =>
        leftToMessage(l) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> effectOf(l.asLeft[B])

          case LeveledMessage.Ignore =>
            effectOf(l.asLeft[B])
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> effectOf(r.asRight[A])

          case LeveledMessage.Ignore =>
            effectOf(r.asRight[A])
        }
    }

  def logPure[A, B](
    feab: F[Either[A, B]]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): F[Either[A, B]] =
    MF0.flatMap(feab) {
      case Left(l)  =>
        leftToMessage(l) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> pureOf(l.asLeft[B])

          case LeveledMessage.Ignore =>
            pureOf(l.asLeft[B])
        }
      case Right(r) =>
        rightToMessage(r) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> pureOf(r.asRight[A])

          case LeveledMessage.Ignore =>
            pureOf(r.asRight[A])
        }
    }

  def log[A](
    otfa: OptionT[F, A]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): OptionT[F, A] =
    OptionT(log(otfa.value)(ifEmpty, toLeveledMessage))

  def logPure[A](
    otfa: OptionT[F, A]
  )(
    ifEmpty: => LeveledMessage | Ignorable,
    toLeveledMessage: A => LeveledMessage | Ignorable
  ): OptionT[F, A] =
    OptionT(logPure(otfa.value)(ifEmpty, toLeveledMessage))

  def log[A, B](
    etfab: EitherT[F, A, B]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): EitherT[F, A, B] =
    EitherT(log(etfab.value)(leftToMessage, rightToMessage))

  def logPure[A, B](
    etfab: EitherT[F, A, B]
  )(
    leftToMessage: A => LeveledMessage | Ignorable,
    rightToMessage: B => LeveledMessage | Ignorable
  ): EitherT[F, A, B] =
    EitherT(logPure(etfab.value)(leftToMessage, rightToMessage))

}

object Log {

  def apply[F[_]: Log]: Log[F] = summon[Log[F]]

  given logF[F[_]](
    using EF: Fx[F],
    EM: Monad[F],
    logger: CanLog
  ): Log[F] =
    new LogF[F]

  final class LogF[F[_]](
    using override val EF0: Fx[F],
    override val MF0: Monad[F],
    override val canLog: CanLog
  ) extends Log[F]

}
