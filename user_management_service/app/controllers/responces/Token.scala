package controllers.responces

import org.joda.time.DateTime
import play.api.libs.json._


case class Token(token: String, expiresOn: DateTime)

object Token {
  implicit val dateFormat = controllers.implicits.Json.dateFormat
  implicit val format = Json.format[Token]
}
