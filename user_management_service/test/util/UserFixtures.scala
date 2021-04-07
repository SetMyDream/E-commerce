package util

import auth.DefaultEnv
import auth.models.User
import storage.UserResource
import storage.db.UsersTableRepository

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.TestSuite
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

trait UserFixtures { self: TestSuite with GuiceOneAppPerSuite =>
  implicit val ec = app.injector.instanceOf[ExecutionContext]

  def withDummyUser(testCode: UserResource => Any): Unit = {
    val username = "user1"
    val userRepo = app.injector.instanceOf[UsersTableRepository]
    userRepo.create(username).map {
      case Right(id) => testCode(UserResource(Option(id), username))
      case Left(_) => fail("Failed to create a user for a fixture")
    }
  }

  def withAuthenticatedDummyUser(
      testCode: (DefaultEnv#I, BearerTokenAuthenticator#Value) => Any
    ): Unit = {
    withDummyUser { user =>
      val userIdentity = User(user.id.get, user.username)
      val loginInfo = LoginInfo("credentials", user.id.get.toString)
      val passInfo = app.injector
        .instanceOf[PasswordHasherRegistry]
        .current
        .hash("12345678")

      val authService = app.injector
        .instanceOf[com.google.inject.Injector]
        .instance[AuthenticatorService[BearerTokenAuthenticator]]
      implicit val req = FakeRequest()
      for {
        _ <- app.injector.instanceOf[AuthInfoRepository].add(loginInfo, passInfo)
        authenticator <- authService.create(loginInfo)
        token <- authService.init(authenticator)
        _ = testCode(userIdentity, token)
      } yield ()
    }
  }

}
