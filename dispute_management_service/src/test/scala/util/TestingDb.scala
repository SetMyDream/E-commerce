package util

import config.DbConfig
import storage.db.{transactorRes, Migrations}

import cats.effect._
import doobie.Transactor
import doobie.hikari.HikariTransactor

object TestingDb {
  final val DB_NAME = "testing"

  def apply(
      mainConfig: DbConfig
    )(implicit
      cs: ContextShift[IO],
      as: Async[IO]
    ): Resource[IO, Transactor[IO]] = {
    val testDbUrl = mainConfig.url.split("(?<=:\\d{4})/").head + '/' + DB_NAME
    val testConfig = mainConfig.copy(url = testDbUrl)

    for {
      mainAppTransactor <- transactorRes[IO](mainConfig)
      _ <- createTestDB(mainAppTransactor)
      transactor <- transactorRes[IO](testConfig)
    } yield transactor
  }

  def withMigrations(transactor: HikariTransactor[IO]): Resource[IO, Unit] =
    Resource.make {
      Migrations.applyMigrations(transactor)
    } { _ => Migrations.unapplyMigrations(transactor) }

  private def createTestDB(
      transactor: Transactor[IO]
    )(implicit as: Async[IO]
    ): Resource[IO, Int] = {
    import doobie.implicits._
    Resource.make {
      sql"""
        |DROP DATABASE IF EXISTS $DB_NAME;
        |CREATE DATABASE $DB_NAME")""".stripMargin.update.run.transact(transactor)
    } { _ =>
      sql"""
        |SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB_NAME';
        |DROP DATABASE $DB_NAME""".stripMargin.update.run.transact(transactor).void
    }
  }

}
