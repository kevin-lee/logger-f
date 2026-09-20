package loggerf

import scala.reflect.macros.blackbox

/** @author Kevin Lee
  * @since 2026-09-20
  */
trait SourceLocationInstances {

  /** The location of the summon site, materialised by the compiler. */
  implicit def here: SourceLocation = macro SourceLocationMacro.hereImpl
}

/** Runs on the JVM at macro expansion time only and is unreachable at run time, so it is safe on Scala.js and
  * Scala Native.
  */
object SourceLocationMacro {

  def hereImpl(c: blackbox.Context): c.Expr[SourceLocation] =
    c.Expr[SourceLocation](locationTree(c))

  /** A tree constructing the `SourceLocation` of the current macro expansion site. */
  def locationTree(c: blackbox.Context): c.Tree = {
    import c.universe._

    val pos      = c.enclosingPosition
    val fileName = pos.source.file.name
    val line     = pos.line

    val owners: List[Symbol] =
      Iterator
        .iterate(c.internal.enclosingOwner)(_.owner)
        .takeWhile(o => !o.isPackageClass && !o.isPackage)
        .toList

    def isRealClass(o: Symbol): Boolean =
      o.isClass && !o.name.decodedName.toString.startsWith("$anon")

    def isRealMethod(o: Symbol): Boolean =
      o.isMethod && !o.isSynthetic && !o.isConstructor && !o.asTerm.isAccessor && !o.asTerm.isLazy

    val enclosingClass: String =
      owners
        .find(isRealClass)
        .fold("<unknown>")(o => normaliseClassName(o.fullName))

    val enclosingMethod: String =
      owners
        .takeWhile(o => !isRealClass(o))
        .find(o => isRealMethod(o) || o.isConstructor)
        .filter(isRealMethod)
        .fold("<init>")(_.name.decodedName.toString)

    q"_root_.loggerf.SourceLocation($enclosingClass, $enclosingMethod, $fileName, $line)"
  }

  private def normaliseClassName(fullName: String): String =
    fullName.stripSuffix("$").replace("$.", ".")
}
