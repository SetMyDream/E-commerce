import cats.effect.IO
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.implicits._

package object routes {

  def httpApp: HttpApp[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root / "ping" =>
        Ok("PONG")
    }.orNotFound
  }
}
