name := "disp-man"

scalaVersion := "2.13.4"

libraryDependencies ++= {
  val http4sVersion = "0.21.22"
  val pureConfigVersion = "0.14.1"
  val doobieVersion = "0.12.1"
  val enumeratumVersion = "1.6.0"

  Seq(
    "org.http4s"            %% "http4s-dsl"             % http4sVersion,
    "org.http4s"            %% "http4s-blaze-server"    % http4sVersion,
    "org.http4s"            %% "http4s-blaze-client"    % http4sVersion,
    "com.github.pureconfig" %% "pureconfig"             % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigVersion,
    "ch.qos.logback"         % "logback-classic"        % "1.2.3",
    "org.postgresql"         % "postgresql"             % "42.2.19",
    "org.flywaydb"           % "flyway-core"            % "7.8.2",
    "org.tpolecat"          %% "doobie-core"            % doobieVersion,
    "org.tpolecat"          %% "doobie-hikari"          % doobieVersion,
    "org.tpolecat"          %% "doobie-postgres"        % doobieVersion,
    "org.tpolecat"          %% "doobie-quill"           % doobieVersion,
    "com.beachape"          %% "enumeratum-quill"       % enumeratumVersion
  )
}

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

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
