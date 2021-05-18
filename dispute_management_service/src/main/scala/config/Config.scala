package config

final case class Config(
      server: ServerConfig,
      http: HttpConfig,
      database: DbConfig,
      client: ClientConfig)
