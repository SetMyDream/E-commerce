package func.integration

import routes.DisputeInfoRoutes
import routes.middleware.UserAuthMiddleware
import storage.db.repo.DisputeRepository
import storage.model.{Dispute, DisputeStatus}
import syntax.ioToFutureValue
import func.share.DisputeInfoRequests
import util.RoutesUtil

import cats.effect.IO
import cats.syntax.option._
import org.http4s.HttpApp
import org.http4s.Status._
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.implicits._
import io.circe.generic.auto._
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class DisputeInfoFuncSpec
      extends AnyWordSpec
        with IntegrationTestApp
        with RoutesUtil
        with DisputeInfoRequests {
  import dependencies._

  "DisputeInfoRoutes" should {
    "provide dispute info" in withDisputeAndRoutes { (dispute, routes) =>
      val request = `request dispute info with auth token`(
        httpConfig.authTokenHeader,
        authToken,
        dispute.id
      )
      val response = routes.run(request)
      check(response, Ok, Some(dispute))
    }

    "return 404 if there is no such dispute" in withRepoAndRoutes { (repo, routes) =>
      val request = `request dispute info with auth token`(
        httpConfig.authTokenHeader,
        authToken,
        1
      )
      val response = routes.run(request)
      check(response, NotFound, none[Dispute])
    }

    "return 401 if there is no auth token header" in withDisputeAndRoutes {
      (dispute, routes) =>
        val request = `request dispute info with no auth token`(dispute.id)
        val response = routes.run(request)
        check(response, Unauthorized, none[Dispute])
    }

    "return 401 if the auth token header is invalid" in withDisputeAndRoutes {
      (dispute, routes) =>
        val request = `request dispute info with auth token`(
          httpConfig.authTokenHeader,
          "invalidAuthToken",
          dispute.id
        )
        val response = routes.run(request)
        check(response, Unauthorized, none[Dispute])
    }

    "return 403 if a user tries to get info on some other two users' dispute" in withRepoAndRoutes {
      (repo, routes) =>
        val dispute = insertDispute(repo)(authedUserId + 1, authedUserId + 2, 1)
        val request = `request dispute info with auth token`(
          httpConfig.authTokenHeader,
          authToken,
          dispute.id
        )
        val response = routes.run(request)
        check(response, Forbidden, none[Dispute])
    }

    "return a list of disputes related to the user" in withRepoAndRoutes {
      (repo, routes) =>
        val dispute1 = insertDispute(repo)(authedUserId, authedUserId + 1, 1)
        val dispute2 = insertDispute(repo)(authedUserId, authedUserId + 2, 2)
        val dispute3 = insertDispute(repo)(authedUserId + 1, authedUserId, 3)
        val unrelatedDispute =
          insertDispute(repo)(authedUserId + 1, authedUserId + 2, 4)
        val request = `request disputes list with auth token`(
          httpConfig.authTokenHeader,
          authToken
        )
        val response = routes.run(request)
        check(response, Ok, List(dispute1, dispute2, dispute3).some)
    }
  }

  lazy val authedUserId: Long = userService.confirm(authToken).futureValue.get

  def withRepoAndRoutes(
      f: (DisputeRepository[IO], HttpApp[IO]) => Any
    ) = withRepo { repo =>
    val authMiddleware = new UserAuthMiddleware(httpConfig, userService)
    val routes = DisputeInfoRoutes(authMiddleware, repo).orNotFound
    f(repo, routes)
  }

  def withDisputeAndRoutes(
      f: (Dispute, HttpApp[IO]) => Any
    ) = withRepoAndRoutes { (repo, routes) =>
    val dispute = insertDispute(repo)(authedUserId, authedUserId + 1, 1)
    f(dispute, routes)
  }

  def insertDispute(
      repo: DisputeRepository[IO]
    )(buyerId: Long,
      sellerId: Long,
      purchaseId: Long
    ) = {
    val disputeId = repo.create(buyerId, sellerId, purchaseId).futureValue.value
    makeDispute(disputeId, buyerId, sellerId, purchaseId)
  }

  def makeDispute(
      id: Long,
      buyerId: Long,
      sellerId: Long,
      purchaseId: Long
    ) =
    Dispute(
      id,
      buyerId,
      sellerId,
      purchaseId,
      DisputeStatus.Active,
      LocalDate.now()
    )

}
