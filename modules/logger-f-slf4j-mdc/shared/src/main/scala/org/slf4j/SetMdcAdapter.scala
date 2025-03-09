package org.slf4j

import org.slf4j.spi.MDCAdapter

/** @author Kevin Lee
  * @since 2025-03-09
  */
trait SetMdcAdapter {
  def apply(mdcAdapter: MDCAdapter): Unit
}

object SetMdcAdapter {
  def apply(mdcAdapter: MDCAdapter): Unit = {
    MDC.setMDCAdapter(mdcAdapter)
  }
}
