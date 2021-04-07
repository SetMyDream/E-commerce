import util._
import controllers.responces.Token
import controllers.validators.CredentialsValidator

import play.api.libs.json.Json
import play.api.test.Helpers._
import org.scalatest.EitherValues._
import org.scalatestplus.play._
import play.api.mvc.AnyContentAsText

class FunctionalSpec
      extends PlaySpec
        with PostgresSuite
        with UserFixtures
        with SimpleFakeRequest {

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

    "register a user with valid credentials" in {
      val credentials = CredentialsValidator("user1", "12345678")
      val resp = makeJsonRequest("/register", POST, Json.toJson(credentials))

      status(resp) mustBe OK
      val token = contentAsJson(resp).validate[Token].asEither.value
      token.token must not be empty

      getUserFromRepo(credentials) mustBe a[Some[_]]
    }

    "return 400 on an invalid /register request" in {
      val resp1 = makeEmptyRequest(
        "/register",
        POST,
        Seq(CONTENT_TYPE -> "application/json")
      )
      status(resp1) mustBe BAD_REQUEST

      val resp2 = makeRequest(
        "/register",
        POST,
        Seq(CONTENT_TYPE -> "application/json"),
        AnyContentAsText("""{"username": "username", "invalidField": true}""")
      )
      status(resp2) mustBe BAD_REQUEST
    }

    "not register the user if a user with the same username already exists" in withDummyUser {
      user =>
        val credentials = CredentialsValidator(user.username, "12345678")
        val resp = makeJsonRequest("/register", POST, Json.toJson(credentials))

        status(resp) mustBe CONFLICT
        getUserFromRepo(credentials).get mustBe user
    }

  }
}
