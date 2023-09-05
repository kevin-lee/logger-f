package loggerf.instances

import effectie.core.FxCtor
import loggerf.core.Log
import loggerf.logger.CanLog

import scala.concurrent.{ExecutionContext, Future}

/** @author Kevin Lee
  * @since 2022-02-09
  */
object future {

  given logFuture(
    using canLog: CanLog,
    EC: ExecutionContext,
  ): Log[Future] =
    new LogFuture(canLog, EC)

  final class LogFuture(
    override val canLog: CanLog,
    val EC: ExecutionContext,
  ) extends Log[Future] {

    override val EF: FxCtor[Future] = effectie.instances.future.fxCtor.fxCtorFuture(EC)

    override def map0[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)(EC)

    override def flatMap0[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)(using EC)
  }

}
