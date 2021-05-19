package controllers

import services.exceptions.AuthException
import storage.model.Dispute
import storage.db.repo.DisputeRepository

import cats.effect.Sync
import cats.syntax.functor._

class DisputeInfoController[F[_]: Sync](repository: DisputeRepository[F]) {

  def get(
      disputeId: Long,
      userId: Long
    ): F[Either[AuthException.Forbidden.type, Option[Dispute]]] = for {
    disputeOption <- repository.get(disputeId)
    accessedDispute = Either.cond(
      disputeOption.fold(true)(accessible(_, userId)),
      disputeOption,
      AuthException.Forbidden
    )
  } yield accessedDispute

  private def accessible(
      dispute: Dispute,
      userId: Long
    ): Boolean =
    userId == dispute.sellerId || userId == dispute.buyerId

}
