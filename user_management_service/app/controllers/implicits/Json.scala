package controllers.implicits

import org.joda.time.DateTime
import play.api.libs.json.{Format, JodaReads, JodaWrites}


object Json {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val dateFormat = Format[DateTime](
    JodaReads.jodaDateReads(pattern),
    JodaWrites.jodaDateWrites(pattern)
  )
}
