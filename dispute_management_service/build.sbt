name := "dispute_management"

scalaVersion := "2.13.4"

libraryDependencies ++= {
  val http4sVersion = "0.21.22"
  val pureConfigVersion = "0.14.1"

  Seq(
    "org.http4s"            %% "http4s-dsl"             % http4sVersion,
    "org.http4s"            %% "http4s-blaze-server"    % http4sVersion,
    "com.github.pureconfig" %% "pureconfig"             % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,
    "ch.qos.logback"         % "logback-classic"        % "1.2.3"
  )
}

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
