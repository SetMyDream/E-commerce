package controllers.validators

import play.api.libs.json.Json

final case class TransferValidator(
      from: Long,
      to: Long,
      amount: BigDecimal,
      totp: String)

object TransferValidator {
  implicit val format = Json.format[TransferValidator]
}
