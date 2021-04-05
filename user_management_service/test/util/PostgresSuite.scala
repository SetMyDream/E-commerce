package util

import org.scalatest._
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import slick.jdbc.PostgresProfile.api._
import play.api.db.{Databases, Database => PlayDatabase}
import play.api.db.evolutions._
import slick.dbio.{Effect, NoStream}
import slick.sql.SqlAction

import scala.util.Using
import scala.concurrent.Await
import scala.concurrent.duration._

trait PostgresSuite extends BeforeAndAfterAll with BeforeAndAfterEach {
  self: Suite =>
  private val dbName = getClass.getSimpleName.toLowerCase
  private val actionTimeout = 5.seconds

  private def dbUrlConfigPath = "slick.dbs.default.db.url"
  private val defaultConf = ConfigFactory.load()
  private val dbUrlRoot = defaultConf.getString(dbUrlConfigPath)
  private val driver = defaultConf.getString("slick.dbs.default.db.profile")
  private val dbUser = defaultConf.getString("slick.dbs.default.db.user")
  private val dbPass = defaultConf.getString("slick.dbs.default.db.password")
  val dbUrl: String = "^.*/".r.findFirstIn(dbUrlRoot).get + dbName

  lazy val appDB: PlayDatabase = Databases(
    url = dbUrl,
    driver = driver,
    name = "default",
    config = Map(
      "username" -> dbUser,
      "password" -> dbPass
    )
  )

  def withDB(f: DatabaseDef => Unit): Unit =
    Using(
      Database.forURL(dbUrl, driver = driver, user = dbUser, password = dbPass)
    )(f)

  def updateDB(_sql: SqlAction[Any, NoStream, Effect]): Unit = {
    withDB { postgres =>
      Await.result(
        postgres.run(_sql),
        actionTimeout
      )
    }
  }

  override def beforeAll(): Unit = {
    updateDB(sqlu"""
      DROP DATABASE IF EXISTS #$dbName;
      CREATE DATABASE #$dbName""")
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    try super.afterAll()
    finally {
      appDB.shutdown()
      updateDB(sqlu"""
        SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '#$dbName';
        DROP DATABASE #$dbName""")
    }
  }

  override def beforeEach(): Unit = {
    Evolutions.applyEvolutions(appDB)
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    try super.afterEach()
    finally Evolutions.cleanupEvolutions(appDB)
  }

}
