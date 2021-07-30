package loggerf.cats

import cats.*
import cats.syntax.all.*
import effectie.cats.Fx
import loggerf.logger.CanLog

trait LoggerOption[F[_]] {

  given EF0: Fx[F]
  given MF0: Monad[F]

  given canLog: CanLog

  def debugOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF0.flatMap(fa) {
      case Some(a) =>
        EF0.effectOf(canLog.debug(a2String(a))) *> EF0.effectOf(a.some)
      case None    =>
        EF0.effectOf(canLog.debug(ifEmpty)) *> EF0.effectOf(none[A])
    }

  def infoOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF0.flatMap(fa) {
      case Some(a) =>
        EF0.effectOf(canLog.info(a2String(a))) *> EF0.effectOf(a.some)
      case None    =>
        EF0.effectOf(canLog.info(ifEmpty)) *> EF0.effectOf(none[A])
    }

  def warnOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF0.flatMap(fa) {
      case Some(a) =>
        EF0.effectOf(canLog.warn(a2String(a))) *> EF0.effectOf(a.some)
      case None    =>
        EF0.effectOf(canLog.warn(ifEmpty)) *> EF0.effectOf(none[A])
    }

  def errorOption[A](fa: F[Option[A]])(ifEmpty: => String, a2String: A => String): F[Option[A]] =
    MF0.flatMap(fa) {
      case Some(a) =>
        EF0.effectOf(canLog.error(a2String(a))) *> EF0.effectOf(a.some)
      case None    =>
        EF0.effectOf(canLog.error(ifEmpty)) *> EF0.effectOf(none[A])
    }
}

object LoggerOption {
  def apply[F[_]: LoggerOption]: LoggerOption[F] = summon[LoggerOption[F]]

  given loggerOption[F[_]](
    using EF: Fx[F],
    MF: Monad[F],
    logger: CanLog
  ): LoggerOption[F] = new LoggerOptionF[F]

  final class LoggerOptionF[F[_]](
    using override val EF0: Fx[F],
    override val MF0: Monad[F],
    override val canLog: CanLog
  ) extends LoggerOption[F]

}
