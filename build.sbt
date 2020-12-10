import ProjectInfo.{ProjectName, _}
import kevinlee.sbt.SbtCommon.crossVersionProps
import just.semver.SemVer
import SemVer.{Major, Minor}

val DottyVersion = "3.0.0-M2"
val ProjectScalaVersion = "2.13.3"

val removeDottyIncompatible: ModuleID => Boolean =
  m =>
    m.name == "wartremover" ||
      m.name == "ammonite" ||
      m.name == "kind-projector" ||
      m.name == "mdoc"

val CrossScalaVersions: Seq[String] = Seq(
  "2.11.12", "2.12.12", "2.13.3", DottyVersion
).distinct
val IncludeTest: String = "compile->compile;test->test"

lazy val hedgehogVersion = "0.5.1"
lazy val hedgehogRepo: MavenRepository =
  "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"

lazy val hedgehogLibs: Seq[ModuleID] = Seq(
    "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion % Test
  , "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion % Test
  , "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion % Test
)

lazy val libScalazCore: ModuleID = "org.scalaz" %% "scalaz-core" % "7.2.30"
lazy val libScalazEffect: ModuleID = "org.scalaz" %% "scalaz-effect" % "7.2.30"

lazy val libCatsCore: ModuleID = "org.typelevel" %% "cats-core" % "2.1.1"
lazy val libCatsEffect: ModuleID = "org.typelevel" %% "cats-effect" % "2.1.2"

lazy val libCatsCore_2_0_0: ModuleID = "org.typelevel" %% "cats-core" % "2.0.0"
lazy val libCatsEffect_2_0_0: ModuleID = "org.typelevel" %% "cats-effect" % "2.0.0"

lazy val slf4jApi: ModuleID = "org.slf4j" % "slf4j-api" % "1.7.30"
lazy val logbackClassic: ModuleID =  "ch.qos.logback" % "logback-classic" % "1.2.3"

lazy val log4sLib: ModuleID = "org.log4s" %% "log4s" % "1.9.0"
lazy val log4sLibForScala3: ModuleID = "org.log4s" %% "log4s" % "1.10.0-M3"

lazy val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.13.1"
lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.13.1"

lazy val sbtLoggingLib = "org.scala-sbt" %% "util-logging"

ThisBuild / scalaVersion     := ProjectScalaVersion
ThisBuild / version          := ProjectVersion
ThisBuild / organization     := "io.kevinlee"
ThisBuild / organizationName := "Kevin's Code"
ThisBuild / crossScalaVersions := CrossScalaVersions

val GitHubUsername = "Kevin-Lee"
val RepoName = "logger-f"

ThisBuild / developers   := List(
  Developer(GitHubUsername, "Kevin Lee", "kevin.code@kevinlee.io", url(s"https://github.com/$GitHubUsername"))
)
ThisBuild / homepage := Some(url(s"https://github.com/$GitHubUsername/$RepoName"))
ThisBuild / scmInfo :=
  Some(ScmInfo(
    browseUrl = url(s"https://github.com/$GitHubUsername/$RepoName")
  , connection = s"scm:git:git@github.com:$GitHubUsername/$RepoName.git"
  ))

def prefixedProjectName(name: String) = s"$RepoName${if (name.isEmpty) "" else s"-$name"}"

lazy val noPublish: SettingsDefinition = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in sbt.Keys.`package` := true,
  skip in packagedArtifacts := true,
  skip in publish := true
)

def scalacOptionsPostProcess(scalaSemVer: SemVer, isDotty: Boolean, options: Seq[String]): Seq[String] =
  if (isDotty || (scalaSemVer.major, scalaSemVer.minor) == (SemVer.Major(3), SemVer.Minor(0))) {
    Seq(
      "-source:3.0-migration",
      "-language:" + List(
        "dynamics",
        "existentials",
        "higherKinds",
        "reflectiveCalls",
        "experimental.macros",
        "implicitConversions"
      ).mkString(","),
      "-Ykind-projector",
      "-siteroot", "./dotty-docs",
    )
  } else {
    scalaSemVer match {
      case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
        if (patch >= 3)
          options.filterNot(_ == "-Xlint:nullary-override")
        else
          options
      case _: SemVer =>
        options
    }
  }

def libraryDependenciesRemoveDottyIncompatible(
  scalaVersion: String,
  isDotty: Boolean,
  libraries: Seq[ModuleID]
): Seq[ModuleID] = (
  if (isDotty)
    libraries
      .filterNot(removeDottyIncompatible)
  else
    libraries
)
def libraryDependenciesPostProcess(
  scalaVersion: String,
  isDotty: Boolean,
  libraries: Seq[ModuleID]
): Seq[ModuleID] = (
  if (isDotty)
    libraries
      .filterNot(removeDottyIncompatible)
      .map(_.withDottyCompat(scalaVersion))
  else
    libraries
)

val effectieVersion: String = "1.8.0"
lazy val effectieCatsEffect: ModuleID = "io.kevinlee" %% "effectie-cats-effect" % effectieVersion
lazy val effectieMonix: ModuleID = "io.kevinlee" %% "effectie-monix" % effectieVersion
lazy val effectieScalazEffect: ModuleID = "io.kevinlee" %% "effectie-scalaz-effect" % effectieVersion

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName)
    , addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)
    , addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    , scalacOptions := scalacOptionsPostProcess(
        SemVer.parseUnsafe(scalaVersion.value),
        isDotty.value,
        scalacOptions.value
      )
    , resolvers ++= Seq(
        Resolver.sonatypeRepo("releases")
        , hedgehogRepo
      )
    , scalacOptions := (SemVer.parseUnsafe(scalaVersion.value) match {
        case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
          val options = scalacOptions.value
          if (patch >= 3)
            options.filterNot(_ == "-Xlint:nullary-override")
          else
            options
        case _: SemVer =>
          scalacOptions.value
      })
    /* Ammonite-REPL { */
    , libraryDependencies ++=
      (scalaBinaryVersion.value match {
        case "2.10" =>
          Seq.empty[ModuleID]
        case "2.11" =>
          Seq("com.lihaoyi" % "ammonite" % "1.6.7" % Test cross CrossVersion.full)
        case "2.12" | "2.13" =>
          Seq("com.lihaoyi" % "ammonite" % "2.2.0" % Test cross CrossVersion.full)
        case _ =>
          Seq.empty[ModuleID]
      })
    , sourceGenerators in Test +=
      (scalaBinaryVersion.value match {
        case "2.10" =>
          task(Seq.empty[File])
        case "2.11" | "2.12" | "2.13" =>
          task {
            val file = (sourceManaged in Test).value / "amm.scala"
            IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
            Seq(file)
          }
        case _ =>
          task(Seq.empty[File])
      })
    /* } Ammonite-REPL */
    /* WartRemover and scalacOptions { */
    //      , wartremoverErrors in (Compile, compile) ++= commonWarts((scalaBinaryVersion in update).value)
    //      , wartremoverErrors in (Test, compile) ++= commonWarts((scalaBinaryVersion in update).value)
    , wartremoverErrors ++= commonWarts((scalaBinaryVersion in update).value)
    , Compile / console / wartremoverErrors := List.empty
    , Compile / console / wartremoverWarnings := List.empty
    , Compile / console / scalacOptions :=
      (console / scalacOptions).value
        .filterNot(option =>
          option.contains("wartremover") || option.contains("import")
        )
    , Test / console / wartremoverErrors := List.empty
    , Test / console / wartremoverWarnings := List.empty
    , Test / console / scalacOptions :=
      (console / scalacOptions).value
        .filterNot( option =>
          option.contains("wartremover") || option.contains("import")
        )
    /* } WartRemover and scalacOptions */
    , testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework"))
    /* Bintray { */
    , bintrayPackageLabels := Seq("Scala", "Logger", "Tagless Final", "Finally Tagless", "Functional Programming", "FP")
    , bintrayVcsUrl := Some(s"""https://github.com/$GitHubUsername/$RepoName""")
    , licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
    /* } Bintray */

    , initialCommands in console :=
      """"""

    /* Coveralls { */
    , coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          false
        case _ =>
          true
      })
    /* } Coveralls */
    )

lazy val core =
  projectCommonSettings("core", ProjectName("core"), file("core"))
  .settings(
    description  := "Logger for F[_] - Core"
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  )

lazy val slf4jLogger =
  projectCommonSettings("slf4jLogger", ProjectName("slf4j"), file("slf4j"))
  .settings(
    description  := "Logger for F[_] - Logger with Slf4j"
  , libraryDependencies ++= Seq(
        slf4jApi % Provided
      )
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  )
  .dependsOn(core)

lazy val log4sLogger =
  projectCommonSettings("log4sLogger", ProjectName("log4s"), file("log4s"))
  .settings(
    description  := "Logger for F[_] - Logger with Log4s"
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  , libraryDependencies ++= Seq(
      if (isDotty.value) {
        log4sLibForScala3 % Provided
      } else {
        (log4sLib % Provided).withDottyCompat(scalaVersion.value)
      }
    )
  )
  .dependsOn(core)

lazy val log4jLogger =
  projectCommonSettings("log4jLogger", ProjectName("log4j"), file("log4j"))
  .settings(
    description  := "Logger for F[_] - Logger with Log4j"
  , Compile / unmanagedSourceDirectories ++= {
      val sharedSourceDir = baseDirectory.value / "src/main"
      if (scalaVersion.value.startsWith("3.0"))
        Seq(
          sharedSourceDir / "scala-2.12_3.0",
          sharedSourceDir / "scala-2.13_3.0",
        )
      else if (scalaVersion.value.startsWith("2.13"))
        Seq(
          sharedSourceDir / "scala-2.12_2.13",
          sharedSourceDir / "scala-2.12_3.0",
          sharedSourceDir / "scala-2.13_3.0",
        )
      else if (scalaVersion.value.startsWith("2.12"))
        Seq(
          sharedSourceDir / "scala-2.12_2.13",
          sharedSourceDir / "scala-2.12_3.0",
          sharedSourceDir / "scala-2.11_2.12",
        )
      else if (scalaVersion.value.startsWith("2.11"))
        Seq(sharedSourceDir / "scala-2.11_2.12")
      else
        Seq.empty
    }
  , Test / unmanagedSourceDirectories ++= {
      val sharedSourceDir = baseDirectory.value / "src/test"
      if (scalaVersion.value.startsWith("3.0"))
        Seq(
          sharedSourceDir / "scala-2.12_3.0",
          sharedSourceDir / "scala-2.13_3.0",
        )
      else if (scalaVersion.value.startsWith("2.13"))
        Seq(
          sharedSourceDir / "scala-2.12_2.13",
          sharedSourceDir / "scala-2.13_3.0",
        )
      else if (scalaVersion.value.startsWith("2.12"))
        Seq(
          sharedSourceDir / "scala-2.12_2.13",
          sharedSourceDir / "scala-2.12_3.0",
          sharedSourceDir / "scala-2.11_2.12",
        )
      else if (scalaVersion.value.startsWith("2.11"))
        Seq(sharedSourceDir / "scala-2.11_2.12")
      else
        Seq.empty
    }
  , libraryDependencies ++= Seq(
        log4jApi, log4jCore
      ).map(_ % Provided)
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  )
  .dependsOn(core)

lazy val sbtLogging =
  projectCommonSettings("sbtLogging", ProjectName("sbt-logging"), file("sbt-logging"))
  .settings(
    description  := "Logger for F[_] - Logger with sbt logging"
  , libraryDependencies ++= crossVersionProps(
        List.empty
      , SemVer.parseUnsafe(scalaVersion.value)
    ) {
      case (Major(2), Minor(11)) =>
        Seq(
          sbtLoggingLib % "1.2.4"
        ).map(_ % Provided)
      case (Major(2), Minor(12)) =>
        Seq(
          sbtLoggingLib % "1.3.3"
        ).map(_ % Provided)
      case (Major(2), Minor(13)) | (Major(3), Minor(0)) =>
        Seq(
          sbtLoggingLib % "1.4.2"
        ).map(_ % Provided)
    }
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  )
  .dependsOn(core)


lazy val catsEffect =
  projectCommonSettings("catsEffect", ProjectName("cats-effect"), file("cats-effect"))
  .settings(
    description  := "Logger for F[_] - Cats Effect"
  , libraryDependencies :=
    crossVersionProps(
      hedgehogLibs
      , SemVer.parseUnsafe(scalaVersion.value)
    ) {
      case (Major(2), Minor(10)) =>
        libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover") ++
          Seq(libCatsCore, libCatsEffect)
      case (Major(2), Minor(11)) =>
        libraryDependencies.value ++ Seq(libCatsCore_2_0_0, libCatsEffect_2_0_0)
      case x =>
        libraryDependencies.value ++ Seq(libCatsCore, libCatsEffect)
    }
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  , libraryDependencies ++= Seq(effectieCatsEffect)
  )
  .dependsOn(core % IncludeTest)


lazy val monix =
  projectCommonSettings("monix", ProjectName("monix"), file(s"$RepoName-monix"))
  .settings(
    description  := "Logger for F[_] - Monix"
  , libraryDependencies :=
    crossVersionProps(
      hedgehogLibs
    , SemVer.parseUnsafe(scalaVersion.value)
    ) {
      case (Major(2), Minor(10)) =>
        libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover")
      case _ =>
        libraryDependencies.value
    }
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  , libraryDependencies ++= Seq(effectieMonix)
  )
  .dependsOn(core % IncludeTest)


lazy val scalazEffect = projectCommonSettings("scalazEffect", ProjectName("scalaz-effect"), file("scalaz-effect"))
  .settings(
    description  := "Logger for F[_] - Scalaz"
  , libraryDependencies :=
    crossVersionProps(
      hedgehogLibs
      , SemVer.parseUnsafe(scalaVersion.value)
    ) {
      case (Major(2), Minor(10)) =>
        libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover") ++
          Seq(libScalazCore, libScalazEffect)
      case _ =>
        libraryDependencies.value ++ Seq(libScalazCore, libScalazEffect)
    }
  , libraryDependencies := libraryDependenciesPostProcess(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value
    )
  , libraryDependencies ++= Seq(effectieScalazEffect)
  )
  .dependsOn(core % IncludeTest)


lazy val testCatsEffectWithSlf4jLogger =
  projectCommonSettings("testCatsEffectWithSlf4jLogger", ProjectName("test-cats-effect-slf4j"), file("test-cats-effect-slf4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, slf4jLogger, catsEffect)

lazy val testMonixWithSlf4jLogger =
  projectCommonSettings("testMonixWithSlf4jLogger", ProjectName("test-monix-slf4j"), file("test-monix-slf4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, slf4jLogger, monix)

lazy val testScalazEffectWithSlf4jLogger =
  projectCommonSettings("testScalazEffectWithSlf4jLogger", ProjectName("test-scalaz-effect-slf4j"), file("test-scalaz-effect-slf4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, slf4jLogger, scalazEffect)

lazy val testCatsEffectWithLog4sLogger =
  projectCommonSettings("testCatsEffectWithLog4sLogger", ProjectName("test-cats-effect-log4s"), file("test-cats-effect-log4s"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4s"
    , libraryDependencies ++= Seq(log4sLib, logbackClassic)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, log4sLogger, catsEffect)

lazy val testScalazEffectWithLog4sLogger =
  projectCommonSettings("testScalazEffectWithLog4sLogger", ProjectName("test-scalaz-effect-log4s"), file("test-scalaz-effect-log4s"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4s"
    , libraryDependencies ++= Seq(log4sLib, logbackClassic)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, log4sLogger, scalazEffect)

lazy val testCatsEffectWithLog4jLogger =
  projectCommonSettings("testCatsEffectWithLog4jLogger", ProjectName("test-cats-effect-log4j"), file("test-cats-effect-log4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4j"
    , libraryDependencies ++= Seq(log4jApi, log4jCore)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, log4jLogger, catsEffect)

lazy val testScalazEffectWithLog4jLogger =
  projectCommonSettings("testScalazEffectWithLog4jLogger", ProjectName("test-scalaz-effect-log4j"), file("test-scalaz-effect-log4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4j"
    , libraryDependencies ++= Seq(log4jApi, log4jCore)
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value,
        isDotty.value,
        libraryDependencies.value
      )
    )
    .settings(noPublish)
    .dependsOn(core, log4jLogger, scalazEffect)


lazy val docs = (project in file("generated-docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
      name := prefixedProjectName("docs")
    , scalacOptions := scalacOptionsPostProcess(
        SemVer.parseUnsafe(scalaVersion.value),
        isDotty.value,
        scalacOptions.value
      )
    , libraryDependencies := libraryDependenciesPostProcess(
        scalaVersion.value, isDotty.value, libraryDependencies.value
      )
    , mdocVariables := Map(
        "VERSION" -> (ThisBuild / version).value,
        "SUPPORTED_SCALA_VERSIONS" -> {
          val versions = CrossScalaVersions.map(v => s"`$v`")
          if (versions.length > 1)
            s"${versions.init.mkString(", ")} and ${versions.last}"
          else
            versions.mkString
        }
      )
    , docusaurDir := (ThisBuild / baseDirectory).value / "website"
    , docusaurBuildDir := docusaurDir.value / "build"

    , gitHubPagesOrgName := GitHubUsername
    , gitHubPagesRepoName := RepoName
  )
  .settings(noPublish)
  .dependsOn(
    core,
    slf4jLogger,
    log4jLogger,
    sbtLogging,
    catsEffect,
    scalazEffect,
    monix,
  )


lazy val loggerF = (project in file("."))
  .enablePlugins(DevOopsGitReleasePlugin)
  .settings(
    name := prefixedProjectName("")
  , description := "Logger for F[_]"
  , libraryDependencies := libraryDependenciesRemoveDottyIncompatible(
      scalaVersion.value,
      isDotty.value,
      libraryDependencies.value,
    )
  /* GitHub Release { */
  , gitTagFrom := "main"
  , devOopsPackagedArtifacts := List(s"*/target/scala-*/${name.value}*.jar")
  /* } GitHub Release */
  )
  .settings(noPublish)
  .aggregate(
    core,
    slf4jLogger,
    log4sLogger,
    log4jLogger,
    sbtLogging,
    catsEffect,
    scalazEffect,
    monix,
  )
