package util

import auth.DefaultEnv
import auth.models.User
import storage.UserResourceHandler
import storage.db.UsersTableRepository
import storage.model.{UserResource, WalletResource}
import controllers.validators.CredentialsValidator

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures.{convertScalaFuture, PatienceConfig}
import play.api.test.FakeRequest

import scala.concurrent.duration._

trait UserFixtures extends InjectedServices {
  self: TestSuite with GuiceOneAppPerSuite =>
  val timeOut = 10.seconds

  def withDummyUser(
      testCode: UserResource => Any
    )(implicit patience: PatienceConfig
    ): Unit = {
    val username = "user1"
    inject[UsersTableRepository]
      .create(username)
      .futureValue match {
      case Right(id) => testCode(UserResource(Option(id), username))
      case Left(e) => fail("Failed to create the dummy user user for a fixture", e)
    }
  }

  def withRegisteredDummyUser(
      testCode: (DefaultEnv#I, CredentialsValidator, LoginInfo) => Any
    )(implicit patience: PatienceConfig
    ): Unit = {
    withDummyUser { user =>
      val userIdentity = User(user.id.get, user.username)
      val loginInfo = LoginInfo(CredentialsProvider.ID, user.id.get.toString)
      val credentials = CredentialsValidator(userIdentity.username, "12345678")
      val passInfo = app.injector
        .instanceOf[PasswordHasherRegistry]
        .current
        .hash(credentials.password)

      inject[AuthInfoRepository].add(loginInfo, passInfo).futureValue
      testCode(userIdentity, credentials, loginInfo)
    }
  }

  def withRegisteredDummyUser(
      username: String = "username",
      password: String = "12345678",
      account: BigDecimal = 10000
    )(testCode: Long => Any
    )(implicit patience: PatienceConfig
    ): Unit = {
    inject[UserResourceHandler]
      .register(username, password, account)
      .futureValue match {
      case Right((_, userId)) => testCode(userId)
      case Left(e) => fail("Failed to create a user for the dummy user fixture", e)
    }
  }

  def withWallet(
      username: String = "username",
      account: BigDecimal = 10000
    )(testCode: WalletResource => Any
    )(implicit patience: PatienceConfig
    ): Unit = withRegisteredDummyUser(username, account = account) { userId =>
    WalletResource(userId, account)
  }

  def withTwoWallets(
      account1: BigDecimal = 10000,
      account2: BigDecimal = 10000
    )(testCode: (WalletResource, WalletResource) => Any
    )(implicit patience: PatienceConfig
    ): Unit =
    withWallet("user1", account1) { wallet1 =>
      withWallet("user2", account2) { wallet2 =>
        testCode(wallet1, wallet2)
      }
    }

  def withAuthenticatedDummyUser(
      testCode: (Long, BearerTokenAuthenticator#Value) => Any
    )(implicit patience: PatienceConfig
    ): Unit = {
    withRegisteredDummyUser() { userId =>
      val token = authenticate(userId)
      testCode(userId, token)
    }
  }

  def authenticate(userId: Long): BearerTokenAuthenticator#Value = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, userId.toString)
    implicit val req = FakeRequest()
    val authenticator = authService.create(loginInfo).futureValue
    authService.init(authenticator).futureValue
  }

}
