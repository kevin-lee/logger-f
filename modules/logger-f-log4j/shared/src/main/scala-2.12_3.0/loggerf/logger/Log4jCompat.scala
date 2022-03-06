package loggerf.logger

import org.apache.logging.log4j.util.Supplier

object Log4jCompat {
  @inline def toStringSupplier(s: => String): Supplier[String] =
    () => s
}
