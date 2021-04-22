package command

import scala.concurrent.ExecutionContext

class VaultClient(
      commands: VaultCommands,
      authToken: String
    )(implicit ec: ExecutionContext) {
  ???
}
