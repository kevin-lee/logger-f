package loggerf.cats.testing

import cats.effect.unsafe.{IORuntime, IORuntimeConfig}

import java.util.concurrent.ExecutorService

object IoAppUtils {

  def runtime(es: ExecutorService): IORuntime = {
    lazy val runtime: IORuntime = {

      val ec = ConcurrentSupport.newExecutionContextWithLogger(es, println(_))

      val (blocking, blockDown) =
        IORuntime.createDefaultBlockingExecutionContext()

      val (scheduler, schedDown) =
        IORuntime.createDefaultScheduler()

      IORuntime(
        ec,
        blocking,
        scheduler,
        { () =>
          es.shutdown()
          blockDown()
          schedDown()
        },
        IORuntimeConfig()
      )
    }
    runtime
  }
}
