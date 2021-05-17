package config

final case class Config(server: ServerConfig, database: DbConfig, client: ClientConfig)
