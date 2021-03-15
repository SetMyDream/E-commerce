package controllers.responces

import org.joda.time.DateTime
import play.api.libs.json._


/**
 * A response blueprint that serializes a session token data provided by
 * Silhouette to JSON.
 * @param token a 257-character hash string for the auth header
 * @param expiresOn token expiration datetime. It's type
 * [[org.joda.time.DateTime]] is the same as
 * [[com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator.expirationDateTime]]'s
 * because otherwise we would need to convert joda's DateTime
 * to it's Java SDK alternative to just immediately serialize it to JSON
 * and send it to the user, which is unnecessary and expensive IMO.
 * The alternative is to use String here, but I think it makes it
 * less obvious what our intentions are with this variable and what is
 * the format of the datetime data in it.
 */
case class Token(token: String, expiresOn: DateTime)

object Token {
  implicit val dateFormat = controllers.implicits.Json.dateFormat
  implicit val writesFormat = Json.writes[Token]
}
