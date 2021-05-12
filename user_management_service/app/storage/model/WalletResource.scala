package storage.model

import io.swagger.annotations.ApiModel
import play.api.libs.json.{Format, Json}

/** DTO for displaying amount of funds on user's account. */
@ApiModel(description = "Wallet data from the database")
final case class WalletResource(
      userId: Long,
      balance: BigDecimal)

object WalletResource {
  implicit val format: Format[WalletResource] = Json.format
}
