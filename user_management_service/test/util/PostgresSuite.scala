package util

import org.scalatest._
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import slick.jdbc.PostgresProfile.api._
import play.api.db.{Database => PlayDatabase, Databases}
import play.api.db.evolutions._

import scala.util.{Try, Using}
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

  def withDB: (DatabaseDef => Unit) => Try[Unit] =
    Using(Database.forURL(dbUrl, driver = driver, user = dbUser, password = dbPass))

  override def beforeAll(): Unit = {
    Using(
      Database.forURL(dbUrlRoot, driver = driver, user = dbUser, password = dbPass)
    ) { postgres =>
      Await.result(postgres.run(sqlu"CREATE DATABASE #$dbName"), actionTimeout)
    }
  }

  override def afterAll(): Unit = {
    Using(
      Database.forURL(dbUrlRoot, driver = driver, user = dbUser, password = dbPass)
    ) { postgres =>
      Await.result(
        postgres.run(
          sqlu"""SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '#$dbName';
                 DROP DATABASE #$dbName"""
        ),
        actionTimeout
      )
    }
    appDB.shutdown()
  }

  override def beforeEach(): Unit = {
    Evolutions.applyEvolutions(appDB)
  }

  override def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(appDB)
  }

}
