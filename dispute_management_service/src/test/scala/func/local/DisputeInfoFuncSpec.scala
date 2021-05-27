package func.local

import storage.db.repo.DisputeRepository
import storage.model.{Dispute, DisputeStatus}
import util.RoutesUtil

import cats.syntax.option._
import cats.syntax.applicative._
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.Status._
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class DisputeInfoFuncSpec
      extends AnyWordSpec
        with LocalTestApp
        with RoutesUtil
        with Matchers {
  "DisputeInfoRoutes" should {
    "provide dispute info" in withMockDisputeAndRepo { (dispute, repo) =>
      val authHeader = Header(httpConfig.authTokenHeader, "authToken")
      val request = makeGetRequest(Uri(path = "/info/" + dispute.id), authHeader)
      val response = disputeInfoRoutes(repo).run(request)
      check(response, Ok, Some(dispute.asJson))
    }

    "return 404 if there is no such dispute" in withMockDispute { dispute =>
      val authHeader = Header(httpConfig.authTokenHeader, "authToken")
      val request = makeGetRequest(Uri(path = "/info/" + dispute.id), authHeader)

      val repo = mock[DisputeRepositoryIO]
      repo.get _ expects dispute.id returning IO.pure(None)

      val response = disputeInfoRoutes(repo).run(request)
      check(response, NotFound, none[Dispute])
    }

    "return 401 if there is no auth token header" in withMockDisputeAndRepo {
      (dispute, repo) =>
        val request = makeGetRequest(Uri(path = "/info/" + dispute.id))
        val response = disputeInfoRoutes(repo).run(request)
        check(response, Unauthorized, none[Dispute])
    }

    "return 401 if the auth token header is invalid" in withMockDispute { dispute =>
      val authHeader = Header(httpConfig.authTokenHeader, "authToken")
      val request = makeGetRequest(Uri(path = "/info/" + dispute.id), authHeader)

      val repo = mock[DisputeRepositoryIO]
      val service = mock[UserServiceIO]
      service.confirm _ expects authHeader.value returning IO.pure(None)

      val response = disputeInfoRoutes(repo, service).run(request)
      check(response, Unauthorized, none[Dispute])
    }

    "return 403 if a user tries to get info on some other two users' dispute" in withMockDisputeAndRepo {
      (dispute, repo) =>
        val unconcernedUserId = dispute.buyerId + dispute.sellerId
        val authHeader = Header(httpConfig.authTokenHeader, "authToken")
        val request = makeGetRequest(Uri(path = "/info/" + dispute.id), authHeader)

        val service = mock[UserServiceIO]
        service.confirm _ expects authHeader.value returning Some(unconcernedUserId)
          .pure[IO]

        val response = disputeInfoRoutes(repo, service).run(request)
        check(response, Forbidden, none[Dispute])
    }

    "return a list of disputes related to the user" in withMockDispute { dispute =>
      val authHeader = Header(httpConfig.authTokenHeader, "authToken")
      val request = makeGetRequest(Uri(path = "/list/all"), authHeader)

      val userId = dispute.buyerId
      val service = mock[UserServiceIO]
      service.confirm _ expects authHeader.value returning Some(userId).pure[IO]

      val repo = mock[DisputeRepositoryIO]
      val disputes = for {
        i <- (userId + 1 to userId + 3).toList
      } yield dispute.copy(i, userId, i, i)
      repo.list _ expects * returning IO.pure(disputes)

      val response = disputeInfoRoutes(repo, service).run(request)
      check(response, Ok, disputes.some)
    }
  }

  def withMockDispute(f: Dispute => Any) =
    f(Dispute(1, 1, 2, 1, DisputeStatus.Active, LocalDate.now()))

  def withMockDisputeAndRepo(f: (Dispute, DisputeRepository[IO]) => Any) = {
    withMockDispute { dispute =>
      val repo = stub[DisputeRepositoryIO]
      repo.get _ when dispute.id returns Some(dispute).pure[IO]
      f(dispute, repo)
    }
  }

  def makeGetRequest(
      uri: Uri,
      headers: Header*
    ): Request[IO] =
    Request[IO](uri = uri).withHeaders(headers: _*)

}
