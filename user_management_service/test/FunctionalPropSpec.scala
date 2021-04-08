import controllers.validators.CredentialsValidator
import util.{PostgresSuite, SimpleFakeRequest}

import play.api.libs.json.Json
import play.api.test.Helpers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.matchers.must.Matchers
import org.scalatest.GivenWhenThen
import org.scalatest.propspec.AnyPropSpec

class FunctionalPropSpec
      extends AnyPropSpec
        with ScalaCheckPropertyChecks
        with Matchers
        with PostgresSuite
        with SimpleFakeRequest
        with GivenWhenThen {

  property("a user should not be registered if their password is invalid") {
    forAll(
      Table(
        ("password", "expectedResponseError"),
        ("", "password can't be empty"),
        ("123", "password is too short"),
        ("123456789012345678901", "password is too long")
      )
    ) { (password: String, expectedResponseError: String) =>
      info("—————-")
      Given(s"""password "$password"""")
      `return BAD_REQUEST and don't create the user when a field is invalid`(
        CredentialsValidator("user1", password),
        "password",
        expectedResponseError
      )
    }
  }

  property("a user should not be registered if their username is invalid") {
    forAll(
      Table(
        ("username", "expectedResponseError"),
        ("", "username can't be empty"),
        ("usr", "username is too short"),
        ("myusernameiswaytoolong", "username is too long")
      )
    ) { (username: String, expectedResponseError: String) =>
      info("—————-")
      Given(s"""username "$username"""")
      `return BAD_REQUEST and don't create the user when a field is invalid`(
        CredentialsValidator(username, "12345678"),
        "username",
        expectedResponseError
      )
    }
  }

  def `return BAD_REQUEST and don't create the user when a field is invalid`(
      credentials: CredentialsValidator,
      fieldName: String,
      expectedResponseError: String
    ) = {
    When("the registration request is made")
    val resp = makeJsonRequest("/register", POST, Json.toJson(credentials))

    Then("the response status must be 400")
    status(resp) mustBe BAD_REQUEST

    And(s"""the response body should have message "$expectedResponseError"""")
    getJsStringField(resp, fieldName) mustBe expectedResponseError

    And(s"the user should not be created in the database")
    getUserFromRepo(credentials) mustBe None
  }
}
