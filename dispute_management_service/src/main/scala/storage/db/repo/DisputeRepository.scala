package storage.db.repo

import storage.model.{Dispute, DisputeStatus => Status}

import cats.Monad
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.Sync
import doobie.Transactor
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill.SnakeCase

import java.time.LocalDate

class DisputeRepository[F[_]: Sync: Monad](transactor: Transactor[F]) {
  val ctx = new DoobieContext.Postgres(SnakeCase)
  import ctx._

  def create(
      buyerId: Long,
      sellerId: Long,
      purchaseId: Long
    ): F[Long] = for {
    dispute <- Sync[F].delay(
      Dispute(0, buyerId, sellerId, purchaseId, Status.Active, LocalDate.now())
    )
    disputeId <- ctx.run {
      query[Dispute]
        .insert(lift(dispute))
        .returningGenerated(_.id)
    }.transact(transactor)
  } yield disputeId

}
