package storage.model

import java.time.LocalDate

final case class Dispute(
      id: Long,
      buyerId: Long,
      sellerId: Long,
      purchaseId: Long,
      status: DisputeStatus,
      created: LocalDate)
