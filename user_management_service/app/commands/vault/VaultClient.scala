package commands.vault

import scala.concurrent.ExecutionContext

class VaultClient(
      commands: VaultCommands,
      authToken: String
    )(implicit ec: ExecutionContext) {
  def generateTOTPKey = commands.generateTOTPKey(authToken) _
}
