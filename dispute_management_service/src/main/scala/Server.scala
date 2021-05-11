import routes.httpApp

import cats.effect.{ConcurrentEffect, IO, Timer}
import org.http4s.HttpApp
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext

object Server {
  val host = sys.env.getOrElse("SERVICE_HOST", "0.0.0.0")
  val port = sys.env.getOrElse("SERVICE_PORT", "8080").toInt

  def start(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO]
    ): IO[Unit] = {
    server(httpApp)
  }

  private[this] def server(
      routes: HttpApp[IO]
    )(implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO]
    ): IO[Unit] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port, host)
      .withHttpApp(routes)
      .serve
      .compile
      .drain

}
