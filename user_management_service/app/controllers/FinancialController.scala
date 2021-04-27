package controllers

import controllers.components.{BaseFinancialController, FinancialControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FinancialController @Inject() (
      cc: FinancialControllerComponents
    )(implicit ec: ExecutionContext)
      extends BaseFinancialController(cc) {
  ???
}
