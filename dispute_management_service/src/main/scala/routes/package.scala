import config.HttpConfig
import services.ServicesHandler.Services

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.implicits._

package object routes {

  def initHttpApp(
      httpConfig: HttpConfig,
      transactor: Transactor[IO],
      services: Services[IO]
    ): HttpApp[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "ping" =>
        Ok("PONG")
      case GET -> Root / "test" =>
        Ok(services.users.confirm("000").map(_.toString))
    }.orNotFound

}
