package util

import controllers.validators.CredentialsValidator
import storage.model.UserResource
import storage.repos.UserRepository

import org.scalatest.TestSuite
import org.scalatest.concurrent.ScalaFutures._
import org.scalatestplus.play.BaseOneAppPerSuite
import play.api.http.{HeaderNames, Writeable}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson, Result}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._

import scala.concurrent.Future

trait SimpleFakeRequest { self: TestSuite with BaseOneAppPerSuite =>
  def userRepo = app.injector.instanceOf[UserRepository]

  def makeEmptyRequest(
      path: String,
      method: String = GET,
      headers: Seq[(String, String)] = Seq.empty
    ): Future[Result] =
    makeRequest(path, method, headers)

  def makeJsonRequest(
      path: String,
      method: String,
      body: JsValue,
      headers: Seq[(String, String)] = Seq.empty
    ): Future[Result] =
    makeRequest(
      path = path,
      method = method,
      Seq(CONTENT_TYPE -> "application/json") ++ headers,
      AnyContentAsJson(body)
    )

  def makeRequest[A](
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

  def getJsStringField(
      resp: Future[Result],
      field: String
    ): String =
    (contentAsJson(resp) \ field).as[String]

  def getUserFromRepo(credentials: CredentialsValidator): Option[UserResource] =
    userRepo.get(credentials.username).futureValue

  def makeInvalidCopy(str: String): String = str.init + (str.last + 1)
}
