lazy val playVersion = "2.8.8"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % playVersion)

// sbteclipse
//addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// The sbt native packager - needed for docker builds
//addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

// Multiple terminal command threads
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")


