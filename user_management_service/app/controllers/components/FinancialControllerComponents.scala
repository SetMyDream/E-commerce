package controllers.components

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Silhouette
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{BaseController, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}
import storage.WalletResourceHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext

final case class FinancialControllerComponents @Inject() (
      walletResourceHandler: WalletResourceHandler,
      silhouette: Silhouette[DefaultEnv],
      actionBuilder: DefaultActionBuilder,
      parsers: PlayBodyParsers,
      fileMimeTypes: FileMimeTypes,
      langs: Langs,
      messagesApi: MessagesApi,
      executionContext: ExecutionContext)
      extends ControllerComponents

class BaseFinancialController @Inject() (fcc: FinancialControllerComponents)
      extends BaseController {

  override protected def controllerComponents: ControllerComponents = fcc

  def walletResourceHandler: WalletResourceHandler = fcc.walletResourceHandler
  def silhouette: Silhouette[DefaultEnv] = fcc.silhouette
}
