// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")

// The sbt native packager - needed for docker builds
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.0")

// For code coverage test
addSbtPlugin("com.beautiful-scala" % "sbt-scalastyle" % "1.5.0")

// For checkstyle
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// For formatting Scala source code
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// Build fat jar file
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")