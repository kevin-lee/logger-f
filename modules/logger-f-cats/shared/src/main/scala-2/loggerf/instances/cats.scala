package loggerf.instances

/** @author Kevin Lee
  * @since 2020-04-10
  */
@deprecated(
  message = _cats.DeprecatedMessage,
  since = "2.3.0",
)
object cats {

  @deprecated(
    message = _cats.DeprecatedMessage,
    since = "2.3.0",
  )
  def logF: Nothing = sys.error(_cats.DeprecatedMessage)
}
private[instances] object _cats {
  @SuppressWarnings(Array("org.wartremover.warts.FinalVal", "org.wartremover.warts.PublicInference"))
  final val DeprecatedMessage = // scalafix:ok DisableSyntax.noFinalVal
    "You don't need it anymore. If your project has `cats` as a dependency, it will be automatically available.\n" +
      "More info:\n" +
      "`loggerf.instances.cats.LogF` has been moved to `loggerf.core.Log` with [orphan-cats](https://github.com/kevin-lee/orphan). " +
      "So, if a project using `logger-f-core` without `logger-f-cats` has `cats` as a dependency, " +
      "the `Log` instance provided by `logger-f` is automatically available without any extra `import`s."
}
