import util._
import controllers.responces.Token
import controllers.validators.CredentialsValidator

import play.api.libs.json.Json
import play.api.test.Helpers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._

class FunctionalSpec
      extends PlaySpec
        with PostgresSuite
        with UserFixtures
        with SimpleFakeRequest
        with ScalaFutures {

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
}
