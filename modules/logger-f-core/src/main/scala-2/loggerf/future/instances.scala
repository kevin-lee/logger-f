package loggerf.future

import effectie.core.FxCtor
import loggerf.core.Log
import loggerf.logger.CanLog

import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-08
  */
object instances {
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def logFuture(
    implicit EF: FxCtor[Future],
    canLog: CanLog,
    EC: ExecutionContext,
  ): Log[Future] =
    new LogFuture(EF, canLog, EC)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  final class LogFuture(
    override val EF: FxCtor[Future],
    override val canLog: CanLog,
    val EC: ExecutionContext,
  ) extends Log[Future] {
    override def flatMap0[A, B](fa: Future[A])(f: A => Future[B]): Future[B] =
      fa.flatMap(f)(EC)
  }

}
