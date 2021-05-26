package storage.db

import cats.syntax.all._
import cats.effect.Sync
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

object Migrations {

  def applyMigrations[F[_]: Sync](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { dataSource =>
      Sync[F].delay {
        val flyway = Flyway.configure().dataSource(dataSource).load()
        flyway.repair()
        flyway.migrate()
      }
    }.void

  def unapplyMigrations[F[_]: Sync](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { dataSource =>
      Sync[F].delay {
        val flyway = Flyway.configure().dataSource(dataSource).load()
        flyway.clean()
      }
    }.void

}
