package loggerf

import cats._
import cats.implicits._

import just.effect.EffectConstructor

trait LoggerA[F[_]] {

  implicit val FE0: EffectConstructor[F]
  implicit val FM0: Monad[F]

  implicit val logger0: Logger

  def debugA[A](fa: F[A])(a2String: A => String): F[A] =
    FM0.flatMap(fa){ a =>
      FE0.effect(logger0.debug(a2String(a))) *> FE0.effect(a)
    }
  def debug(message: F[String]): F[String] = debugA(message)(identity)

  def infoA[A](fa: F[A])(a2String: A => String): F[A] =
    FM0.flatMap(fa){ a =>
      FE0.effect(logger0.info(a2String(a))) *> FE0.effect(a)
    }
  def info(message: F[String]): F[String] = infoA(message)(identity)

  def warnA[A](fa: F[A])(a2String: A => String): F[A] =
    FM0.flatMap(fa){ a =>
      FE0.effect(logger0.warn(a2String(a))) *> FE0.effect(a)
    }
  def warn(message: F[String]): F[String] = warnA(message)(identity)

  def errorA[A](fa: F[A])(a2String: A => String): F[A] =
    FM0.flatMap(fa){ a =>
      FE0.effect(logger0.error(a2String(a))) *> FE0.effect(a)
    }
  def error(message: F[String]): F[String] = errorA(message)(identity)
}

object LoggerA {
  def apply[F[_] : LoggerA]: LoggerA[F] = implicitly[LoggerA[F]]

  implicit def loggerA[F[_]](
    implicit FE: EffectConstructor[F], FM: Monad[F], logger: Logger
  ): LoggerA[F] =
    new LoggerAF[F]

  final class LoggerAF[F[_]](
    implicit override val FE0: EffectConstructor[F]
  , override val FM0: Monad[F]
  , override val logger0: Logger
  ) extends LoggerA[F]

}