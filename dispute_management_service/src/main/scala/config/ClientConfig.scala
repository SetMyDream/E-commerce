package config

import org.http4s.Uri

final case class ClientConfig(userManagementPath: Uri, poolSize: Int)
