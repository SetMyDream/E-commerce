package storage.model

import enumeratum.values._

sealed abstract class DisputeStatus(val value: Byte) extends ByteEnumEntry

object DisputeStatus
      extends ByteEnum[DisputeStatus]
        with ByteQuillEnum[DisputeStatus] {
  val values = findValues

  case object Active extends DisputeStatus(1)
  case object Waiting extends DisputeStatus(2)
  case object Resolved extends DisputeStatus(3)
}
