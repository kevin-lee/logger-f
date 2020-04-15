package loggerf.scalaz

import scalaz._
import Scalaz._
import effectie.Effectful._
import effectie.scalaz.EffectConstructor
import loggerf.Logger
import loggerf.scalaz.Log.{MaybeIgnorable, NotIgnorable}

/**
 * @author Kevin Lee
 * @since 2020-04-10
 */
trait Log[F[_]] {

  import Log.{LeveledMessage, getLogger}

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  val logger0: Logger

  def log[A](fa: F[A])(toLeveledMessage: A => LeveledMessage with NotIgnorable): F[A] =
    MF0.bind(fa) { a =>
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
    MF0.bind(foa) {
      case None =>
        ifEmpty match {
          case LeveledMessage.LogMessage(message, level) =>
            effectOf(getLogger(logger0, level)(message)) *> effectOfPure(none[A])

          case LeveledMessage.Ignore =>
            effectOfPure(none[A])
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
    MF0.bind(feab) {
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

  sealed trait LeveledMessage
  sealed trait MaybeIgnorable
  sealed trait Ignorable extends MaybeIgnorable
  sealed trait NotIgnorable extends MaybeIgnorable

  object LeveledMessage {
    final case class LogMessage(message: String, level: Level) extends LeveledMessage with NotIgnorable
    case object Ignore extends LeveledMessage with Ignorable

    def debug(message: String): LeveledMessage with NotIgnorable =
      LogMessage(message, Level.debug)

    def info(message: String): LeveledMessage with NotIgnorable =
      LogMessage(message, Level.info)

    def warn(message: String): LeveledMessage with NotIgnorable =
      LogMessage(message, Level.warn)

    def error(message: String): LeveledMessage with NotIgnorable =
      LogMessage(message, Level.error)

    def ignore: LeveledMessage with MaybeIgnorable = Ignore
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