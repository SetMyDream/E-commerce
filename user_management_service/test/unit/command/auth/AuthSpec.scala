package unit.command.auth

import command.auth.VaultLogin
import command.model.AppRoleCredentials
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import play.api.libs.json.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Millis, Span}

import java.io.{BufferedWriter, File, FileWriter}

class AuthSpec extends AnyFlatSpec with Matchers {

  "getCredentials" should "fetch the contents of a json file" in {
    val filename = System.getProperty("user.dir") + "/credentials.json"
    val credentials = Json.toJson(
      AppRoleCredentials(
        "f4f97a69-b31d-41b9-2c90-a389de773e4a",
        "69030a28-4bfc-1f29-45ae-bc89bdfbb548",
        "daa11e13-930b-3051-9d02-fb43f17e3dbd",
        28800
      )
    )

    val fileFetchingFuture = VaultLogin.getCredentials(filename)
    val file = writeFile(filename, credentials.toString())

    fileFetchingFuture.futureValue(Timeout(Span(500, Millis))) shouldBe credentials

    file.delete()
  }

  private def writeFile(
      filename: String,
      content: String
    ): File = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
    file
  }

}
