// For formatting Scala source code
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")

// The sbt native packager - needed for docker builds
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")

// Build fat jar file
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

// Load testing tool:
// http://gatling.io/docs/2.2.2/extensions/sbt_plugin.html
addSbtPlugin("io.gatling" % "gatling-sbt" % "3.0.0")

// Scala formatting: "sbt scalafmt"
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "2.4.2")
