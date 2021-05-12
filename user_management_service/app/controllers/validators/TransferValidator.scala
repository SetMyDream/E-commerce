package controllers.validators

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.Json

@ApiModel(description = "Payload for transfer operation")
final case class TransferValidator(
      from: Long,
      to: Long,
      amount: BigDecimal,
      @ApiModelProperty(value = "TOTP code from Vault for key finance__`from_id`",
            required = true)
      totp: String)

object TransferValidator {
  implicit val format = Json.format[TransferValidator]
}
