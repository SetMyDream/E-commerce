package storage

import play.api.libs.json._


/**
 * DTO for displaying user information.
 */
final case class UserResource(id: Option[Long], username: String)

object UserResource {
  /**
   * Mapping to read/write a UserResource out as a JSON value.
   */
  implicit val format: Format[UserResource] = Json.format
}
