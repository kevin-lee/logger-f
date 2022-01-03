logLevel := sbt.Level.Warn

addDependencyTreePlugin

addSbtPlugin("com.github.sbt"  % "sbt-ci-release"  % "1.5.10")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.15")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"   % "1.9.2")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"   % "1.3.1")
addSbtPlugin("org.scalameta"   % "sbt-mdoc"        % "2.2.22")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.8.1")

val sbtDevOopsVersion = "2.14.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
