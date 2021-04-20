package command.auth

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import play.api.libs.json.{JsValue, Json}

import java.io.FileInputStream
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Using}

object VaultLogin {

  def getCredentials(
      filePath: String,
      pollingFrequency: FiniteDuration = 100.milliseconds,
      pollingTimeout: FiniteDuration = 5.minutes
    )(implicit ec: ExecutionContext
    ): Future[JsValue] = {
    implicit val timeout: Timeout = pollingTimeout
    implicit val system = ActorSystem(
      jsonFileFetcher,
      "Credentials polling"
    )
    system ? { ref => CheduleFetch(filePath, ref, pollingFrequency, pollingTimeout) }
  }

  sealed trait FetcherCommand
  case class CheduleFetch(
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
      Behaviors.receiveMessagePartial {
        case CheduleFetch(path, replyTo, pollingFrequency, pollingTimeout) =>
          context.log.trace(s"Scheduling fetching for file $path")
          timers.startTimerWithFixedDelay(
            key = path,
            FetchFile(path, replyTo),
            pollingFrequency
          )
          timers.startSingleTimer(key = path, Stop(path), pollingTimeout)
          Behaviors.same
        case FetchFile(path, replyTo) =>
          context.log.trace(s"Fetching for file $path")
          Using(new FileInputStream(path)) { file =>
            context.log.trace(s"Successfully fetched file $path")
            replyTo ! Json.parse(file)
          } match {
            case _: Success[_] => Behaviors.stopped
            case _: Failure[_] => Behaviors.same
          }
        case Stop(path) =>
          context.log.error(s"Couldn't find the file $path")
          Behaviors.stopped
      }
    }
  }

}
