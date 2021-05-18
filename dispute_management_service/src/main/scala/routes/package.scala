import config.HttpConfig
import services.ServicesHandler.Services
import storage.db.repo.DisputeRepository

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.http4s.HttpApp
import org.http4s.implicits._

package object routes {

  def initHttpApp(
      httpConfig: HttpConfig,
      transactor: Transactor[IO],
      services: Services[IO]
    ): HttpApp[IO] = {
    import services._
    val disputeRepository = new DisputeRepository(transactor)

    DisputeInfoRoutes(httpConfig, users, disputeRepository).orNotFound
  }

}
