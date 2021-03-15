name := "user_management"

scalaVersion := "2.13.4"

enablePlugins(PlayService, PlayLayoutPlugin)

val slickVersion = "5.0.0"
val silhouetteVersion = "7.0.0"

libraryDependencies ++= {
  Seq(
    guice,
    ehcache,
    "net.codingwell"         %% "scala-guice"                     % "4.2.6",
    "org.typelevel"          %% "cats-core"                       % "2.2.0",
    "net.logstash.logback"    % "logstash-logback-encoder"        % "6.2",
    "com.typesafe.play"      %% "play-json-joda"                  % "2.9.2",
    "com.typesafe.play"      %% "play-slick"                      % slickVersion,
    "com.typesafe.play"      %% "play-slick-evolutions"           % slickVersion,
    "org.postgresql"          % "postgresql"                      % "42.2.19",
    "com.mohiva"             %% "play-silhouette"                 % silhouetteVersion,
    "com.mohiva"             %% "play-silhouette-password-bcrypt" % silhouetteVersion,
    "com.mohiva"             %% "play-silhouette-persistence"     % silhouetteVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"              % "5.0.0" % Test,
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
  "-Ywarn-numeric-widen",
  "-Xfatal-warnings"
)
scalacOptions in Test ++= Seq("-Yrangepos")

autoAPIMappings := true
