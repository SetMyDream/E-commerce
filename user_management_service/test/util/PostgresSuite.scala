package util

import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}
import slick.jdbc.PostgresProfile.api._
import slick.dbio.{Effect, NoStream}
import slick.sql.SqlAction
import play.api.db.{DBApi, Database => PlayDatabase}
import play.api.db.evolutions._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.util.Using
import scala.concurrent.Await
import scala.concurrent.duration._

trait PostgresSuite
      extends GuiceOneAppPerSuite
        with BeforeAndAfterAll
        with BeforeAndAfterEach {
  self: TestSuite =>
  override val invokeBeforeAllAndAfterAllEvenIfNoTestsAreExpected = true

  private val dbName = getClass.getSimpleName.toLowerCase
  private val actionTimeout = 5.seconds

  private def dbUrlConfigPath = "slick.dbs.default.db.url"
  private val defaultConf = ConfigFactory.load()
  private val dbUrlRoot = defaultConf.getString(dbUrlConfigPath)
  private val driver = defaultConf.getString("slick.dbs.default.db.profile")
  private val dbUser = defaultConf.getString("slick.dbs.default.db.user")
  private val dbPass = defaultConf.getString("slick.dbs.default.db.password")
  val dbUrl: String = "^.*/".r.findFirstIn(dbUrlRoot).get + dbName

  lazy val appDB: PlayDatabase = app.injector.instanceOf[DBApi].database("default")

  protected def beforeAppSetup(): Unit = {
    updateRootDB(sqlu"""
      DROP DATABASE IF EXISTS #$dbName;
      CREATE DATABASE #$dbName""")
  }

  override def fakeApplication() = {
    beforeAppSetup()
    new GuiceApplicationBuilder()
      .configure(
        dbUrlConfigPath -> dbUrl
      )
      .build()
  }

  def withRootDB(f: DatabaseDef => Unit): Unit =
    Using(
      Database.forURL(dbUrlRoot, driver = driver, user = dbUser, password = dbPass)
    )(f)

  def updateRootDB(_sql: SqlAction[Any, NoStream, Effect]): Unit = {
    withRootDB { postgres =>
      Await.result(
        postgres.run(_sql),
        actionTimeout
      )
    }
  }

  override def afterAll(): Unit = {
    try super.afterAll()
    finally {
      appDB.shutdown()
      updateRootDB(sqlu"""
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
