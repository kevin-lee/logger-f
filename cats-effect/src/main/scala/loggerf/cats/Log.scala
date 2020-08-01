package loggerf.cats

import cats._
import cats.data.{EitherT, OptionT}
import cats.implicits._

import effectie.Effectful._
import effectie.cats.EffectConstructor

import loggerf.LeveledMessage
import loggerf.LeveledMessage.{MaybeIgnorable, NotIgnorable}
import loggerf.logger.Logger
import loggerf.syntax._

/**
 * @author Kevin Lee
 * @since 2020-04-10
 */
trait Log[F[_]] {

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  val logger0: Logger

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable): F[A] =
    MF0.flatMap(fa) { a =>
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
    MF0.flatMap(foa) {
      case None =>
        ifEmpty match {
          case LeveledMessage.Ignore =>
            effectOfPure(none[A])

          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(logger0, level)(message)) *> effectOfPure(none[A])
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
      feab: F[Either[A, B]]
    )(
      leftToMessage: A => LeveledMessage with MaybeIgnorable
    , rightToMessage: B => LeveledMessage with MaybeIgnorable
    ): F[Either[A, B]] =
    MF0.flatMap(feab) {
    case Left(l) =>
      leftToMessage(l) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(l.asLeft[B])

        case LeveledMessage.Ignore =>
          effectOf(l.asLeft[B])
      }
    case Right(r) =>
      rightToMessage(r) match {
        case LeveledMessage.LogMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(r.asRight[A])

        case LeveledMessage.Ignore =>
          effectOf(r.asRight[A])
      }
  }

  def log[A](
      otfa: OptionT[F, A]
    )(
      ifEmpty: => LeveledMessage with MaybeIgnorable
    , toLeveledMessage: A => LeveledMessage with MaybeIgnorable
    ): OptionT[F, A] =
    OptionT(log(otfa.value)(ifEmpty, toLeveledMessage))


  def log[A, B](
      etfab: EitherT[F, A, B]
    )(
      leftToMessage: A => LeveledMessage with MaybeIgnorable
    , rightToMessage: B => LeveledMessage with MaybeIgnorable
    ): EitherT[F, A, B] =
    EitherT(log(etfab.value)(leftToMessage, rightToMessage))

}

object Log {

  def apply[F[_] : Log]: Log[F] = implicitly[Log[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def logF[F[_]](
    implicit EF: EffectConstructor[F], EM: Monad[F], logger: Logger
  ): Log[F] =
    new LogF[F]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  final class LogF[F[_]](
    implicit override val EF0: EffectConstructor[F]
  , implicit override val MF0: Monad[F]
  , override val logger0: Logger
  ) extends Log[F]

}