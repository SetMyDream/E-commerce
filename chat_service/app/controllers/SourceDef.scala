package controllers

import play.api.libs.json.JsValue
import akka.stream.scaladsl.Source

trait SourceDef {
  def chatChannel: Source[JsValue, _] = {

  }


  def ChatOut: Source[JsValue, _] = {

  }

}
