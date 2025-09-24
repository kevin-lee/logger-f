package loggerf.testing

import cats.syntax.all._
import cats.{Eq, Show}
import loggerf.Level
import loggerf.logger.CanLog

import java.util.concurrent.atomic.AtomicInteger

/** @author Kevin Lee
  * @since 2023-02-05
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final case class CanLog4Testing(
  @volatile private var _messages: Vector[(Int, Level, String)] // scalafix:ok DisableSyntax.var
) extends CanLog {
  private val index = new AtomicInteger(-1)

  def currentIndex: Int = index.get()

  def messages: Vector[(Int, Level, String)] = _messages

  override def debug(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.debug, message))

  override def debug(throwable: Throwable)(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.debug, s"$message\n${throwable.toString}"))

  override def info(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.info, message))

  override def info(throwable: Throwable)(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.info, s"$message\n${throwable.toString}"))

  override def warn(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.warn, message))

  override def warn(throwable: Throwable)(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.warn, s"$message\n${throwable.toString}"))

  override def error(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.error, message))

  override def error(throwable: Throwable)(message: => String): Unit =
    _messages = _messages :+ ((index.addAndGet(1), Level.error, s"$message\n${throwable.toString}"))

  override def hashCode(): Int = messages.hashCode()

  override def equals(that: Any): Boolean = that match {
    case thatCanLog4Testing @ CanLog4Testing(_) =>
      val (thisMessages, thatMessages) = (this.getOrderedMessages, thatCanLog4Testing.getOrderedMessages)
      thisMessages === thatMessages
    case _ =>
      false
  }
}

object CanLog4Testing {

  def apply(): CanLog4Testing = CanLog4Testing(Vector.empty)

  implicit val canLog4TestingEq: Eq[CanLog4Testing] = Eq.fromUniversalEquals

  implicit class CanLog4TestingOps(private val canLog: CanLog4Testing) extends AnyVal {

    def getOrderedMessages: OrderedMessages = OrderedMessages(canLog.messages)

    def getMessagesWithoutOrder: MessagesWithoutOrder =
      CanLog4Testing.MessagesWithoutOrder(
        canLog.messages.map {
          case (_, level, message) => (level, message)
        }
      )

    def getLeveledMessages: LeveledMessages =
      (LeveledMessages.apply _)
        .tupled(
          canLog
            .messages
            .foldLeft(
              (Vector.empty[String], Vector.empty[String], Vector.empty[String], Vector.empty[String])
            ) {
              case ((debugs, infos, warns, errors), (_, level, message)) =>
                level match {
                  case Level.Debug =>
                    (debugs :+ message, infos, warns, errors)
                  case Level.Info =>
                    (debugs, infos :+ message, warns, errors)
                  case Level.Warn =>
                    (debugs, infos, warns :+ message, errors)
                  case Level.Error =>
                    (debugs, infos, warns, errors :+ message)
                }

            }
        )

  }

  final case class OrderedMessages(messages: Vector[(Int, Level, String)]) extends AnyVal
  object OrderedMessages {

    def withAutoIndex(levelAndMessage: (Level, String)*): OrderedMessages =
      OrderedMessages(
        levelAndMessage
          .zipWithIndex
          .map {
            case ((level, message), index) =>
              (index, level, message)
          }
          .toVector
      )

    implicit val orderedMessagesEq: Eq[OrderedMessages]     = Eq.fromUniversalEquals
    implicit val orderedMessagesShow: Show[OrderedMessages] = Show.fromToString
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class MessagesWithoutOrder(
    messages: Vector[(Level, String)]
  ) extends AnyVal
  object MessagesWithoutOrder {

    def apply(levelAndMessage: (Level, String)*): MessagesWithoutOrder =
      MessagesWithoutOrder(levelAndMessage.toVector)

    implicit val messagesWithoutOrderEq: Eq[MessagesWithoutOrder]     = Eq.fromUniversalEquals
    implicit val messagesWithoutOrderShow: Show[MessagesWithoutOrder] = Show.fromToString
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class LeveledMessages(
    debugMessages: Vector[String],
    infoMessages: Vector[String],
    warnMessages: Vector[String],
    errorMessages: Vector[String],
  )
  object LeveledMessages {
    implicit val leveledMessagesEq: Eq[LeveledMessages]     = Eq.fromUniversalEquals
    implicit val leveledMessagesShow: Show[LeveledMessages] = { leveledMessages =>
      s"""LeveledMessages(
         |  debugMessages=${leveledMessages.debugMessages.mkString("[", ",", "]")},
         |   infoMessages=${leveledMessages.infoMessages.mkString("[", ",", "]")},
         |   warnMessages=${leveledMessages.warnMessages.mkString("[", ",", "]")},
         |  errorMessages=${leveledMessages.errorMessages.mkString("[", ",", "]")}
         |)""".stripMargin
    }
  }

}
