package util

import org.scalatest.TestSuite
import org.scalatestplus.play.BaseOneAppPerSuite
import play.api.http.{HeaderNames, Writeable}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._

import scala.concurrent.Future

trait SimpleFakeRequest { self: TestSuite with BaseOneAppPerSuite =>

  def makeEmptyRequest(
      path: String,
      method: String = GET,
      headers: Seq[(String, String)] = Seq.empty
    ): Future[Result] =
    makeRequest(path, method, headers)

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
}
