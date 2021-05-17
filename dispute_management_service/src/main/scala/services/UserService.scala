package services

import services.exceptions.Unauthorized
import org.http4s.client.Client

class UserService[F[_]](client: Client[F]) {

  /** Ask user management service for userId given the auth token */
  def confirm(token: String): F[Either[Unauthorized.type, Long]] = ???
}
