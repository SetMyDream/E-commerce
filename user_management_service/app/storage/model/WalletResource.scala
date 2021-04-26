package storage.model

import play.api.libs.json.{Format, Json}

/** DTO for displaying amount of funds on user's account. */
case class WalletResource(
      userId: Long,
      balance: BigDecimal)

object WalletResource {
  implicit val format: Format[WalletResource] = Json.format
}
