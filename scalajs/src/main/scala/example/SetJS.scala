package example

import config.Routes
import org.scalajs.dom.extensions.Ajax
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import js.Dynamic.{ global => g }
import org.scalajs.dom
import scalatags.JsDom._
import all._
import org.scalajs.jquery.{jQuery=>$}

@JSExport
object SetJS {

  val maxMessages = 20

  var assetsDir: String = ""
  var wsBaseUrl: String = ""

//  var client: Option[ChatClient] = None


}