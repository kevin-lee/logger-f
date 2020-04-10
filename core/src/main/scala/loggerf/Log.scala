package loggerf

import cats._
import cats.data.{EitherT, OptionT}
import cats.implicits._

import effectie.Effectful._
import effectie.cats.EffectConstructor

/**
 * @author Kevin Lee
 * @since 2020-04-10
 */
trait Log[F[_]] {

  import Log.{LeveledMessage, getLogger}

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  val logger0: Logger

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage): F[A] =
    MF0.flatMap(fa) { a =>
      toLeveledMessage(a) match {
        case LeveledMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(a)
      }
    }

  def log[A](
      foa: F[Option[A]]
    )(
      ifEmpty: => LeveledMessage
    , toLeveledMessage: A => LeveledMessage
    ): F[Option[A]] =
    MF0.flatMap(foa) {
      case None =>
        (for {
          message <- effectOf(ifEmpty)
          _ <- effectOf(getLogger(logger0, message.level)(message.message))
        } yield ()) *> effectOf(none[A])
      case Some(a) =>
        toLeveledMessage(a) match {
          case LeveledMessage(message, level) =>
            effectOf(getLogger(logger0, level)(message)) *> effectOf(a.some)
        }
    }


  def log[A, B](
      feab: F[Either[A, B]]
    )(
      leftToMessage: A => LeveledMessage
    , rightToMessage: B => LeveledMessage
    ): F[Either[A, B]] =
    MF0.flatMap(feab) {
    case Left(l) =>
      leftToMessage(l) match {
        case LeveledMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(l.asLeft[B])
      }
    case Right(r) =>
      rightToMessage(r) match {
        case LeveledMessage(message, level) =>
          effectOf(getLogger(logger0, level)(message)) *> effectOf(r.asRight[A])
      }
  }

  def log[A](
      otfa: OptionT[F, A]
    )(
      ifEmpty: => LeveledMessage
    , toLeveledMessage: A => LeveledMessage
    ): OptionT[F, A] =
    OptionT(log(otfa.value)(ifEmpty, toLeveledMessage))


  def log[A, B](
      etfab: EitherT[F, A, B]
    )(
      leftToMessage: A => LeveledMessage
    , rightToMessage: B => LeveledMessage
    ): EitherT[F, A, B] =
    EitherT(log(etfab.value)(leftToMessage, rightToMessage))

}

object Log {
  sealed trait Level
  object Level {
    case object Debug extends Level
    case object Info extends Level
    case object Warn extends Level
    case object Error extends Level

    def debug: Level = Debug
    def info: Level = Info
    def warn: Level = Warn
    def error: Level = Error
  }

  final case class LeveledMessage(message: String, level: Level)
  object LeveledMessage {
    def debug(message: String): LeveledMessage =
      LeveledMessage(message, Level.debug)

    def info(message: String): LeveledMessage =
      LeveledMessage(message, Level.info)

    def warn(message: String): LeveledMessage =
      LeveledMessage(message, Level.warn)

    def error(message: String): LeveledMessage =
      LeveledMessage(message, Level.error)
  }

  def getLogger(logger: Logger, level: Level): String => Unit = level match {
    case Level.Debug => logger.debug
    case Level.Info => logger.info
    case Level.Warn => logger.warn
    case Level.Error => logger.error
  }

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