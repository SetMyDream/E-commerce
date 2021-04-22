package unit.command

import commands.vault.VaultCommands
import commands.vault.model.AppRoleCredentials

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Span}
import play.api.Configuration
import play.api.libs.json._
import play.api.test._
import play.api.mvc.Results._
import play.api.routing.sird._
import play.core.server.Server

import scala.concurrent.ExecutionContext.Implicits.global

class VaultCommandsSpec extends AnyFlatSpec with Matchers {

  "login" should "return an auth token" in
    Server.withRouterFromComponents() { components =>
      { case POST(p"/auth/approle/login") =>
        components.defaultActionBuilder {
          Ok(Json.parse("""{
          |  "auth": {
          |    "renewable": true,
          |    "lease_duration": 1200,
          |    "metadata": null,
          |    "token_policies": ["default"],
          |    "accessor": "fd6c9a00-d2dc-3b11-0be5-af7ae0e1d374",
          |    "client_token": "5b1a0318-679c-9c45-e5c6-d1b9a9035"
          |  },
          |  "warnings": null,
          |  "wrap_info": null,
          |  "data": null,
          |  "lease_duration": 0,
          |  "renewable": false,
          |  "lease_id": ""
          |}""".stripMargin))
        }
      }
    } { implicit port =>
      WsTestClient.withClient { client =>
        val config = Configuration("vault.api.path" -> "")
        val commandsAPI = new VaultCommands(client, config, null)
        val responseTimeout = Timeout(Span(1000, Millis))
        commandsAPI
          .login(AppRoleCredentials("", "", "", 1))
          .futureValue(responseTimeout) shouldBe "5b1a0318-679c-9c45-e5c6-d1b9a9035"
      }
    }

}
