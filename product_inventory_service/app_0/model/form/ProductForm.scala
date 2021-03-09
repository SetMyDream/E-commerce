package model.form

import play.api.data.Form
import play.api.data.Forms.{email, longNumber, mapping, nonEmptyText}

case class ProductFormData(title: String, description: String, userId: Long, emailOfSeller: String)

object ProductForm {

  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "mobile" -> longNumber,
      "emailOfSeller" -> email
    )(ProductFormData.apply)(ProductFormData.unapply)
  )
}
