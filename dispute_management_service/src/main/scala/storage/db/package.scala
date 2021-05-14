package storage

import config.DbConfig

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

package object db {

  def transactorRes[F[_]: Async: ContextShift](
      config: DbConfig
    ): Resource[F, HikariTransactor[F]] = {
    import config._
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.poolSize)
      blocker <- Blocker[F]
      transactor <- HikariTransactor.newHikariTransactor(
        driver, url, user, password, ec, blocker
      )
    } yield transactor
  }

}
