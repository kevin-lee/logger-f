package loggerf.instances

import effectie.core.FxCtor
import loggerf.core.Log
import loggerf.logger.CanLog

import scala.util.Try

/** @author Kevin Lee
  * @since 2023-09-05
  */
object tries {
  implicit def logTry(implicit canLog: CanLog): Log[Try] = new LogTry(canLog)

  private final class LogTry(override val canLog: CanLog) extends Log[Try] {
    override implicit val EF: FxCtor[Try] = effectie.instances.tries.fxCtor.fxCtorTry

    override def map0[A, B](fa: Try[A])(f: A => B): Try[B] = fa.map(f)

    override def flatMap0[A, B](fa: Try[A])(f: A => Try[B]): Try[B] = fa.flatMap(f)
  }
}
