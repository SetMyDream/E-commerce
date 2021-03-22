package controllers

import storage.ProductResource

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{RequestHeader, Result}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._

import scala.concurrent.Future

class ProductRouterTest extends PlaySpec with GuiceOneAppPerTest {

  "ProductsRouter" should {

    "render the list of products" in {
      val request = FakeRequest(GET, "/products/all").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val posts: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      posts.filter(_.id == "1").head mustBe (ProductResource("1", "/v1/posts/1", "title 1", "blog post 1"))
    }

    "render the list of products when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/posts/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val posts: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      posts.filter(_.id == "1").head mustBe (ProductResource("1", "/v1/posts/1", "title 1", "blog post 1"))
    }
  }

}