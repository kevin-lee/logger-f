package loggerf

import cats.Monad
import just.effect.{EffectConstructor, Effectful}

/**
 * @author Kevin Lee
 * @since 2020-03-25
 */
trait Loggers[F[_]] extends LoggerA[F]

object Loggers {

  def apply[F[_] : Loggers]: Loggers[F] = implicitly[Loggers[F]]

  implicit def loggers[F[_]](implicit EF: EffectConstructor[F], EM: Monad[F], logger: Logger): Loggers[F] =
    new LoggersF[F]

  final class LoggersF[F[_] : EffectConstructor : Monad](
    implicit FE: EffectConstructor[F], FM: Monad[F], logger: Logger
  )
    extends Loggers[F]
    with LoggerA[F]
    with Effectful {

    override implicit val FE0: EffectConstructor[F] = FE
    override implicit val FM0: Monad[F] = FM

    override val logger0: Logger = logger
  }

}
