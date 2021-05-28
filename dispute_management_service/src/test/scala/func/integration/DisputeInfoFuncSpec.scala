package func.integration

import func.share.DisputeInfoRequests

import org.scalatest.wordspec.AnyWordSpec

class DisputeInfoFuncSpec
      extends AnyWordSpec
        with IntegrationTestApp
        with DisputeInfoRequests {
  import dependencies._

  "this" should {
    "work" in {
      println(authToken)
    }
  }
}
