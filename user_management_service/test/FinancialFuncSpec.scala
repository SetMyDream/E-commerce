import controllers.validators.TransferValidator
import storage.WalletResourceHandler
import util.{PostgresSuite, SimpleFakeRequest, UserFixtures}

import org.scalatest.concurrent.ScalaFutures.{convertScalaFuture, PatienceConfig}
import org.scalatest.time.{Millis, Span}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class FinancialFuncSpec
      extends PlaySpec
        with PostgresSuite
        with UserFixtures
        with SimpleFakeRequest {
  implicit val patience = PatienceConfig(Span(1500, Millis))

  "FinancialController" must {
    "show the authenticated user's balance" in withWallet() { wallet =>
      val token = authenticate(wallet.userId)
      val resp = makeEmptyRequest("/balance", authToken = token)
      status(resp) mustBe OK
      contentAsJson(resp) mustBe Json.toJson(wallet)
    }

    "transfer funds between two accounts" in withTwoWallets() { (from, to) =>
      val amount = 500
      val totp = makeTOTP(from.userId.toString)
      val resp = makeTransferRequest(from.userId, to.userId, amount, totp)

      status(resp) mustBe OK
      getBalance(from.userId) mustBe from.balance - amount
      getBalance(to.userId) mustBe to.balance + amount
    }

    "not transfer funds if the payer doesn't have enough " +
      "on their balance" in withTwoWallets(499) { (from, to) =>
        val totp = makeTOTP(from.userId.toString)
        val resp = makeTransferRequest(from.userId, to.userId, 500, totp)

        status(resp) mustBe PAYMENT_REQUIRED
        getBalance(from.userId) mustBe from.balance
        getBalance(to.userId) mustBe to.balance
      }

    "not transfer funds if TOTP code is invalid" in withTwoWallets() { (from, to) =>
      val totp = makeInvalidCopy(makeTOTP(from.userId.toString))
      val resp = makeTransferRequest(from.userId, to.userId, 500, totp)

      status(resp) mustBe UNAUTHORIZED
      getBalance(from.userId) mustBe from.balance
      getBalance(to.userId) mustBe to.balance
    }

    "not transfer funds if amount is not a positive number" in withTwoWallets() {
      (from, to) =>
        val totp = makeTOTP(from.userId.toString)
        val resp = makeTransferRequest(from.userId, to.userId, -500, totp)

        status(resp) mustBe BAD_REQUEST
        getBalance(from.userId) mustBe from.balance
        getBalance(to.userId) mustBe to.balance
    }

    "not transfer funds if payer doesn't exist" in withRegisteredDummyUser() { to =>
      val initialBalance = getBalance(to)
      val resp = makeTransferRequest(999, to, 500, "999999")

      status(resp) mustBe BAD_REQUEST
      getBalance(to) mustBe initialBalance
    }

    "not transfer if recipient doesn't exist" in withRegisteredDummyUser() { from =>
      val initialBalance = getBalance(from)
      val resp = makeTransferRequest(from, 999, 500, makeTOTP(from.toString))

      status(resp) mustBe BAD_REQUEST
      getBalance(from) mustBe initialBalance
    }

  }

  def getBalance(userId: Long): BigDecimal =
    inject[WalletResourceHandler].find(userId).futureValue.get.balance

  def makeTOTP(keyPostfix: String): String =
    vaultClient.generateTOTPCode(keyPostfix).futureValue

  def makeTransferRequest(
      from: Long,
      to: Long,
      amount: BigDecimal,
      totp: String
    ): Future[Result] = {
    val transfer = TransferValidator(from, to, amount, totp)
    makeJsonRequest("/transfer", POST, Json.toJson(transfer))
  }
}
