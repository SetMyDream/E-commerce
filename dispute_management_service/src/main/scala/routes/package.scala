import cats.effect.IO
import doobie.util.transactor.Transactor
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.implicits._

package object routes {

  def initHttpApp(transactor: Transactor[IO]): HttpApp[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root / "ping" =>
        Ok("PONG")
    }.orNotFound
  }

}
