import ProjectInfo.{ProjectName, _}
import kevinlee.sbt.SbtCommon.crossVersionProps
import just.semver.SemVer
import SemVer.{Major, Minor}

val ProjectScalaVersion: String = "2.13.3"
val CrossScalaVersions: Seq[String] = Seq("2.11.12", "2.12.12", ProjectScalaVersion)
val IncludeTest: String = "compile->compile;test->test"

lazy val hedgehogVersion = "0.4.2"
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

lazy val log4sLib: ModuleID = "org.log4s" %% "log4s" % "1.8.2"

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
def scalacOptionsPostProcess(scalaSemVer: SemVer, options: Seq[String]): Seq[String] = scalaSemVer match {
  case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
    if (patch >= 3)
      options.filterNot(_ == "-Xlint:nullary-override")
    else
      options
  case _: SemVer =>
    options
}

def prefixedProjectName(name: String) = s"$RepoName${if (name.isEmpty) "" else s"-$name"}"

lazy val noPublish: SettingsDefinition = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in sbt.Keys.`package` := true,
  skip in packagedArtifacts := true,
  skip in publish := true
)

val effectieVersion: String = "1.3.0"
lazy val effectieCatsEffect: ModuleID = "io.kevinlee" %% "effectie-cats-effect" % effectieVersion
lazy val effectieScalazEffect: ModuleID = "io.kevinlee" %% "effectie-scalaz-effect" % effectieVersion

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName)
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
    , addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)
    , addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    /* Ammonite-REPL { */
    , libraryDependencies ++=
      (scalaBinaryVersion.value match {
        case "2.10" =>
          Seq.empty[ModuleID]
        case "2.11" =>
          Seq("com.lihaoyi" % "ammonite" % "1.6.7" % Test cross CrossVersion.full)
        case _ =>
          Seq("com.lihaoyi" % "ammonite" % "2.2.0" % Test cross CrossVersion.full)
      })
    , sourceGenerators in Test +=
      (scalaBinaryVersion.value match {
        case "2.10" =>
          task(Seq.empty[File])
        case _ =>
          task {
            val file = (sourceManaged in Test).value / "amm.scala"
            IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
            Seq(file)
          }
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
  )

lazy val slf4jLogger =
  projectCommonSettings("slf4jLogger", ProjectName("slf4j"), file("slf4j"))
  .settings(
    description  := "Logger for F[_] - Logger with Slf4j"
  , libraryDependencies ++= Seq(
        slf4jApi % Provided
      )
  )
  .dependsOn(core)

lazy val log4sLogger =
  projectCommonSettings("log4sLogger", ProjectName("log4s"), file("log4s"))
  .settings(
    description  := "Logger for F[_] - Logger with Log4s"
  , libraryDependencies ++= Seq(
        log4sLib % Provided
      )
  )
  .dependsOn(core)

lazy val log4jLogger =
  projectCommonSettings("log4jLogger", ProjectName("log4j"), file("log4j"))
  .settings(
    description  := "Logger for F[_] - Logger with Log4j"
  , unmanagedSourceDirectories in Compile ++= {
      val sharedSourceDir = baseDirectory.value / "src/main"
      if (scalaVersion.value.startsWith("2.13") || scalaVersion.value.startsWith("2.12"))
        Seq(sharedSourceDir / "scala-2.12_2.13")
      else
        Seq(sharedSourceDir / "scala-2.10_2.11")
    }
  , libraryDependencies ++= Seq(
        log4jApi, log4jCore
      ).map(_ % Provided)
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
      case (Major(2), Minor(13)) =>
        Seq(
          sbtLoggingLib % "1.3.2"
        ).map(_ % Provided)
    }
  )
  .dependsOn(core)


lazy val catsEffect =
  projectCommonSettings("catsEffect", ProjectName("cats-effect"), file("cats-effect"))
  .settings(
    description  := "Logger for F[_] - Core"
  , libraryDependencies :=
    crossVersionProps(
      hedgehogLibs ++ Seq(effectieCatsEffect)
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
  )
  .dependsOn(core % IncludeTest)


lazy val scalazEffect = projectCommonSettings("scalazEffect", ProjectName("scalaz-effect"), file("scalaz-effect"))
  .settings(
    description  := "Logger for F[_] - Scalaz"
  , libraryDependencies :=
    crossVersionProps(
      hedgehogLibs ++ Seq(effectieScalazEffect)
      , SemVer.parseUnsafe(scalaVersion.value)
    ) {
      case (Major(2), Minor(10)) =>
        libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover") ++
          Seq(libScalazCore, libScalazEffect)
      case (Major(2), Minor(11)) =>
        libraryDependencies.value ++ Seq(libScalazCore, libScalazEffect)
      case x =>
        libraryDependencies.value ++ Seq(libScalazCore, libScalazEffect)
    }
  )
  .dependsOn(core % IncludeTest)


lazy val testCatsEffectWithSlf4jLogger =
  projectCommonSettings("testCatsEffectWithSlf4jLogger", ProjectName("test-cats-effect-slf4j"), file("test-cats-effect-slf4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    )
    .settings(noPublish)
    .dependsOn(core, slf4jLogger, catsEffect)

lazy val testScalazEffectWithSlf4jLogger =
  projectCommonSettings("testScalazEffectWithSlf4jLogger", ProjectName("test-scalaz-effect-slf4j"), file("test-scalaz-effect-slf4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    )
    .settings(noPublish)
    .dependsOn(core, slf4jLogger, scalazEffect)

lazy val testCatsEffectWithLog4sLogger =
  projectCommonSettings("testCatsEffectWithLog4sLogger", ProjectName("test-cats-effect-log4s"), file("test-cats-effect-log4s"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4s"
    , libraryDependencies ++= Seq(log4sLib, logbackClassic)
    )
    .settings(noPublish)
    .dependsOn(core, log4sLogger, catsEffect)

lazy val testScalazEffectWithLog4sLogger =
  projectCommonSettings("testScalazEffectWithLog4sLogger", ProjectName("test-scalaz-effect-log4s"), file("test-scalaz-effect-log4s"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4s"
    , libraryDependencies ++= Seq(log4sLib, logbackClassic)
    )
    .settings(noPublish)
    .dependsOn(core, log4sLogger, scalazEffect)

lazy val testCatsEffectWithLog4jLogger =
  projectCommonSettings("testCatsEffectWithLog4jLogger", ProjectName("test-cats-effect-log4j"), file("test-cats-effect-log4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4j"
    , libraryDependencies ++= Seq(log4jApi, log4jCore)
    )
    .settings(noPublish)
    .dependsOn(core, log4jLogger, catsEffect)

lazy val testScalazEffectWithLog4jLogger =
  projectCommonSettings("testScalazEffectWithLog4jLogger", ProjectName("test-scalaz-effect-log4j"), file("test-scalaz-effect-log4j"))
    .settings(
      description  := "Test Logger for F[_] - Logger with Log4j"
    , libraryDependencies ++= Seq(log4jApi, log4jCore)
    )
    .settings(noPublish)
    .dependsOn(core, log4jLogger, scalazEffect)


lazy val docs = (project in file("generated-docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
      name := prefixedProjectName("docs")
    , scalacOptions := scalacOptionsPostProcess(SemVer.parseUnsafe(scalaVersion.value), scalacOptions.value)
    , mdocVariables := Map(
        "VERSION" -> (ThisBuild / version).value
      )
    , docusaurDir := (ThisBuild / baseDirectory).value / "website"
    , docusaurBuildDir := docusaurDir.value / "build"

    , gitHubPagesOrgName := GitHubUsername
    , gitHubPagesRepoName := RepoName
  )
  .settings(noPublish)
  .dependsOn(core, slf4jLogger, log4jLogger, sbtLogging, catsEffect, scalazEffect)


lazy val loggerF = (project in file("."))
  .enablePlugins(DevOopsGitReleasePlugin)
  .settings(
    name := prefixedProjectName("")
  , description := "Logger for F[_]"
  /* GitHub Release { */
  , gitTagFrom := "main"
  , devOopsPackagedArtifacts := List(s"*/target/scala-*/${name.value}*.jar")
  /* } GitHub Release */
  )
  .settings(noPublish)
  .aggregate(
    core,
    slf4jLogger,
    log4jLogger,
    sbtLogging,
    catsEffect,
    scalazEffect,
    docs
  )
