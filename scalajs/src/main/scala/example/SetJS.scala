package example

import config.Routes
import org.scalajs.dom.extensions.Ajax
import org.scalajs.spickling.PicklerRegistry
import shared.{CreateGame, JoinGame, ClientSends}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import js.Dynamic.{ global => g }
import org.scalajs.dom
import scalatags.JsDom._
import all._
import org.scalajs.jquery.{jQuery=>$}
import org.scalajs.spickling.jsany._


@JSExport
object SetJS {

  @JSExport
  def main(wsUrl: String) = {
    val connection = new WSConnection(wsUrl)
  }

  class WSConnection(url: String) {

    PicklerRegistry.register[CreateGame]

    val socket = new dom.WebSocket(url)
    socket.onmessage = receive _
    socket.onopen = onStart _

    def send(message: ClientSends) = {
      val json = PicklerRegistry.pickle(message)
      println("sending: " + json)
      socket.send(json)
    }

    def receive(e: dom.MessageEvent) = {
      val json = js.JSON.parse(e.data.toString)
      println(json)
    }

    def onStart(e: dom.Event) = {
      this.send(CreateGame("foo"))
    }


  }


}

