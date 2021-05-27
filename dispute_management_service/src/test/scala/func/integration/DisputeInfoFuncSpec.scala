package func.integration

import org.scalatest.wordspec.AnyWordSpec

class DisputeInfoFuncSpec extends AnyWordSpec with IntegrationTestApp {
  import dependencies._

  "this" should {
    "work" in {
      println(authToken)
    }
  }
}
