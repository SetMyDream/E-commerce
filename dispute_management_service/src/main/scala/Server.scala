import config._
import routes.initHttpApp
import services.ServicesHandler.servicesRes
import storage.db.{transactorRes, Migrations}

import cats.effect._
import org.http4s.HttpApp
import org.http4s.server.{Server => BlazeServer}
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext

class Server(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO],
      S: ContextShift[IO]) {

  def start: Resource[IO, BlazeServer[IO]] = {
    for {
      Config(serverConfig, httpConfig, dbConfig, clientConfig) <- configRes[IO]
      transactor <- transactorRes[IO](dbConfig)
      _ <- Resource.eval(Migrations.applyMigrations(transactor))
      services <- servicesRes[IO](clientConfig, httpConfig)
      httpApp = initHttpApp(httpConfig, transactor, services)
      server <- serverRes(serverConfig, httpApp)
    } yield server
  }

  private[this] def serverRes(
      config: ServerConfig,
      routes: HttpApp[IO]
    ): Resource[IO, BlazeServer[IO]] = {
    import config._
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port, host)
      .withHttpApp(routes)
      .resource
  }

}

object Server {
  def init(
      implicit
      C: ConcurrentEffect[IO],
      T: Timer[IO],
      S: ContextShift[IO]
    ): Server = new Server()
}
