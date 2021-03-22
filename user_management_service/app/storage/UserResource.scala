package storage

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json._

/** DTO for displaying user information. */
@ApiModel(description = "User data from the database")
final case class UserResource(
      @ApiModelProperty(required = true, dataType = "Long")
      id: Option[Long],
      username: String)

object UserResource {

  /** Mapping to read/write a UserResource out as a JSON value. */
  implicit val format: Format[UserResource] = Json.format
}
