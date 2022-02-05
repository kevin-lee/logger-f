package loggerf.scalaz

import scalaz.*
import Scalaz.*
import effectie.scalaz.Fx
import loggerf.logger.CanLog

trait LoggerEither[F[_]] {

  given EF: FxCtor[F]
  given MF: Monad[F]

  def canLog: CanLog

  def debugEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(canLog.debug(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(canLog.debug(b2String(b))) *> EF.effectOf(b.right[A])
    }

  def infoEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(canLog.info(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(canLog.info(b2String(b))) *> EF.effectOf(b.right[A])
    }

  def warnEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(canLog.warn(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(canLog.warn(b2String(b))) *> EF.effectOf(b.right[A])
    }

  def errorEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(canLog.error(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(canLog.error(b2String(b))) *> EF.effectOf(b.right[A])
    }
}

object LoggerEither {
  def apply[F[_] : LoggerEither]: LoggerEither[F] = summon[LoggerEither[F]]

  given loggerEither[F[_]](
    using EF: FxCtor[F], MF: Monad[F], canLog: CanLog
  ): LoggerEither[F] = new LoggerEitherF[F](EF, MF, canLog)

  final class LoggerEitherF[F[_]](
    override val EF: FxCtor[F]
  , override val MF: Monad[F]
  , override val canLog: CanLog
  ) extends LoggerEither[F]

}