package util

import org.scalatest._
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import slick.jdbc.PostgresProfile.api._

import scala.util.{Try, Using}
import scala.concurrent.Await
import scala.concurrent.duration._

trait PostgresSuite extends BeforeAndAfterAll {
  self: TestSuite =>
  private val dbName = getClass.getSimpleName.toLowerCase
  private val actionTimeout = 5.seconds

  private val defaultConf = ConfigFactory.load()
  private val dbUrlRoot = defaultConf.getString("slick.dbs.default.db.url")
  private val driver = defaultConf.getString("slick.dbs.default.db.profile")
  private val dbUser = defaultConf.getString("slick.dbs.default.db.user")
  private val dbPass = defaultConf.getString("slick.dbs.default.db.password")
  val dbUrl: String = "^.*/".r.findFirstIn(dbUrlRoot).get + dbName

  def withDB: (DatabaseDef => ()) => Try[Unit] =
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
      Await.result(postgres.run(sqlu"DROP DATABASE #$dbName"), actionTimeout)
    }
  }

}
