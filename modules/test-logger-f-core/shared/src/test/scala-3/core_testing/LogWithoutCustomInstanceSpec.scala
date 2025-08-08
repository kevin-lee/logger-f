package core_testing

import hedgehog._
import hedgehog.runner._

/** @author Kevin Lee
  * @since 2025-08-05
  */
object LogWithoutCustomInstanceSpec extends Properties {

  override def tests: List[Test] = List(
    example("test compile time error", testCompileTimeError)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testCompileTimeError: Result = {
    val expectedMessage =
      """
        |  Could not find an implicit Log[util.Try].
        |  If you add [cats](https://typelevel.org/cats) library to your project,
        |  you can automatically get `Log[util.Try]` instance provided by logger-f.
        |
        |  Add
        |  ---
        |    "org.typelevel" %% "cats-core" % CATS_VERSION
        |  ---
        |  to build.sbt.
        |
        |  Or you can use your own instance of `Log[util.Try]` by importing yours.
        |  -----
        |  If it doesn't solve, it's probably because of missing an Fx[util.Try] instance.
        |
        |  You can simply import the Fx[util.Try] instance of your effect library.
        |  Please check out the message of @implicitNotFound annotation on `effectie.core.Fx`.
        |  Or please check out the following document.
        |
        |    https://logger-f.kevinly.dev/docs/cats/import/#instances-for-effectie
        |
        |    NOTE: You only need it once in the main method. In all other places,
        |    you can simply use the context bound with `Fx`.
        |
        |    e.g.)
        |      def foo[F[*]: Fx: Log]: F[A] =
        |        ...
        |        fa.log(a => info(s"The result is $a)"))
        |        ...
        |  -----
        |  If this doesn't solve, you probably need a `CanLog` instance.
        |  To create it, please check out the following document.
        |
        |    https://logger-f.kevinly.dev/docs/cats/import#canlog-logger
        |
        |    NOTE: You only need it once in the main method. In all other places,
        |    you should not see it unless you want to use a different logger per each type.
        |  -----""".stripMargin

    import scala.compiletime.testing.typeCheckErrors

    val actual = typeCheckErrors(
      """
      import effectie.core.*
      import loggerf.core.*
      import loggerf.core.syntax.all.*
      import loggerf.logger.{CanLog, Slf4JLogger}

      import effectie.instances.tries.fx.given
      import scala.util.Try

      given canLog: CanLog = Slf4JLogger.slf4JCanLog("test-can-log")

      def foo[F[*]: Fx: Log](n: Int): F[Int] = Log[F].log(Fx[F].effectOf(n * 2))(n => info(n.toString))


      foo[Try](1)
      """
    )

    val actualErrorMessage = actual.map(_.message).mkString

    Result
      .assert(actualErrorMessage.startsWith(expectedMessage))
      .log(
        s""">> actual error message doesn't match.
          |actualErrorMessage:
          |$actualErrorMessage
          |>> ------------------------
          |expectedMessage:
          |$expectedMessage
          |""".stripMargin
      )

  }
}
