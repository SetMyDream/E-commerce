package services

import config.{ClientConfig, HttpConfig}

import cats.effect.{ConcurrentEffect, Resource}

object ServicesHandler {
  case class Services[F[_]](users: UserService[F])

  def servicesRes[F[_]: ConcurrentEffect](
      clientConfig: ClientConfig,
      httpConfig: HttpConfig
    ): Resource[F, Services[F]] = ServiceClient.res(clientConfig).map { client =>
    val userService = new UserService(client, clientConfig.userManagementPath, httpConfig)
    Services(userService)
  }
}
