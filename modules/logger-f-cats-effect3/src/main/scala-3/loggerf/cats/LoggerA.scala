package loggerf.cats

import cats.*
import cats.syntax.all.*
import effectie.cats.Fx
import loggerf.logger.CanLog

trait LoggerA[F[_]] {

  given EF: Fx[F]
  given MF: Monad[F]

  def canLog: CanLog

  def debugA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.flatMap(fa) { a =>
      EF.effectOf(canLog.debug(a2String(a))) *> EF.effectOf(a)
    }
  def debugS(message: F[String]): F[String]            = debugA(message)(identity)

  def infoA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.flatMap(fa) { a =>
      EF.effectOf(canLog.info(a2String(a))) *> EF.effectOf(a)
    }
  def infoS(message: F[String]): F[String]            = infoA(message)(identity)

  def warnA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.flatMap(fa) { a =>
      EF.effectOf(canLog.warn(a2String(a))) *> EF.effectOf(a)
    }
  def warnS(message: F[String]): F[String]            = warnA(message)(identity)

  def errorA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.flatMap(fa) { a =>
      EF.effectOf(canLog.error(a2String(a))) *> EF.effectOf(a)
    }
  def errorS(message: F[String]): F[String]            = errorA(message)(identity)
}

object LoggerA {
  def apply[F[_]: LoggerA]: LoggerA[F] = summon[LoggerA[F]]

  given loggerA[F[_]](
    using EF: Fx[F],
    MF: Monad[F],
    canLog: CanLog
  ): LoggerA[F] =
    new LoggerAF[F](EF, MF, canLog)

  final class LoggerAF[F[_]](
    override val EF: Fx[F],
    override val MF: Monad[F],
    override val canLog: CanLog
  ) extends LoggerA[F]

}
