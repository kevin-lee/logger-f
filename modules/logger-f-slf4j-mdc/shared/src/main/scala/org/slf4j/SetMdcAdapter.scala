package org.slf4j

import org.slf4j.spi.MDCAdapter

import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2025-03-09
  */
trait SetMdcAdapter {
  def apply(mdcAdapter: MDCAdapter): Unit
}

object SetMdcAdapter {
  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def apply(mdcAdapter: MDCAdapter): Unit = {
    val mdcClass = classOf[MDC]
    try {
      val method = mdcClass.getDeclaredMethod("setMDCAdapter", classOf[MDCAdapter])
      val _      = method.invoke(null, mdcAdapter) // scalafix:ok DisableSyntax.null
//      println("[MDC] A method named `setMDCAdapter` was found, so it was set using `setMDCAdapter` with reflection.")
      ()
    } catch {
      case NonFatal(_) =>
//        println(
//          "[MDC] No method named `setMDCAdapter` was found, so it will instead be set to the `mdcAdapter` field using reflection."
//        )
        val field = mdcClass.getDeclaredField("mdcAdapter")
        field.setAccessible(true)
        field.set(null, mdcAdapter) // scalafix:ok DisableSyntax.null
        field.setAccessible(false)
    }
  }
}
