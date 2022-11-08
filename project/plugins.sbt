logLevel := sbt.Level.Warn

addDependencyTreePlugin

addSbtPlugin("com.github.sbt"  % "sbt-ci-release"  % "1.5.10")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.15")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "2.0.6")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.3.2")
addSbtPlugin("org.scalameta"   % "sbt-mdoc"        % "2.3.2")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.12.0")

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.9.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

val sbtDevOopsVersion = "2.23.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter"   % sbtDevOopsVersion)
