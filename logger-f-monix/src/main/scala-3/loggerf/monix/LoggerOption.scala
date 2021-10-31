package loggerf.monix

import cats.*
import cats.syntax.all.*
import effectie.monix.Fx
import loggerf.logger.CanLog

trait LoggerOption[F[_]] {

  given EF: Fx[F]
  given MF: Monad[F]

  def canLog: CanLog

  def debugOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.flatMap(fa) {
      case Some(a) =>
        EF.effectOf(canLog.debug(a2String(a))) *> EF.effectOf(a.some)
      case None    =>
        EF.effectOf(canLog.debug(ifEmpty)) *> EF.effectOf(none[A])
    }

  def infoOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.flatMap(fa) {
      case Some(a) =>
        EF.effectOf(canLog.info(a2String(a))) *> EF.effectOf(a.some)
      case None    =>
        EF.effectOf(canLog.info(ifEmpty)) *> EF.effectOf(none[A])
    }

  def warnOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.flatMap(fa) {
      case Some(a) =>
        EF.effectOf(canLog.warn(a2String(a))) *> EF.effectOf(a.some)
      case None    =>
        EF.effectOf(canLog.warn(ifEmpty)) *> EF.effectOf(none[A])
    }

  def errorOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF.flatMap(fa) {
      case Some(a) =>
        EF.effectOf(canLog.error(a2String(a))) *> EF.effectOf(a.some)
      case None    =>
        EF.effectOf(canLog.error(ifEmpty)) *> EF.effectOf(none[A])
    }
}

object LoggerOption {
  def apply[F[_]: LoggerOption]: LoggerOption[F] = summon[LoggerOption[F]]

  given loggerOption[F[_]](
    using EF: Fx[F],
    MF: Monad[F],
    canLog: CanLog
  ): LoggerOption[F] = new LoggerOptionF[F](EF, MF, canLog)

  final class LoggerOptionF[F[_]](
    override val EF: Fx[F],
    override val MF: Monad[F],
    override val canLog: CanLog
  ) extends LoggerOption[F]

}