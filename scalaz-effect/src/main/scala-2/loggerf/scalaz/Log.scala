package loggerf.scalaz

import scalaz._
import Scalaz._

import effectie.scalaz.Effectful._
import effectie.scalaz.Fx

import loggerf.LeveledMessage
import loggerf.LeveledMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.logger.CanLog
import loggerf.syntax._

/**
 * @author Kevin Lee
 * @since 2020-04-10
 */
trait Log[F[_]] {

  implicit val EF: Fx[F]
  implicit val MF: Monad[F]

  val logger0: CanLog

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable): F[A] =
    MF.bind(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(a)
      }
    }

  def log[A](
      foa: F[Option[A]]
    )(
      ifEmpty: => LeveledMessage with MaybeIgnorable
    , toLeveledMessage: A => LeveledMessage with MaybeIgnorable
    ): F[Option[A]] =
    MF.bind(foa) {
      case None =>
        ifEmpty match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(logger0, level)(message)) *> pureOf(none[A])

          case LeveledMessage.Ignore =>
            pureOf(none[A])
        }
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(logger0, level)(message)) *> effectOf(a.some)

          case LeveledMessage.Ignore =>
            effectOf(a.some)
        }
    }


  def log[A, B](
      feab: F[A \/ B]
    )(
      leftToMessage: A => LeveledMessage with MaybeIgnorable
    , rightToMessage: B => LeveledMessage with MaybeIgnorable
    ): F[A \/ B] =
    MF.bind(feab) {
    case -\/(a) =>
      leftToMessage(a) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(a.left[B])

        case LeveledMessage.Ignore =>
          effectOf(a.left[B])
      }
    case \/-(b) =>
      rightToMessage(b) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(b.right[A])

        case LeveledMessage.Ignore =>
          effectOf(b.right[A])
      }
  }

  def log[A](
      otfa: OptionT[F, A]
    )(
      ifEmpty: => LeveledMessage with MaybeIgnorable
    , toLeveledMessage: A => LeveledMessage with MaybeIgnorable
    ): OptionT[F, A] =
    OptionT(log(otfa.run)(ifEmpty, toLeveledMessage))


  def log[A, B](
      etfab: EitherT[F, A, B]
    )(
      leftToMessage: A => LeveledMessage with MaybeIgnorable
    , rightToMessage: B => LeveledMessage with MaybeIgnorable
    ): EitherT[F, A, B] =
    EitherT(log(etfab.run)(leftToMessage, rightToMessage))

}

object Log {

  def apply[F[_] : Log]: Log[F] = implicitly[Log[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def logF[F[_]](
    implicit EF: Fx[F], MF: Monad[F], canLog: CanLog
  ): Log[F] =
    new LogF[F](EF, MF, canLog)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  final class LogF[F[_]](
    override val EF: Fx[F]
  , override val MF: Monad[F]
  , override val logger0: CanLog
  ) extends Log[F]

}