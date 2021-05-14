package controllers

import play.api.libs.json.JsValue
import akka.stream.scaladsl.Source

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

trait SourceDef {
  def chatChannel: Source[JsValue, _] = {
    val df: DateTimeFormatter = DateTimeFormatter.ofPattern("HH mm ss")
    val tickSource = Source.tick(0.millis, 100.millis, "TICK")
    val s = tickSource.map(_ => df.format(ZonedDateTime.now()))
    s
  }


  def ChatOut: Source[JsValue, _] = {

  }

}
