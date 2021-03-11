package controllers.validators

import play.api.libs.json.Json


case class CredentialsValidator(username: String, password: String)

object CredentialsValidator {
  implicit val format = Json.format[CredentialsValidator]
}