import ProjectInfo.{ProjectName, _}
import kevinlee.sbt.SbtCommon.crossVersionProps
import just.semver.SemVer
import SemVer.{Major, Minor}
import microsites.ConfigYml

val ProjectScalaVersion: String = "2.13.1"
val CrossScalaVersions: Seq[String] = Seq("2.11.12", "2.12.11", ProjectScalaVersion)
val IncludeTest: String = "compile->compile;test->test"

lazy val hedgehogVersion = "97854199ef795a5dfba15478fd9abe66035ddea2"
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

lazy val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.13.1"
lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.13.1"

ThisBuild / scalaVersion     := ProjectScalaVersion
ThisBuild / version          := ProjectVersion
ThisBuild / organization     := "io.kevinlee"
ThisBuild / organizationName := "Kevin's Code"
ThisBuild / crossScalaVersions := CrossScalaVersions
ThisBuild / developers   := List(
  Developer("Kevin-Lee", "Kevin Lee", "kevin.code@kevinlee.io", url("https://github.com/Kevin-Lee"))
)
ThisBuild / homepage := Some(url("https://github.com/Kevin-Lee/logger-f"))
ThisBuild / scmInfo :=
  Some(ScmInfo(
    url("https://github.com/Kevin-Lee/logger-f")
    , "git@github.com:Kevin-Lee/logger-f.git"
  ))

def prefixedProjectName(name: String) = s"logger-f${if (name.isEmpty) "" else s"-$name"}"

lazy val noPublish: SettingsDefinition = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in packagedArtifacts := true,
  skip in publish := true
)

val effectieVersion = "0.3.0"
lazy val effectieCatsEffect: ModuleID = "io.kevinlee" %% "effectie-cats-effect" % effectieVersion
lazy val effectieScalazEffect: ModuleID = "io.kevinlee" %% "effectie-scalaz-effect" % effectieVersion

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName)
    , unmanagedSourceDirectories in Compile ++= {
      val sharedSourceDir = baseDirectory.value / "src/main"
      if (scalaVersion.value.startsWith("2.13") || scalaVersion.value.startsWith("2.12"))
        Seq(sharedSourceDir / "scala-2.12_2.13")
      else
        Seq(sharedSourceDir / "scala-2.10_2.11")
    }
    , resolvers ++= Seq(
      Resolver.sonatypeRepo("releases")
      , hedgehogRepo
    )
    , addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)
    , addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    /* Ammonite-REPL { */
    , libraryDependencies ++=
      (scalaBinaryVersion.value match {
        case "2.10" =>
          Seq.empty[ModuleID]
        case "2.11" =>
          Seq("com.lihaoyi" % "ammonite" % "1.6.7" % Test cross CrossVersion.full)
        case "2.12" =>
          Seq.empty[ModuleID] // TODO: add ammonite when it supports Scala 2.12.11
        case _ =>
          Seq("com.lihaoyi" % "ammonite" % "2.0.4" % Test cross CrossVersion.full)
      })
    , sourceGenerators in Test +=
      (scalaBinaryVersion.value match {
        case "2.10" =>
          task(Seq.empty[File])
        case "2.12" =>
          task(Seq.empty[File]) // TODO: add ammonite when it supports Scala 2.12.11
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
    , bintrayVcsUrl := Some("""git@github.com:Kevin-Lee/logger-f.git""")
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
    description  := "Logger for Tagless Final - Core"
  )

lazy val slf4jLogger =
  projectCommonSettings("slf4jLogger", ProjectName("slf4j"), file("slf4j"))
  .settings(
    description  := "Logger for Tagless Final - Logger with Slf4j"
  , libraryDependencies ++= Seq(
        slf4jApi % Provided
      )
  )
  .dependsOn(core)

lazy val log4jLogger =
  projectCommonSettings("log4jLogger", ProjectName("log4j"), file("log4j"))
  .settings(
    description  := "Logger for Tagless Final - Logger with Log4j"
  , libraryDependencies ++= Seq(
        log4jApi, log4jCore
      ).map(_ % Provided)
  )
  .dependsOn(core)


lazy val catsEffect =
  projectCommonSettings("catsEffect", ProjectName("cats-effect"), file("cats-effect"))
  .settings(
    description  := "Logger for Tagless Final - Core"
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
    description  := "Logger for Tagless Final - Scalaz"
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
      description  := "Test Logger for Tagless Final - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    )
    .dependsOn(core, slf4jLogger, catsEffect)

lazy val testScalazEffectWithSlf4jLogger =
  projectCommonSettings("testScalazEffectWithSlf4jLogger", ProjectName("test-scalaz-effect-slf4j"), file("test-scalaz-effect-slf4j"))
    .settings(
      description  := "Test Logger for Tagless Final - Logger with Slf4j"
    , libraryDependencies ++= Seq(slf4jApi, logbackClassic)
    )
    .dependsOn(core, slf4jLogger, scalazEffect)

lazy val testCatsEffectWithLog4jLogger =
  projectCommonSettings("testCatsEffectWithLog4jLogger", ProjectName("test-cats-effect-log4j"), file("test-cats-effect-log4j"))
    .settings(
      description  := "Test Logger for Tagless Final - Logger with Log4j"
    , libraryDependencies ++= Seq(log4jApi, log4jCore)
    )
    .dependsOn(core, log4jLogger, catsEffect)

lazy val testScalazEffectWithLog4jLogger =
  projectCommonSettings("testScalazEffectWithLog4jLogger", ProjectName("test-scalaz-effect-log4j"), file("test-scalaz-effect-log4j"))
    .settings(
      description  := "Test Logger for Tagless Final - Logger with Log4j"
    , libraryDependencies ++= Seq(log4jApi, log4jCore)
    )
    .dependsOn(core, log4jLogger, scalazEffect)



lazy val docDir = file("docs")
lazy val docs = (project in docDir)
  .enablePlugins(MicrositesPlugin)
  .settings(
      name := prefixedProjectName("docs")
    /* microsites { */
    , micrositeName := prefixedProjectName("")
    , micrositeAuthor := "Kevin Lee"
    , micrositeHomepage := "https://blog.kevinlee.io"
    , micrositeDescription := "Logger for F[_]"
    , micrositeGithubOwner := "Kevin-Lee"
    , micrositeGithubRepo := "logger-f"
    , micrositeBaseUrl := "/logger-f"
    , micrositeDocumentationUrl := s"${micrositeBaseUrl.value}/docs"
    , micrositePushSiteWith := GitHub4s
    , micrositeGithubToken := sys.env.get("GITHUB_TOKEN")
    , micrositeTheme := "pattern"
    , micrositeHighlightTheme := "atom-one-light"
    , micrositeGitterChannel := false
    , micrositeGithubLinks := false
    , micrositeShareOnSocial := false
    , micrositeHighlightLanguages ++= Seq("shell")

    , micrositeConfigYaml := ConfigYml(
      yamlPath = Some(docDir / "microsite" / "_config.yml")
    )
    , micrositeImgDirectory := docDir / "microsite" / "img"
    , micrositeCssDirectory := docDir / "microsite" / "css"
    , micrositeSassDirectory := docDir / "microsite" / "sass"
    , micrositeJsDirectory := docDir / "microsite" / "js"
    , micrositeExternalLayoutsDirectory := docDir / "microsite" / "layouts"
    , micrositeExternalIncludesDirectory := docDir / "microsite" / "includes"
    , micrositeDataDirectory := docDir / "microsite" / "data"
    , micrositeStaticDirectory := docDir / "microsite" / "static"
    , micrositeExtraMdFilesOutput := docDir / "microsite" / "extra_md"
    , micrositePluginsDirectory := docDir / "microsite" / "plugins"

    /* } microsites */

  )
  .settings(noPublish)
  .dependsOn(catsEffect)

lazy val loggerF = (project in file("."))
  .enablePlugins(DevOopsGitReleasePlugin)
  .settings(
    name := prefixedProjectName("")
  , description := "Logger for Tagless Final"
  )
  .dependsOn(core, slf4jLogger, log4jLogger, catsEffect, scalazEffect)
