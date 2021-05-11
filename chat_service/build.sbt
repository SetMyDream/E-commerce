name := "chat"

version := "1.0-SNAPSHOT"

val akkaVersion = "2.5.25"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
    scalaVersion := "2.13.4"
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "org.scodec" %% "scodec-core" % "1.11.4",
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "org.scalatest" %% "scalatest" % "3.0.8" % Test,
        "org.webjars" % "bootstrap" % "3.3.6",
    )
