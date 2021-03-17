package controllers.responces

import io.swagger.annotations.{ApiModel, ApiModelProperty}
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
@ApiModel(description = "Auth session token")
case class Token(
      @ApiModelProperty(required = true,
            example = "b2e17047d9ac6805c93786b52e2fac362a57362bdabb9e315bbb1c40263ebd51d030581b68914330fb773134d983d05577b2c1dbc2816f43a65ced8f2515807e1c8eb3418b57bdc2178f56236344ebc96f8385f03eb5630557f7e433ccc3ef6bd9de796a5bb0179e3aab4321c346fd6316033b13ae873291cd48c684460fb865")
      token: String,
      @ApiModelProperty(required = false, example = "2021-03-17T01:40:55.120+0200")
      expiresOn: DateTime)

object Token {
  implicit val dateFormat = controllers.implicits.Json.dateFormat
  implicit val writesFormat = Json.writes[Token]
  final val httpHeaderName = "X-Auth-Token"

  /** Used for defining [[io.swagger.annotations.Authorization]] */
  final val docsKey = "token"
  final val docsDescription = "Session token header"
}
