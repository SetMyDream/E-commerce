package controllers

import play.api._
import play.api.libs.EventSource
import play.api.libs.iteratee.{Iteratee, Concurrent}
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

class Application extends Controller {

  val (chatOut, chatChannel) = Concurrent.broadcast[JsValue]

  // Following two lines are just for debugging broadcast
  val chatDebug = Iteratee.foreach[JsValue](m => println("Debug: " + m.toString))
  chatOut |>>> chatDebug

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
