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

  "UserController" should {
    "return user info by user id" in withDummyUser { user =>
      val resp = makeEmptyRequest(s"/user/${user.id}")
      contentAsJson(resp) mustBe Json.toJson(user)
    }

    "return 404 if there is no such user" in {
      val resp = makeEmptyRequest("/user/1")
      status(resp) mustBe 404
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
