import play.api.http.DefaultHttpErrorHandler


/**
 * Fix exceptions being masked by Play's default error handler
 *
 * https://github.com/playframework/playframework/issues/10486
 */
class AppErrorHandler extends DefaultHttpErrorHandler(sourceMapper = None)