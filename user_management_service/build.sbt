name := "ecomm-user"
version := "1.0.0"

scalaVersion := "2.13.4"

enablePlugins(PlayService, PlayLayoutPlugin)

val slickVersion = "5.0.0"
val silhouetteVersion = "7.0.0"
val akkaVersion = "2.6.10"

libraryDependencies ++= {
  Seq(
    guice,
    ehcache,
    ws,
    "net.codingwell"         %% "scala-guice"                     % "4.2.6",
    "org.typelevel"          %% "cats-core"                       % "2.2.0",
    "net.logstash.logback"    % "logstash-logback-encoder"        % "6.2",
    "com.typesafe.play"      %% "play-json-joda"                  % "2.9.2",
    "com.typesafe.akka"      %% "akka-actor-typed"                % akkaVersion,
    "com.typesafe.akka"      %% "akka-actor-testkit-typed"        % akkaVersion % Test,
    "com.typesafe.play"      %% "play-slick"                      % slickVersion,
    "com.typesafe.play"      %% "play-slick-evolutions"           % slickVersion,
    "org.postgresql"          % "postgresql"                      % "42.2.19",
    "com.mohiva"             %% "play-silhouette"                 % silhouetteVersion,
    "com.mohiva"             %% "play-silhouette-password-bcrypt" % silhouetteVersion,
    "com.mohiva"             %% "play-silhouette-persistence"     % silhouetteVersion,
    "org.scalatest"          %% "scalatest"                       % "3.2.7"     % Test,
    "org.scalatestplus"      %% "scalacheck-1-15"                 % "3.2.7.0"   % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"              % "5.1.0"     % Test,
    "org.webjars"             % "swagger-ui"                      % "3.45.0",
    // This is a fork from io.swagger version, which doesn't support Play 2.8 at the moment
    // Change to the official version when it gets upgraded
    "com.github.dwickern" %% "swagger-play2.8" % "3.1.0"
  )
}

resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"

scalafmtConfig := baseDirectory.value / "conf" / "formatting.scalafmt.conf"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wnumeric-widen",
  "-Xfatal-warnings"
)

autoAPIMappings := true

enablePlugins(DockerPlugin)
Docker / daemonUser := "userman-daemon"

// Fixes java.nio.file.AccessDeniedException when started from Docker
// https://stackoverflow.com/questions/56153102
Universal / javaOptions ++= Seq(
  "-Dpidfile.path=/dev/null"
)

