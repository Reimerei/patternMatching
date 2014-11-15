package controllers

import play.api.mvc.{WebSocket, Action, Controller}
import models.UserActor

/**
 * User: Björn Reimer
 * Date: 15.11.14
 * Time: 12:19
 */
object SetController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.set())
  }

  def socket = WebSocket.acceptWithActor[String, String] {
    request => out =>
      UserActor.props(out)
  }

}
