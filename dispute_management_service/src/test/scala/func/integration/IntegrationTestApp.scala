package func.integration

import config._
import services.UserService
import storage.db.repo.DisputeRepository
import util.{TestingDb, TestingUserService}

import cats.effect.{ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import org.scalatest.{Args, Status, TestSuite}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait IntegrationTestApp extends TestSuite {
  implicit val CS: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  protected val dependencies = new Dependencies(null, null, null, null)

  protected def withRepo(f: DisputeRepository[IO] => Any) =
    f(new DisputeRepository(dependencies.transactor))

  override def withFixture(test: NoArgTest) = {
    TestingDb.migrations(dependencies.transactor).use { _ =>
      IO(super.withFixture(test))
    }.unsafeRunTimed(5.minutes).get
  }

  override def run(
      testName: Option[String],
      args: Args
    ): Status = {
    val dep = for {
      Config(_, httpConfig, dbConfig_, clientConfig_) <- configRes[IO]
      dbConfig = dbConfig_.copy(poolSize = 3)
      clientConfig = clientConfig_.copy(poolSize = 3)
      transactor <- TestingDb(dbConfig)
      userService <- TestingUserService(clientConfig, httpConfig)
      authToken <- Resource.eval(TestingUserService.acquireAuthToken(userService))
      _ = {
        dependencies._transactor = transactor
        dependencies._userService = userService
        dependencies._authToken = authToken
        dependencies._httpConfig = httpConfig
      }
    } yield ()
    dep.use(_ => IO(super.run(testName, args))).unsafeRunTimed(5.minutes).get
  }

  protected class Dependencies(
        private[IntegrationTestApp] var _transactor: HikariTransactor[IO],
        private[IntegrationTestApp] var _userService: UserService[IO],
        private[IntegrationTestApp] var _authToken: String,
        private[IntegrationTestApp] var _httpConfig: HttpConfig) {
    def transactor = getResource(_transactor, "transactor")
    def userService = getResource(_userService, "userService")
    def authToken = getResource(_authToken, "authToken")
    def httpConfig = getResource(_httpConfig, "httpConfig")

    private def getResource[R](
        res: R,
        name: String
      ): R =
      if (res != null) res
      else throw new IllegalStateException(
        s"Failed to initialize the dependencies: $name is missing."
      )
  }

}
