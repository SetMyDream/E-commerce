name := """product_inventory"""

version := "1.0-SNAPSHOT"
val AkkaVersion = "2.5.31"
lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin, PlayLayoutPlugin)
  .settings(
    name := """play-scala-slick-example""",
    version := "2.8.x",
    scalaVersion := "2.13.4",
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "com.typesafe.slick" %% "slick" % "3.3.2",
      "org.postgresql" % "postgresql" % "42.1.4",
      "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "2.0.2",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
//      "org.scalatest" %% "scalatest" % "3.2.5" % "test",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      specs2 % Test, evolutions, jdbc,
      "com.iterable" %% "swagger-play" % "2.0.1",
      "org.webjars" % "swagger-ui" % "3.45.0",
      "org.webjars" %% "webjars-play" % "2.8.0"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    ),
    resolvers += Resolver.sonatypeRepo("releases")
  )

scalacOptions ++= Seq(
  // Warnings propogates as errors
  "-Xfatal-warnings",
  "-language:implicitConversions",
  // turns all warnings into errors ;-)
  "-target:jvm-1.8",
  "-language:reflectiveCalls",
  "-Xfatal-warnings",
  // possibly old/deprecated linter options
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Xlog-free-terms",
  // enables linter options
  "-Xlint:adapted-args", // warn if an argument list is modified to match the receiver
  "-Xlint:nullary-unit", // warn when nullary methods return Unit
  "-Xlint:inaccessible", // warn about inaccessible types in method signatures
  "-Xlint:nullary-override", // warn when non-nullary `def f()' overrides nullary `def f'
  "-Xlint:infer-any", // warn when a type argument is inferred to be `Any`
  "-Xlint:-missing-interpolator", // disables missing interpolator warning
  "-Xlint:doc-detached", // a ScalaDoc comment appears to be detached from its element
  "-Xlint:private-shadow", // a private field (or class parameter) shadows a superclass field
  "-Xlint:type-parameter-shadow", // a local type parameter shadows a type already in scope
  "-Xlint:poly-implicit-overload", // parameterized overloaded implicit methods are not visible as view bounds
  "-Xlint:option-implicit", // Option.apply used implicit view
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator
  "-Xlint:package-object-classes", // Class or object defined in package object
  "-Xlint:unsound-match" // Pattern match may not be typesafe
)

scalacOptions in Test ++= Seq("-Yrangepos")

javacOptions ++= Seq(
  "-Xlint:unchecked",
  "-Xlint:deprecation"
)
