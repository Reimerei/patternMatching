package example

import config.Routes
import org.scalajs.dom.extensions.Ajax
import org.scalajs.spickling.PicklerRegistry
import shared.{JoinGame, ClientSends}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import js.Dynamic.{ global => g }
import org.scalajs.dom
import scalatags.JsDom._
import all._
import org.scalajs.jquery.{jQuery=>$}

@JSExport
object SetJS {

  @JSExport
  def main(settings: js.Dynamic) = {

//    new WSConnection()

  }





}

class WSConnection(url: String) {

  PicklerRegistry.register[JoinGame]

  val socket = new dom.WebSocket(url)
  socket.onmessage = receive _

  def send(message: ClientSends) = {
//    socket.send(PicklerRegistry.pickle(message))
  }

  def receive(e: dom.MessageEvent) = {
    val json = js.JSON.parse(e.data.toString)
    println(json)
  }


}
