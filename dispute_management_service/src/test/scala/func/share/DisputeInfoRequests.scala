package func.share

import cats.effect.IO
import org.http4s.{Header, Request, Uri}

trait DisputeInfoRequests {

  def `request dispute info with auth token`(
      authTokenHeader: String,
      authToken: String,
      disputeId: Long
    ): Request[IO] = {
    val authHeader = Header(authTokenHeader, authToken)
    makeGetRequest(Uri(path = "/info/" + disputeId), authHeader)
  }

  def `request dispute info with no auth token`(
      disputeId: Long
    ): Request[IO] = {
    makeGetRequest(Uri(path = "/info/" + disputeId))
  }

  def `request disputes list with auth token`(
      authTokenHeader: String,
      authToken: String
    ) = {
    val authHeader = Header(authTokenHeader, authToken)
    makeGetRequest(Uri(path = "/list/all"), authHeader)
  }

  def makeGetRequest(
      uri: Uri,
      headers: Header*
    ): Request[IO] =
    Request[IO](uri = uri).withHeaders(headers: _*)

}
