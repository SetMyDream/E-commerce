package util

import auth.DefaultEnv
import auth.models.User
import storage.UserResource
import storage.db.UsersTableRepository

import controllers.validators.CredentialsValidator
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures._
import play.api.test.FakeRequest

trait UserFixtures extends InjectedServices {
  self: TestSuite with GuiceOneAppPerSuite =>

  def withDummyUser(testCode: UserResource => Any): Unit = {
    val username = "user1"
    app.injector.instanceOf[UsersTableRepository]
      .create(username).map {
        case Right(id) => testCode(UserResource(Option(id), username))
        case Left(_) => fail("Failed to create a user for a fixture")
      }
  }

  def withRegisteredDummyUser(
      testCode: (DefaultEnv#I, CredentialsValidator, LoginInfo) => Any
    ): Unit = {
    withDummyUser { user =>
      val userIdentity = User(user.id.get, user.username)
      val loginInfo = LoginInfo("credentials", user.id.get.toString)
      val credentials = CredentialsValidator(userIdentity.username, "12345678")
      val passInfo = app.injector
        .instanceOf[PasswordHasherRegistry]
        .current
        .hash(credentials.password)

      inject[AuthInfoRepository].add(loginInfo, passInfo)
        .map(_ => testCode(userIdentity, credentials, loginInfo)).futureValue
    }
  }

  def withAuthenticatedDummyUser(
      testCode: (DefaultEnv#I, BearerTokenAuthenticator#Value) => Any
    ): Unit = {
    withRegisteredDummyUser { (user, _, loginInfo) =>
      implicit val req = FakeRequest()
      for {
        authenticator <- authService.create(loginInfo)
        token <- authService.init(authenticator)
      } yield testCode(user, token)
    }
  }

}
