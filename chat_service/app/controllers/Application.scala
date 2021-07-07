package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}

import play.api.routing.Router.empty.routes
import play.api.libs.EventSource
import play.api.mvc.{ControllerComponents, InjectedController}

import javax.inject.Inject

class Application  @Inject() (override val controllerComponents: ControllerComponents,
                              inputSanitizer: InputSanitizer)
  (implicit mat: Materializer) extends InjectedController
{
  private type WSMessage = String

  // chat room many clients -> merge hub -> broadcasthub -> many clients
  val (chatOut, chatChannel) = {
    // recoverWithRetries -1 is essentially "recoverWith"
    val source = MergeHub.source[WSMessage]
      .log("source")
      // Input sanitization to avoid XSS attacks
      .map(inputSanitizer.sanitize)
      .recoverWithRetries(-1, { case _: Exception => Source.empty })

    val sink = BroadcastHub.sink[WSMessage]
    source.toMat(sink)(Keep.both).run()
  }

   def index = Action { implicit req =>
    Ok(views.html.index(routes.Application.chatFeed(), routes.Application.postMessage()))
  }

  def chatFeed = Action { req =>
    println("User connected to chat: " + req.remoteAddress)
    Ok.chunked(chatOut
      &> EventSource()
    ).as("text/event-stream")
  }

  def postMessage = Action(parse.json) { req =>
    chatChannel.push(req.body)
    Ok
  }

}
