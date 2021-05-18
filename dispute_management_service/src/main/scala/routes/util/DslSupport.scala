package routes.util

import cats.effect.IO
import org.http4s.{Challenge, Response}
import org.http4s.dsl.io._
import org.http4s.headers.`WWW-Authenticate`

object DslSupport {
  val REALM = "Dispute management service"

  def unauthorized(scheme: String): IO[Response[IO]] =
    Unauthorized(`WWW-Authenticate`(Challenge(scheme, REALM)))

}
