package services

import config.HttpConfig
import services.exceptions.Unauthorized
import services.model.UserIdentity

import cats.syntax.functor._
import cats.syntax.either._
import cats.syntax.applicativeError._
import cats.effect.Sync
import org.http4s._
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.Status.{Unauthorized => Unauthorized401}
import org.http4s.circe.CirceEntityDecoder._
import io.circe.generic.auto._

class UserService[F[_]: Sync](
      client: Client[F],
      baseTargetURI: Uri,
      httpConfig: HttpConfig) {

  /** Ask user management service for userId given the auth token */
  def confirm(token: String): F[Either[Unauthorized.type, Long]] = {
    val tokenHeader = Header(httpConfig.authTokenHeader, token)
    val request = Request[F](
      uri = baseTargetURI / "user",
      headers = Headers.of(tokenHeader)
    )
    client
      .expect[UserIdentity](request)
      .map(_.id.asRight[Unauthorized.type])
      .recover {
        case UnexpectedStatus(Unauthorized401) => Unauthorized.asLeft[Long]
      }
  }

}
