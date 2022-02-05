package loggerf.scalaz

import scalaz._
import Scalaz._
import effectie.scalaz.Fx
import loggerf.logger.CanLog

trait LoggerEither[F[_]] {

  implicit val EF: FxCtor[F]
  implicit val MF: Monad[F]

  implicit val logger0: CanLog

  def debugEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(logger0.debug(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(logger0.debug(b2String(b))) *> EF.effectOf(b.right[A])
    }

  def infoEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(logger0.info(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(logger0.info(b2String(b))) *> EF.effectOf(b.right[A])
    }

  def warnEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(logger0.warn(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(logger0.warn(b2String(b))) *> EF.effectOf(b.right[A])
    }

  def errorEither[A, B](fab: F[A \/ B])(a2String: A => String, b2String: B => String): F[A \/ B] =
    MF.bind(fab) {
      case -\/(a) =>
        EF.effectOf(logger0.error(a2String(a))) *> EF.effectOf(a.left[B])
      case \/-(b) =>
        EF.effectOf(logger0.error(b2String(b))) *> EF.effectOf(b.right[A])
    }
}

object LoggerEither {
  def apply[F[_]: LoggerEither]: LoggerEither[F] = implicitly[LoggerEither[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerEither[F[_]](
    implicit EF: FxCtor[F],
    MF: Monad[F],
    canLog: CanLog,
  ): LoggerEither[F] = new LoggerEitherF[F]

  final class LoggerEitherF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: FxCtor[F],
    override val MF: Monad[F],
    override val logger0: CanLog,
  ) extends LoggerEither[F]

}
