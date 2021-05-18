package controllers

import services.UserService
import services.exceptions.AuthException
import storage.model.Dispute
import storage.db.repo.DisputeRepository

import cats.data.EitherT
import cats.effect.Sync

class DisputeInfoController[F[_]: Sync](
      userService: UserService[F],
      repository: DisputeRepository[F]) {

  def get(
      disputeId: Long,
      authToken: String
    ): F[Either[AuthException, Option[Dispute]]] = {
    val dispute = for {
      userId <- EitherT(userService.confirm(authToken))
      disputeOption <- EitherT.liftF(repository.get(disputeId))
      accessedDispute <- EitherT.cond(
        disputeOption.fold(true)(dispute =>
          userId == dispute.sellerId || userId == dispute.buyerId
        ),
        disputeOption,
        AuthException.Forbidden: AuthException
      )
    } yield accessedDispute
    dispute.value
  }

}
