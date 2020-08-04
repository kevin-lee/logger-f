package loggerf.logger

import org.apache.logging.log4j.util.Supplier

/**
 * @author Kevin Lee
 * @since 2020-08-04
 */
object Log4jCompat {
  def toStringSupplier(s: => String): Supplier[String] =
    new Supplier[String] {
      override def get(): String = s
    }
}
