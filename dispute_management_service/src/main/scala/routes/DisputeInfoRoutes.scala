package routes

import controllers.DisputeInfoController
import config.HttpConfig
import routes.util.DslSupport._
import storage.db.repo.DisputeRepository
import services.UserService
import services.exceptions.AuthException

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import io.circe.generic.auto._

object DisputeInfoRoutes {

  def apply(
      httpConfig: HttpConfig,
      userService: UserService[IO],
      repository: DisputeRepository[IO]
    ): HttpRoutes[IO] = {
    import httpConfig._
    val ctrl = new DisputeInfoController(userService, repository)

    HttpRoutes.of[IO] {
      case req @ GET -> Root / "info" / LongVar(disputeId) =>
        req.headers.get(authTokenHeader.ci) match {
          case None => unauthorized(authTokenHeader)
          case Some(authToken) =>
            ctrl.get(disputeId, authToken.value).flatMap {
              case Right(Some(dispute)) => Ok(dispute)
              case Right(None) => NotFound()
              case Left(AuthException.Unauthorized) => unauthorized(authTokenHeader)
              case Left(AuthException.Forbidden) => Forbidden()
            }
        }

    }
  }

}
