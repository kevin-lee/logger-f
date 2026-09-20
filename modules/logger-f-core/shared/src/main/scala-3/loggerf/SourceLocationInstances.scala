package loggerf

import scala.quoted.*

/** @author Kevin Lee
  * @since 2026-09-20
  */
trait SourceLocationInstances {

  /** The location of the summon site, materialised by the compiler. */
  inline given here: SourceLocation = ${ SourceLocationMacro.hereImpl }
}

/** Runs on the JVM at macro expansion time only and is unreachable at run time, so it is safe on Scala.js and
  * Scala Native.
  */
object SourceLocationMacro {

  def hereImpl(using Quotes): Expr[SourceLocation] = {
    import quotes.reflect.*

    val pos      = Position.ofMacroExpansion
    val fileName = pos.sourceFile.name
    val line     = pos.startLine + 1

    val owners: List[Symbol] =
      Iterator
        .iterate(Symbol.spliceOwner)(_.maybeOwner)
        .takeWhile(o => !o.isNoSymbol && !o.isPackageDef)
        .toList

    def isSkipped(o: Symbol): Boolean =
      o.flags.is(Flags.Macro) || o.flags.is(Flags.Artifact) || o.flags.is(Flags.Synthetic) ||
        o.isAnonymousFunction || o.isLocalDummy

    def isRealClass(o: Symbol): Boolean =
      !isSkipped(o) && o.isClassDef && !o.isAnonymousClass

    def isRealMethod(o: Symbol): Boolean =
      !isSkipped(o) && o.isDefDef && !o.isClassConstructor

    val enclosingClass: String =
      owners
        .find(isRealClass)
        .fold("<unknown>")(o => normaliseClassName(o.fullName))

    val enclosingMethod: String =
      owners
        .takeWhile(o => !isRealClass(o))
        .find(o => isRealMethod(o) || (!isSkipped(o) && o.isClassConstructor))
        .filter(isRealMethod)
        .fold("<init>")(_.name)

    '{ SourceLocation(${ Expr(enclosingClass) }, ${ Expr(enclosingMethod) }, ${ Expr(fileName) }, ${ Expr(line) }) }
  }

  private def normaliseClassName(fullName: String): String =
    fullName.stripSuffix("$").replace("$.", ".")
}
