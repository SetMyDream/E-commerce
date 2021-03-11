package controllers.validators

import play.api.libs.json.Json


case class RegisterValidator(username: String, password: String)

object RegisterValidator {
  implicit val format = Json.format[RegisterValidator]
}