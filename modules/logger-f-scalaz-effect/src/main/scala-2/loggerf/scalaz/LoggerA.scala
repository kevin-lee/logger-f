package loggerf.scalaz

import scalaz._
import Scalaz._
import effectie.scalaz.Fx
import loggerf.logger.CanLog

trait LoggerA[F[_]] {

  implicit val EF: FxCtor[F]
  implicit val MF: Monad[F]

  implicit val logger0: CanLog

  def debugA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.bind(fa) { a =>
      EF.effectOf(logger0.debug(a2String(a))) *> EF.effectOf(a)
    }
  def debugS(message: F[String]): F[String]            = debugA(message)(identity)

  def infoA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.bind(fa) { a =>
      EF.effectOf(logger0.info(a2String(a))) *> EF.effectOf(a)
    }
  def infoS(message: F[String]): F[String]            = infoA(message)(identity)

  def warnA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.bind(fa) { a =>
      EF.effectOf(logger0.warn(a2String(a))) *> EF.effectOf(a)
    }
  def warnS(message: F[String]): F[String]            = warnA(message)(identity)

  def errorA[A](fa: F[A])(a2String: A => String): F[A] =
    MF.bind(fa) { a =>
      EF.effectOf(logger0.error(a2String(a))) *> EF.effectOf(a)
    }
  def errorS(message: F[String]): F[String]            = errorA(message)(identity)
}

object LoggerA {
  def apply[F[_]: LoggerA]: LoggerA[F] = implicitly[LoggerA[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerA[F[_]](
    implicit EF: FxCtor[F],
    MF: Monad[F],
    canLog: CanLog,
  ): LoggerA[F] =
    new LoggerAF[F]

  final class LoggerAF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF: FxCtor[F],
    override val MF: Monad[F],
    override val logger0: CanLog,
  ) extends LoggerA[F]

}
