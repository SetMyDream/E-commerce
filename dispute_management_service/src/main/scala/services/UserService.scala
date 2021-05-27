package services

import config.HttpConfig
import services.model.UserIdentity

import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.applicativeError._
import cats.effect.Sync
import org.http4s._
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.Status.Unauthorized
import org.http4s.circe.CirceEntityDecoder._
import io.circe.generic.auto._

class UserService[F[_]: Sync](
      val client: Client[F],
      val baseTargetURI: Uri,
      httpConfig: HttpConfig) {

  /** Ask user management service for userId given the auth token */
  def confirm(token: String): F[Option[Long]] = {
    val tokenHeader = Header(httpConfig.authTokenHeader, token)
    val request = Request[F](
      uri = baseTargetURI / "user"
    ).withHeaders(tokenHeader)
    client
      .expect[UserIdentity](request)
      .map(_.id.some)
      .recover {
        case UnexpectedStatus(Unauthorized) => None
      }
  }

}
