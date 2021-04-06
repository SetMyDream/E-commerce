import auth.DefaultEnv
import auth.models.User
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import storage.UserResource
import storage.db.UsersTableRepository
import controllers.UserController
import util.PostgresSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import com.mohiva.play.silhouette.api.{Authenticator, LoginInfo, StorableAuthenticator}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import controllers.responces.Token
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.http.{HeaderNames, Writeable}

import scala.concurrent.{ExecutionContext, Future}

class FunctionalSpec extends PlaySpec with PostgresSuite with ScalaFutures {
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  val userController = app.injector.instanceOf[UserController]

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

  "UserController" should {
    "return user info by user id" in withDummyUser { user =>
      val resp = makeEmptyRequest(s"/user/${user.id}")
      contentAsJson(resp) mustBe Json.toJson(user)
    }

    "return 404 if there is no such user" in {
      val resp = makeEmptyRequest("/user/1")
      status(resp) mustBe 404
    }

    "return user info by authentication token" in withAuthenticatedDummyUser {
      (user, token) =>
        val resp = makeEmptyRequest(
          path = "/user",
          headers = Seq(Token.httpHeaderName -> token)
        )

        status(resp) mustBe OK
        contentAsJson(resp) mustBe Json.toJson(user)
    }

    "return 401 if invalid token is specified" in withAuthenticatedDummyUser {
      (_, token) =>
        val invalidToken = token.init + (token.last + 1)
        val resp = makeEmptyRequest(
          path = "/user",
          headers = Seq(Token.httpHeaderName -> invalidToken)
        )
        status(resp) mustBe UNAUTHORIZED
        contentAsString(resp) mustBe "User is not authenticated"
    }
  }

  private def makeEmptyRequest(
      path: String,
      method: String = GET,
      headers: Seq[(String, String)] = Seq.empty
    ): Future[Result] =
    makeRequest(path, method, headers)

  private def makeRequest[A](
      path: String,
      method: String,
      headers: Seq[(String, String)],
      body: A = AnyContentAsEmpty
    )(implicit w: Writeable[A]
    ): Future[Result] = {
    route(
      app,
      FakeRequest(
        method = method,
        uri = path,
        headers = FakeHeaders(Seq(HeaderNames.HOST -> "localhost") ++ headers),
        body = body
      )
    ).get

  }
}
