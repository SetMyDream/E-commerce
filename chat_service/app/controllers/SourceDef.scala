package controllers

import play.api.libs.json.JsValue
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}
import play.api.mvc.ControllerComponents

import javax.inject.Inject

trait SourceDef @Inject() (val controllerComponents: ControllerComponents, inputSanitizer: InputSanitizer){
  val chatChannel = MergeHub.source[JsValue]
    .log("source")
    // Let's also do some input sanitization to avoid XSS attacks
    .map(inputSanitizer.sanitize)
    .recoverWithRetries(-1, { case _: Exception => Source.empty })

  val ChatOut = BroadcastHub.sink[JsValue]
  chatChannel.toMat (ChatOut) (Keep.both).run ()
}
