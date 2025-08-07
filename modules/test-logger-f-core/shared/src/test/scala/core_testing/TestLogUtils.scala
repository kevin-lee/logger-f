package core_testing

import effectie.core.FxCtor
import loggerf.core.Log
import loggerf.logger.CanLog

import scala.util.Try

/** @author Kevin Lee
  * @since 2025-08-06
  */
object TestLogUtils {

  implicit def logTry(implicit canLog0: CanLog, fxCtor: FxCtor[Try]): Log[Try] = new Log[Try] {
    override implicit def EF: FxCtor[Try] = fxCtor

    override def map0[A, B](fa: Try[A])(f: A => B): Try[B] = fa.map(f)

    override def flatMap0[A, B](fa: Try[A])(f: A => Try[B]): Try[B] = fa.flatMap(f)

    override def canLog: CanLog = canLog0
  }

}
