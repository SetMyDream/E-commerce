package controllers


import play.api.routing.Router.empty.routes
import play.api.libs.EventSource
import play.api.mvc.{InjectedController}

class Application extends InjectedController with SourceDef {

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
