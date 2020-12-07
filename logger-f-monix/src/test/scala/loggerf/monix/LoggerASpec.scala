package loggerf.monix

import cats._

import effectie.monix.EffectConstructor
import effectie.monix.Effectful._

import hedgehog._
import hedgehog.runner._

import loggerf.logger.LoggerForTesting

import monix.eval.Task

/**
 * @author Kevin Lee
 * @since 2020-04-12
 */
object LoggerASpec extends Properties {
  override def tests: List[Test] = List(
    property("test LoggerA.debugA(F[A])", testDebugAFA)
  , property("test LoggerA.debugS(F[A])", testDebugSFString)
  , property("test LoggerA.infoA(F[A])", testInfoAFA)
  , property("test LoggerA.infoS(F[A])", testInfoSFString)
  , property("test LoggerA.warnA(F[A])", testWarnAFA)
  , property("test LoggerA.warnS(F[A])", testWarnSFString)
  , property("test LoggerA.errorA(F[A])", testErrorAFA)
  , property("test LoggerA.errorS(F[A])", testErrorSFString)
  )

  import monix.execution.Scheduler.Implicits.global

  def testDebugAFA: Property = for {
    n <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("n")
    debugMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("debugMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad]: F[Int] =
      LoggerA[F].debugA(effectOf(n))(a => s"$debugMsg - $a")

    val result = runLog[Task].runSyncUnsafe()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = LoggerForTesting(
        debugMessages = Vector(s"$debugMsg - $n")
      , infoMessages = Vector.empty
      , warnMessages = Vector.empty
      , errorMessages = Vector.empty
    )

    Result.all(
      List(
        (result ==== n).log("result ==== n failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testDebugSFString: Property = for {
    s <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("s")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad]: F[String] =
      LoggerA[F].debugS(effectOf(s))

    val result = runLog[Task].runSyncUnsafe()

    val expected = LoggerForTesting(
        debugMessages = Vector(s)
      , infoMessages = Vector.empty
      , warnMessages = Vector.empty
      , errorMessages = Vector.empty
    )

    Result.all(
      List(
        (result ==== s).log("result ==== s failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }


  def testInfoAFA: Property = for {
    n <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("n")
    infoMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("infoMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad]: F[Int] =
      LoggerA[F].infoA(effectOf(n))(a => s"$infoMsg - $a")

    val result = runLog[Task].runSyncUnsafe()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = LoggerForTesting(
        debugMessages = Vector.empty
      , infoMessages = Vector(s"$infoMsg - $n")
      , warnMessages = Vector.empty
      , errorMessages = Vector.empty
    )

    Result.all(
      List(
        (result ==== n).log("result ==== n failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testInfoSFString: Property = for {
    s <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("s")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad]: F[String] =
      LoggerA[F].infoS(effectOf(s))

    val result = runLog[Task].runSyncUnsafe()

    val expected = LoggerForTesting(
        debugMessages = Vector.empty
      , infoMessages = Vector(s)
      , warnMessages = Vector.empty
      , errorMessages = Vector.empty
    )

    Result.all(
      List(
        (result ==== s).log("result ==== s failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }


  def testWarnAFA: Property = for {
    n <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("n")
    warnMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("warnMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad]: F[Int] =
      LoggerA[F].warnA(effectOf(n))(a => s"$warnMsg - $n")

    val result = runLog[Task].runSyncUnsafe()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = LoggerForTesting(
        debugMessages = Vector.empty
      , infoMessages = Vector.empty
      , warnMessages = Vector(s"$warnMsg - $n")
      , errorMessages = Vector.empty
    )

    Result.all(
      List(
        (result ==== n).log("result ==== n failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testWarnSFString: Property = for {
    s <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("s")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad]: F[String] =
      LoggerA[F].warnS(effectOf(s))

    val result = runLog[Task].runSyncUnsafe()

    val expected = LoggerForTesting(
        debugMessages = Vector.empty
      , infoMessages = Vector.empty
      , warnMessages = Vector(s)
      , errorMessages = Vector.empty
    )

    Result.all(
      List(
        (result ==== s).log("result ==== s failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }


  def testErrorAFA: Property = for {
    n <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("n")
    errorMsg <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("errorMsg")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def runLog[F[_] : EffectConstructor : Monad]: F[Int] =
      LoggerA[F].errorA(effectOf(n))(a => s"$errorMsg - $n")

    val result = runLog[Task].runSyncUnsafe()

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    val expected = LoggerForTesting(
        debugMessages = Vector.empty
      , infoMessages = Vector.empty
      , warnMessages = Vector.empty
      , errorMessages = Vector(s"$errorMsg - $n")
    )

    Result.all(
      List(
        (result ==== n).log("result ==== n failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }

  def testErrorSFString: Property = for {
    s <- Gen.string(Gen.unicode, Range.linear(1, 20)).log("s")
  } yield {

    implicit val logger: LoggerForTesting = LoggerForTesting()

    def runLog[F[_] : EffectConstructor : Monad]: F[String] =
      LoggerA[F].errorS(effectOf(s))

    val result = runLog[Task].runSyncUnsafe()

    val expected = LoggerForTesting(
        debugMessages = Vector.empty
      , infoMessages = Vector.empty
      , warnMessages = Vector.empty
      , errorMessages = Vector(s)
    )

    Result.all(
      List(
        (result ==== s).log("result ==== s failed")
      , (logger ==== expected).log("logger ==== expected failed")
      )
    )
  }


}
