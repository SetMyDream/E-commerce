package commands.vault

import scala.concurrent.ExecutionContext

class VaultClient(
      commands: VaultCommands,
      authToken: String
    )(implicit ec: ExecutionContext) {
  def generateTOTPKey = commands.generateTOTPKey(authToken) _
  def generateTOTPCode = commands.generateTOTPCode(authToken) _
  def validateTOTPCode = commands.validateTOTPCode(authToken) _
  def authenticatedRequest = commands.authenticatedRequest(authToken) _
}
