name := "user_management"

scalaVersion := "2.13.4"

enablePlugins(PlayService, PlayLayoutPlugin)

val slickVersion = "5.0.0"

libraryDependencies ++= {
  Seq(
    guice,
    "net.codingwell" %% "scala-guice" % "4.2.6",
    "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
    "com.typesafe.play" %% "play-slick" % slickVersion,
    "com.typesafe.play" %% "play-slick-evolutions" % slickVersion,
    "org.postgresql" % "postgresql" % "42.2.19",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
  )
}

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-numeric-widen",
  "-Xfatal-warnings"
)
scalacOptions in Test ++= Seq("-Yrangepos")

autoAPIMappings := true
