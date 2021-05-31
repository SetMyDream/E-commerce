package util

import config.DbConfig
import storage.db.{transactorRes, Migrations}

import cats.effect._
import cats.effect.syntax.bracket._
import doobie._
import doobie.hikari.HikariTransactor

object TestingDb {
  final val DB_NAME = "testing"

  def apply(
      mainConfig: DbConfig
    )(implicit
      cs: ContextShift[IO],
      as: Async[IO]
    ): Resource[IO, HikariTransactor[IO]] = {
    val testDbUrl = mainConfig.url.split("(?<=:\\d{4})/").head + '/' + DB_NAME
    val testConfig = mainConfig.copy(url = testDbUrl)

    for {
      mainAppTransactor <- transactorRes[IO](mainConfig)
      _ <- createTestDB(mainAppTransactor)
      transactor <- transactorRes[IO](testConfig)
    } yield transactor
  }

  def migrations(transactor: HikariTransactor[IO]): Resource[IO, Unit] =
    Resource.make {
      Migrations.applyMigrations(transactor)
    } { _ => Migrations.unapplyMigrations(transactor) }

  private def createTestDB(
      transactor: Transactor[IO]
    )(implicit as: Async[IO]
    ): Resource[IO, Int] = {
    import doobie.implicits._
    Resource.make {
      val query = for {
        _ <- disconnectUnclearedConnections
        _ <- (fr"DROP DATABASE IF EXISTS" ++ Fragment.const(DB_NAME)).update.run
        create <- (fr"CREATE DATABASE" ++ Fragment.const(DB_NAME)).update.run
      } yield create
      runWithoutTransaction(query).transact(transactor)
    } { _ =>
      val query = for {
        _ <- disconnectUnclearedConnections
        _ <- (fr"DROP DATABASE" ++ Fragment.const(DB_NAME)).update.run
      } yield ()
      runWithoutTransaction(query).transact(transactor).void
    }
  }

  def runWithoutTransaction[A](p: ConnectionIO[A]): ConnectionIO[A] =
    FC.setAutoCommit(true).bracket(_ => p)(_ => FC.setAutoCommit(false))

  def disconnectUnclearedConnections = {
    import doobie.implicits._
    // @formatter:off
    (fr"""
       |DO '
       |BEGIN
       |  PERFORM pg_terminate_backend(pid) FROM pg_stat_activity
       |  WHERE datname = ''""".stripMargin ++ Fragment.const(DB_NAME) ++ fr"''; END; ';")
       .update.run
    // @formatter:on
  }

}
