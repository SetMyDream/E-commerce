package util

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

trait RoutesUtil extends Matchers {

  def check[A](
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A]
    )(implicit ev: EntityDecoder[IO, A]
    ): Assertion = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status shouldBe expectedStatus
    expectedBody.fold[Assertion](
      actualResp.body.compile.toVector.unsafeRunSync() shouldBe empty
    )(expected => actualResp.as[A].unsafeRunSync() shouldBe expected)
  }

}
