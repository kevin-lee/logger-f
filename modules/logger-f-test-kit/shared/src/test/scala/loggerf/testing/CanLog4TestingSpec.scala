package loggerf.testing

import cats.syntax.all._
import hedgehog._
import hedgehog.runner._
import loggerf.Level

/** @author Kevin Lee
  * @since 2023-02-05
  */
object CanLog4TestingSpec extends Properties {
  override def tests: List[Test] = List(
    property("test CanLog4Testing with index", testCanLog4TestingWithIndex),
    property("test CanLog4Testing check currentIndex", testCanLog4TestingCheckCurrentIndex),
    property("test CanLog4Testing without index", testCanLog4TestingWithMessagesWithoutOrder),
    property("test CanLog4Testing with leveled messages", testCanLog4TestingLeveledMessage),
    property("test CanLog4Testing.messages", testCanLog4TestingMessages),
    property("test CanLog4Testing hashCode and equality", testCanLog4TestingHashCodeAndEquality),
  )

  def testCanLog4TestingWithIndex: Property = for {
    levelAndMessageList <- Gens.genLevelAndMessage.list(Range.linear(1, 10)).log("levelAndMessageList")
  } yield {
    val expected = CanLog4Testing.OrderedMessages.withAutoIndex(levelAndMessageList: _*)
    val canLog   = CanLog4Testing()

    levelAndMessageList.foreach {
      case (level, message) =>
        level match {
          case Level.Debug => canLog.debug(message)
          case Level.Info => canLog.info(message)
          case Level.Warn => canLog.warn(message)
          case Level.Error => canLog.error(message)
        }
    }

    val actual = canLog.getOrderedMessages
    Result.all(
      List(
        Result.diffNamed("actual === expected", actual, expected)(_ === _),
        actual ==== expected,
      )
    )
  }

  def testCanLog4TestingCheckCurrentIndex: Property = for {
    levelAndMessageList <- Gens.genLevelAndMessage.list(Range.linear(1, 10)).log("levelAndMessageList")
  } yield {
    val expected = levelAndMessageList.length - 1

    val canLog = CanLog4Testing()

    val before = canLog.currentIndex ==== -1

    levelAndMessageList.foreach {
      case (level, message) =>
        level match {
          case Level.Debug => canLog.debug(message)
          case Level.Info => canLog.info(message)
          case Level.Warn => canLog.warn(message)
          case Level.Error => canLog.error(message)
        }
    }

    val actual = canLog.currentIndex
    Result.all(
      List(
        before,
        actual ==== expected,
      )
    )
  }

  def testCanLog4TestingWithMessagesWithoutOrder: Property = for {
    levelAndMessageList <- Gens.genLevelAndMessage.list(Range.linear(1, 10)).log("levelAndMessageList")
  } yield {
    val expected = CanLog4Testing.MessagesWithoutOrder(levelAndMessageList: _*)
    val canLog   = CanLog4Testing()

    levelAndMessageList.foreach {
      case (level, message) =>
        level match {
          case Level.Debug => canLog.debug(message)
          case Level.Info => canLog.info(message)
          case Level.Warn => canLog.warn(message)
          case Level.Error => canLog.error(message)
        }
    }

    val actual = canLog.getMessagesWithoutOrder
    Result.all(
      List(
        Result.diffNamed("actual === expected", actual, expected)(_ === _),
        actual ==== expected,
      )
    )

  }

  def testCanLog4TestingLeveledMessage: Property = for {
    levelAndMessageList <- Gens.genLevelAndMessage.list(Range.linear(1, 10)).log("levelAndMessageList")
    (debugMessages, infoMessages, warnMessages, errorMessages) =
      levelAndMessageList.foldLeft(
        (Vector.empty[String], Vector.empty[String], Vector.empty[String], Vector.empty[String])
      ) {
        case ((debugs, infos, warns, errors), (level, message)) =>
          level match {
            case Level.Debug => (debugs :+ message, infos, warns, errors)
            case Level.Info => (debugs, infos :+ message, warns, errors)
            case Level.Warn => (debugs, infos, warns :+ message, errors)
            case Level.Error => (debugs, infos, warns, errors :+ message)
          }

      }
  } yield {
    val expected = CanLog4Testing.LeveledMessages(debugMessages, infoMessages, warnMessages, errorMessages)

    val canLog = CanLog4Testing()

    levelAndMessageList.foreach {
      case (level, message) =>
        level match {
          case Level.Debug => canLog.debug(message)
          case Level.Info => canLog.info(message)
          case Level.Warn => canLog.warn(message)
          case Level.Error => canLog.error(message)
        }
    }

    val expectedShow =
      s"""LeveledMessages(
         |  debugMessages=${expected.debugMessages.mkString("[", ",", "]")},
         |   infoMessages=${expected.infoMessages.mkString("[", ",", "]")},
         |   warnMessages=${expected.warnMessages.mkString("[", ",", "]")},
         |  errorMessages=${expected.errorMessages.mkString("[", ",", "]")}
         |)""".stripMargin

    val actual = canLog.getLeveledMessages
    Result.all(
      List(
        Result.diffNamed("actual === expected", actual, expected)(_ === _),
        actual ==== expected,
        actual.show ==== expectedShow,
      )
    )

  }

  def testCanLog4TestingMessages: Property = for {
    levelAndMessageList <- Gens.genLevelAndMessage.list(Range.linear(1, 10)).log("levelAndMessageList")
  } yield {
    val expected =
      levelAndMessageList
        .zipWithIndex
        .map {
          case ((level, message), index) => (index, level, message)
        }
        .toVector

    val canLog = CanLog4Testing()

    levelAndMessageList.foreach {
      case (level, message) =>
        level match {
          case Level.Debug => canLog.debug(message)
          case Level.Info => canLog.info(message)
          case Level.Warn => canLog.warn(message)
          case Level.Error => canLog.error(message)
        }
    }

    val actual = canLog.messages

    Result.all(
      List(
        actual ==== expected
      )
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testCanLog4TestingHashCodeAndEquality: Property = for {
    levelAndMessageList <- Gens.genLevelAndMessage.list(Range.linear(1, 10)).log("levelAndMessageList")
  } yield {
    val expected = CanLog4Testing(
      levelAndMessageList
        .zipWithIndex
        .map {
          case ((level, message), index) => (index, level, message)
        }
        .toVector
    )

    val canLog = CanLog4Testing()

    levelAndMessageList.foreach {
      case (level, message) =>
        level match {
          case Level.Debug => canLog.debug(message)
          case Level.Info => canLog.info(message)
          case Level.Warn => canLog.warn(message)
          case Level.Error => canLog.error(message)
        }
    }

    val actual = canLog

    Result.all(
      List(
        actual.hashCode() ==== expected.hashCode(),
        Result.diffNamed("CanLog4Testing == CanLog4Testing", actual, expected)(_ == _),
        Result.diffNamed("CanLog4Testing != different CanLog4Testing", actual, CanLog4Testing())(_ != _),
        Result.diffNamed("CanLog4Testing === CanLog4Testing", actual, expected)(_ === _),
      )
    )
  }

}
