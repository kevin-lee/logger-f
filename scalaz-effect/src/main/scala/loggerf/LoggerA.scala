package loggerf

import scalaz._
import Scalaz._

import effectie.scalaz.EffectConstructor

trait LoggerA[F[_]] {

  implicit val EF0: EffectConstructor[F]
  implicit val MF0: Monad[F]

  implicit val logger0: Logger

  def debugA[A](fa: F[A])(a2String: A => String): F[A] =
    MF0.bind(fa){ a =>
      EF0.effectOf(logger0.debug(a2String(a))) *> EF0.effectOf(a)
    }
  def debugS(message: F[String]): F[String] = debugA(message)(identity)

  def infoA[A](fa: F[A])(a2String: A => String): F[A] =
    MF0.bind(fa){ a =>
      EF0.effectOf(logger0.info(a2String(a))) *> EF0.effectOf(a)
    }
  def infoS(message: F[String]): F[String] = infoA(message)(identity)

  def warnA[A](fa: F[A])(a2String: A => String): F[A] =
    MF0.bind(fa){ a =>
      EF0.effectOf(logger0.warn(a2String(a))) *> EF0.effectOf(a)
    }
  def warnS(message: F[String]): F[String] = warnA(message)(identity)

  def errorA[A](fa: F[A])(a2String: A => String): F[A] =
    MF0.bind(fa){ a =>
      EF0.effectOf(logger0.error(a2String(a))) *> EF0.effectOf(a)
    }
  def errorS(message: F[String]): F[String] = errorA(message)(identity)
}

object LoggerA {
  def apply[F[_] : LoggerA]: LoggerA[F] = implicitly[LoggerA[F]]

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def loggerA[F[_]](
    implicit EF: EffectConstructor[F], MF: Monad[F], logger: Logger
  ): LoggerA[F] =
    new LoggerAF[F]

  final class LoggerAF[F[_]](
    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    implicit override val EF0: EffectConstructor[F]
  , override val MF0: Monad[F]
  , override val logger0: Logger
  ) extends LoggerA[F]

}