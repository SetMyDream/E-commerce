package controllers.validators

import play.api.libs.json.Json

/**
 * Validates a JSON request body that consists of:
 * @param username a string of length of 4 to 20 characters
 * @param password a clear-text password string of length of 4 to 20 characters
 */
case class CredentialsValidator(
      username: String,
      password: String)

object CredentialsValidator {
  implicit val format = Json.format[CredentialsValidator]
}
