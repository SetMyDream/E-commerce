package routes

import controllers.DisputeInfoController
import routes.middleware.UserAuthMiddleware
import storage.db.repo.DisputeRepository
import services.exceptions.AuthException

import cats.effect.{IO, Sync}
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import io.circe.generic.auto._

object DisputeInfoRoutes {

  def apply(
      authenticated: UserAuthMiddleware,
      repository: DisputeRepository[IO]
    )(implicit F: Sync[IO]): HttpRoutes[IO] = {
    val ctrl = new DisputeInfoController(repository)

    authenticated {
      AuthedRoutes.of[Long, IO] {
        case GET -> Root / "info" / LongVar(disputeId) as userId =>
          ctrl.get(disputeId, userId).flatMap {
            case Right(dispute) => dispute.fold(NotFound())(Ok(_))
            case Left(AuthException.Forbidden) => Forbidden()
          }

      }
    }
  }

}
