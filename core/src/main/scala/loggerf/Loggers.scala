package loggerf

import cats.Monad

import just.effect.{EffectConstructor, Effectful}

/**
 * @author Kevin Lee
 * @since 2020-03-25
 */
trait Loggers[F[_]] extends LoggerA[F] with LoggerOption[F] with LoggerEither[F]

object Loggers {

  def apply[F[_] : Loggers]: Loggers[F] = implicitly[Loggers[F]]

  implicit def loggers[F[_]](implicit EF: EffectConstructor[F], EM: Monad[F], logger: Logger): Loggers[F] =
    new LoggersF[F]

  final class LoggersF[F[_] : EffectConstructor : Monad](
    implicit override val FE0: EffectConstructor[F]
  , override val FM0: Monad[F]
  , override val logger0: Logger
  ) extends Loggers[F]
    with LoggerA[F]
    with LoggerOption[F]
    with LoggerEither[F]
    with Effectful

}
