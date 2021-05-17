package services

import config.ClientConfig

import cats.effect.{ConcurrentEffect, Resource}

object ServicesHandler {
  case class Services[F[_]](users: UserService[F])

  def servicesRes[F[_]: ConcurrentEffect](
      config: ClientConfig
    ): Resource[F, Services[F]] = ServiceClient.res(config).map { client =>
    val userService = new UserService(client)
    Services(userService)
  }
}
