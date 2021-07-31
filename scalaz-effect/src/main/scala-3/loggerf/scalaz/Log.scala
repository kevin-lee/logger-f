package loggerf.scalaz

import scalaz.*
import Scalaz.*
import effectie.scalaz.Effectful.*
import effectie.scalaz.Fx
import loggerf.LeveledMessage
import loggerf.LeveledMessage.{Ignorable, NotIgnorable}
import loggerf.logger.CanLog
import loggerf.syntax.*

/**
 * @author Kevin Lee
 * @since 2020-04-10
 */
trait Log[F[_]] {

  given EF: Fx[F]
  given MF: Monad[F]

  def canLog: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage & NotIgnorable): F[A] =
    MF.bind(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(canLog, level)(message)) *> effectOf(a)
      }
    }

  def log[A](
      foa: F[Option[A]]
    )(
      ifEmpty: => LeveledMessage | Ignorable
    , toLeveledMessage: A => LeveledMessage | Ignorable
    ): F[Option[A]] =
    MF.bind(foa) {
      case None =>
        ifEmpty match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> pureOf(none[A])

          case LeveledMessage.Ignore =>
            pureOf(none[A])
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(canLog, level)(message)) *> effectOf(a.some)

          case LeveledMessage.Ignore =>
            effectOf(a.some)
        }
    }


  def log[A, B](
      feab: F[A \/ B]
    )(
      leftToMessage: A => LeveledMessage | Ignorable
    , rightToMessage: B => LeveledMessage | Ignorable
    ): F[A \/ B] =
    MF.bind(feab) {
    case -\/(a) =>
      leftToMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(canLog, level)(message)) *> effectOf(a.left[B])

        case LeveledMessage.Ignore =>
          effectOf(a.left[B])
      }
    case \/-(b) =>
      rightToMessage(b) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(canLog, level)(message)) *> effectOf(b.right[A])

        case LeveledMessage.Ignore =>
          effectOf(b.right[A])
      }
  }

  def log[A](
      otfa: OptionT[F, A]
    )(
      ifEmpty: => LeveledMessage | Ignorable
    , toLeveledMessage: A => LeveledMessage | Ignorable
    ): OptionT[F, A] =
    OptionT(log(otfa.run)(ifEmpty, toLeveledMessage))


  def log[A, B](
      etfab: EitherT[F, A, B]
    )(
      leftToMessage: A => LeveledMessage | Ignorable
    , rightToMessage: B => LeveledMessage | Ignorable
    ): EitherT[F, A, B] =
    EitherT(log(etfab.run)(leftToMessage, rightToMessage))

}

object Log {

  def apply[F[_] : Log]: Log[F] = summon[Log[F]]

  given logF[F[_]](
    using EF: Fx[F], EM: Monad[F], canLog: CanLog
  ): Log[F] =
    new LogF[F](EF, EM, canLog)

  final class LogF[F[_]](
    override val EF: Fx[F]
  , override val MF: Monad[F]
  , override val canLog: CanLog
  ) extends Log[F]

}