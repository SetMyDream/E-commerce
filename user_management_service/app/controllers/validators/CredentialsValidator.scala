package controllers.validators

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.Json

/**
 * Validates a JSON request body that consists of:
 * @param username a string of length of 4 to 20 characters
 * @param password a clear-text password string of length of 4 to 20 characters
 */
@ApiModel(description = "Credentials model")
case class CredentialsValidator(
      @ApiModelProperty(required = true, example = "AzureDiamond")
      username: String,
      @ApiModelProperty(required = true, example = "hunter2")
      password: String)

object CredentialsValidator {
  implicit val format = Json.format[CredentialsValidator]
}
