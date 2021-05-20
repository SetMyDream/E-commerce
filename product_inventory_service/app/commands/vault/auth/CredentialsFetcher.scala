package commands.vault.auth

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout
import play.api.libs.json.{JsValue, Json}

import java.io.FileInputStream
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Using}

object CredentialsFetcher {

  def getCredentials(
      filePath: String,
      pollingFrequency: FiniteDuration = 100.milliseconds,
      pollingTimeout: FiniteDuration = 5.minutes
    ): Future[JsValue] = {
    implicit val timeout: Timeout = pollingTimeout
    implicit val system = ActorSystem(jsonFileFetcher, "CredentialsPolling")

    system ? { ref =>
      ScheduleFetch(filePath, ref, pollingFrequency, pollingTimeout)
    }
  }

  sealed trait FetcherCommand
  case class ScheduleFetch(
        path: String,
        replyTo: ActorRef[JsValue],
        pollingFrequency: FiniteDuration,
        pollingTimeout: FiniteDuration)
        extends FetcherCommand
  case class FetchFile(
        path: String,
        replyTo: ActorRef[JsValue])
        extends FetcherCommand
  case class Stop(path: String) extends FetcherCommand

  def jsonFileFetcher: Behavior[FetcherCommand] = Behaviors.setup { context =>
    Behaviors.withTimers[FetcherCommand] { timers =>
      val log = context.log
      Behaviors.receiveMessagePartial {
        case ScheduleFetch(path, replyTo, pollingFrequency, pollingTimeout) =>
          val fetchMessage = FetchFile(path, replyTo)
          val stopMessage = Stop(path)

          log.debug(s"Scheduling fetching for file $path")
          timers.startTimerWithFixedDelay(
            s"Poller $path",
            fetchMessage,
            pollingFrequency
          )
          timers.startSingleTimer(
            s"Poller timeout $path",
            stopMessage,
            pollingTimeout
          )
          Behaviors.same

        case FetchFile(path, replyTo) =>
          log.trace(s"Trying to read file $path")
          Using(new FileInputStream(path)) { file =>
            log.debug(s"Successfully fetched file $path")
            replyTo ! Json.parse(file)
          } match {
            case _: Success[_] => Behaviors.stopped
            case _: Failure[_] => Behaviors.same
          }

        case Stop(path) =>
          log.error(s"Couldn't find the file $path")
          Behaviors.stopped
      }
    }
  }

}
