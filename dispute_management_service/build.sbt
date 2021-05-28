name := "disp-man"

scalaVersion := "2.13.6"

libraryDependencies ++= {
  val http4sVersion = "0.21.22"
  val pureConfigVersion = "0.14.1"
  val doobieVersion = "0.12.1"
  val enumeratumVersion = "1.6.0"

  Seq(
    "org.http4s"            %% "http4s-dsl"              % http4sVersion,
    "org.http4s"            %% "http4s-blaze-server"     % http4sVersion,
    "org.http4s"            %% "http4s-blaze-client"     % http4sVersion,
    "org.http4s"            %% "http4s-circe"            % http4sVersion,
    "com.github.pureconfig" %% "pureconfig"              % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect"  % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-http4s"       % pureConfigVersion,
    "io.circe"              %% "circe-generic"           % "0.13.0",
    "ch.qos.logback"         % "logback-classic"         % "1.2.3",
    "org.postgresql"         % "postgresql"              % "42.2.20",
    "org.flywaydb"           % "flyway-core"             % "7.9.1",
    "org.tpolecat"          %% "doobie-core"             % doobieVersion,
    "org.tpolecat"          %% "doobie-hikari"           % doobieVersion,
    "org.tpolecat"          %% "doobie-postgres"         % doobieVersion,
    "org.tpolecat"          %% "doobie-quill"            % doobieVersion,
    "com.beachape"          %% "enumeratum-quill"        % enumeratumVersion,
    "org.scalatest"         %% "scalatest"               % "3.2.9"   % Test,
    "org.scalatestplus"     %% "scalacheck-1-15"         % "3.2.9.0" % Test,
    "org.scalamock"         %% "scalamock"               % "5.1.0"   % Test,
  )
}

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin(
  "org.typelevel" % "kind-projector" % "0.13.0" cross CrossVersion.full
)

scalacOptions ~= (_.filterNot(
  Set(
//  "-Wunused:imports",
//  "-Wunused:implicits",
    "-Wunused:explicits",
    "-Wunused:locals",
    "-Wunused:params",
    "-Wunused:patvars",
    "-Wunused:privates"
  )
))

scalacOptions in Test ~= (_.filterNot(
  Set(
    "-Wdead-code"
  )
))
