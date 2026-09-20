package loggerf

/** The location in the source file at which a log message was written.
  *
  * An instance is materialised by the compiler at the point where it is summoned, so a log message constructor
  * which takes one reports the caller's location rather than a location inside logger-f itself.
  *
  * No path is stored, only the file name. A path is baked into the compiled class file as a string constant, so
  * the same source compiled from two checkout directories would produce two different class files. That defeats a
  * shared build cache and reproducible builds, and it would leak the publisher's filesystem into a published
  * artifact.
  *
  * @param enclosingClass the fully qualified name of the class, trait, or object enclosing the call site
  * @param enclosingMethod the name of the method enclosing the call site, or `<init>` when the call site is in a
  *                        class or object body
  * @param fileName the simple name of the source file
  * @param line the 1-based line number
  * @author Kevin Lee
  * @since 2026-09-20
  */
final case class SourceLocation(enclosingClass: String, enclosingMethod: String, fileName: String, line: Int)

object SourceLocation extends SourceLocationInstances {

  implicit class SourceLocationOps(private val sourceLocation: SourceLocation) extends AnyVal {

    /** Renders this location in the shape of a JVM stack trace frame, e.g. `com.example.Foo.bar(Foo.scala:42)`. */
    def render: String = sourceLocation match {
      case SourceLocation(enclosingClass, enclosingMethod, fileName, line) =>
        s"$enclosingClass.$enclosingMethod($fileName:${line.toString})"
    }
  }
}
