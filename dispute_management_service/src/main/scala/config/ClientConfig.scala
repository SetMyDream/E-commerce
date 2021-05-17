package config

import org.http4s.Uri

final case class ClientConfig(userManagementUri: Uri, poolSize: Int)
