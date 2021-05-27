package func.integration

import config._
import services.UserService
import util.{TestingDb, TestingUserService}

import cats.effect.{ContextShift, IO, Resource}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.scalatest.{Args, Status, TestSuite}

trait IntegrationTestApp extends TestSuite {
  implicit val CS: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  protected val dependencies = new Dependencies(null, null, null)

  override def withFixture(test: NoArgTest) = {
    TestingDb.migrations(dependencies.transactor).use { _ =>
      IO(super.withFixture(test))
    }.unsafeRunSync()
  }

  override def run(
      testName: Option[String],
      args: Args
    ): Status = {
    val dep = for {
      Config(_, httpConfig, dbConfig_, clientConfig_) <- configRes[IO]
      dbConfig = dbConfig_.copy(poolSize = 1)
      clientConfig = clientConfig_.copy(poolSize = 1)
      transactor <- TestingDb(dbConfig)
      userService <- TestingUserService(clientConfig, httpConfig)
      authToken <- Resource.eval(TestingUserService.acquireAuthToken(userService))
      _ = {
        dependencies._transactor = transactor
        dependencies._userService = userService
        dependencies._authToken = authToken
      }
    } yield ()
    dep.use(_ => IO(super.run(testName, args))).unsafeRunSync()
  }

  protected class Dependencies(
        private[IntegrationTestApp] var _transactor: HikariTransactor[IO],
        private[IntegrationTestApp] var _userService: UserService[IO],
        private[IntegrationTestApp] var _authToken: String) {
    def transactor = getResource(_transactor, "transactor")
    def userService = getResource(_userService, "userService")
    def authToken = getResource(_authToken, "authToken")

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
