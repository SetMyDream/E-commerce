package unit.command.auth

import command.auth.VaultLogin._
import command.model.AppRoleCredentials

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import play.api.libs.json.{JsValue, Json}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Millis, Span}

import scala.concurrent.duration._

class FileFetcherSpec
      extends ScalaTestWithActorTestKit
        with AnyFlatSpecLike
        with AuthSpecFixtures {
  val filename = System.getProperty("user.dir") + "/credentials.json"
  val credentials = Json.toJson(
    AppRoleCredentials(
      "f4f97a69-b31d-41b9-2c90-a389de773e4a",
      "69030a28-4bfc-1f29-45ae-bc89bdfbb548",
      "daa11e13-930b-3051-9d02-fb43f17e3dbd",
      28800
    )
  )

  "getCredentials" should "fetch the contents of a json file" in {
    val fileFetchingFuture = getCredentials(filename)
    withFile(filename, credentials.toString()) {
      val responseTimeout = Timeout(Span(500, Millis))
      fileFetchingFuture.futureValue(responseTimeout) shouldBe credentials
    }
  }

  "jsonFileFetcher" should "stop after successfully fetching a file" in withFile(
    filename,
    credentials.toString()
  ) {
    val fetcher = testKit.spawn(jsonFileFetcher, "fetcher")
    val probe = testKit.createTestProbe[JsValue]()
    fetcher ! ScheduleFetch(filename, probe.ref, 10.milliseconds, 1.minute)
    probe.expectMessageType[JsValue]
    probe.expectTerminated(fetcher, 500.milliseconds)
  }

  "jsonFileFetcher" should "stop if it didn't fetch a file until specified timeout" in {
    val fetcher = testKit.spawn(jsonFileFetcher, "fetcher")
    val probe = testKit.createTestProbe[JsValue]()
    fetcher ! ScheduleFetch(filename, probe.ref, 10.milliseconds, 11.milliseconds)
    probe.expectNoMessage()
    probe.expectTerminated(fetcher, 100.milliseconds)
  }

}
