name := "reporting_service"

scalaVersion := "2.13.6"

libraryDependencies ++= {
  val http4sVersion = "0.21.+"
  Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion
  )
}