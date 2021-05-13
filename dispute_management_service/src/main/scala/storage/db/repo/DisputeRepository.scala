package storage.db.repo

import storage.model.{Dispute, DisputeStatus => Status}

import cats.effect.Sync
import doobie.Transactor
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill.SnakeCase

import java.time.LocalDate

class DisputeRepository[F[_]: Sync](transactor: Transactor[F]) {
  val ctx = new DoobieContext.Postgres(SnakeCase)
  import ctx._

  def create(
      buyerId: Long,
      sellerId: Long,
      purchaseId: Long
    ): F[Long] = {
    val date = LocalDate.now()
    ctx.run {
      query[Dispute]
        .insert(lift(Dispute(0, buyerId, sellerId, purchaseId, Status.Active, date)))
        .returningGenerated(_.id)
    }.transact(transactor)
  }

}
