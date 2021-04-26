import commands.vault.{VaultClient, VaultCommands}
import util.{InjectedServices, SimpleFakeRequest}

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Millis, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._

class CommandsSpec
      extends PlaySpec
        with GuiceOneAppPerSuite
        with InjectedServices
        with SimpleFakeRequest {
  val timeout = Timeout(Span(500, Millis))
  "VaultCommands" must {
    "create a TOTP key in Vault" in withGeneratedTOTPKey("1") {
      (cmd, client, keyPostfix, accName) =>
        val vaultRes = client
          .authenticatedRequest(
            "/totp/keys/" + cmd.keyName(keyPostfix)
          )
          .get()
          .futureValue(timeout)
        vaultRes.status mustBe OK
        val resData = vaultRes.json \ "data"
        resData("account_name").as[String] mustBe accName
    }

    "validate a TOTP code generated in Vault" in withGeneratedTOTPKey("2") {
      (_, client, keyPostfix, _) =>
        val code = client.generateTOTPCode(keyPostfix).futureValue(timeout)
        val isValid = client.validateTOTPCode(keyPostfix, code).futureValue(timeout)
        isValid mustBe true
    }

    "return false for an invalid TOTP code" in withGeneratedTOTPKey("3") {
      (_, client, keyPostfix, _) =>
        val realCode = client.generateTOTPCode(keyPostfix).futureValue(timeout)
        val invalidCode = makeInvalidCopy(realCode)
        val isValid = client.validateTOTPCode(keyPostfix, invalidCode).futureValue(timeout)
        isValid mustBe false
    }
  }

  def withGeneratedTOTPKey(
      keyPostpostfix: String,
      accName: String = "account_name"
    )(f: (VaultCommands, VaultClient, String, String) => Unit
    ): Unit = {
    val cmd = inject[VaultCommands]
    val client = cmd.client.futureValue(timeout)

    val keyPostfix = "test_" + keyPostpostfix
    val res = client.generateTOTPKey(keyPostfix, accName).futureValue(timeout)
    res.status mustBe NO_CONTENT

    f(cmd, client, keyPostfix, accName)
  }
}
