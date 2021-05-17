package services

import services.exceptions.Unauthorized
import org.http4s.client.Client
import org.http4s.Uri

class UserService[F[_]](
      client: Client[F],
      targetURI: Uri) {

  /** Ask user management service for userId given the auth token */
  def confirm(token: String): F[Either[Unauthorized.type, Long]] = ???
}
