logLevel := sbt.Level.Warn

addDependencyTreePlugin

addSbtPlugin("com.github.sbt"  % "sbt-ci-release"  % "1.11.2")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.4.3")
addSbtPlugin("ch.epfl.scala"   % "sbt-scalafix"    % "0.14.5")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"    % "2.5.6")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "2.4.4")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.3.15")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.20.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")

addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.5.9")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")

addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.2")

val sbtDevOopsVersion = "3.3.2"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter"   % sbtDevOopsVersion)
