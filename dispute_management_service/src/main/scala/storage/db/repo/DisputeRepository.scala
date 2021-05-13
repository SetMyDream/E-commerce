package storage.db.repo

import storage.model.{Dispute, DisputeStatus => Status}

import cats.Monad
import cats.syntax.functor._
import cats.effect.Sync
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.quill.DoobieContext
import io.getquill.SnakeCase

class DisputeRepository[F[_]: Sync: Monad](transactor: Transactor[F]) {
  val ctx = new DoobieContext.Postgres(SnakeCase)
  import ctx._

  def create(
      buyerId: Long,
      sellerId: Long,
      purchaseId: Long
    ): F[Either[UniqueAgendaViolation.type, Long]] =
    sql"""
      INSERT INTO "dispute" 
      (buyer_id, seller_id, purchase_id, status) VALUES 
      ($buyerId, $sellerId, $purchaseId, ${Status.Active.value})""".update
      .withUniqueGeneratedKeys[Long]("id")
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => UniqueAgendaViolation
      }
      .transact(transactor)

}
