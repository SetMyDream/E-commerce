name := "user_management"

scalaVersion := "2.13.4"

enablePlugins(PlayService, PlayLayoutPlugin)

libraryDependencies ++= {
  Seq(
    guice,
    "net.codingwell" %% "scala-guice" % "4.2.6",
    "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
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
