package example

import org.scalajs.dom
import org.scalajs.jquery.{jQuery => $}
import org.scalajs.spickling.PicklerRegistry
import org.scalajs.spickling.jsany._
import shared._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom._
import scalatags.JsDom.all._


@JSExport
object SetJS {


  @JSExport
  def main(wsUrl: String) = {
    val setClient = new SetClient(wsUrl)
  }

  class SetClient(url: String) {

    PicklerRegistry.register[CreateGame]
    PicklerRegistry.register[JoinGameWithoutId]
    PicklerRegistry.register[JoinGameWithId]
    PicklerRegistry.register[GameStart]

    val socket = new dom.WebSocket(url)
    socket.onmessage = receive _
    socket.onopen = onStart _

    def send(message: ClientSends) = {
      val json = PicklerRegistry.pickle(message)
      socket.send(JSON.stringify(json))
    }

    def receive(e: dom.MessageEvent) = {
      println("received: " + e.data.toString)
//      val json = js.JSON.parse(e.data.toString)
      PicklerRegistry.unpickle(e.data) match {
        case GameStart(cards, scoreCard, gameId) =>
          println("game start!")
        case x => println("unknown message: " + println(x))
      }
    }

    def onStart(e: dom.Event) = {
      val content = dom.document.getElementById("content")
      content.appendChild(WebElements.enterName(this).render)
    }

    def joinGame(name: String) = {
      this.send(JoinGameWithoutId(name))
      val content = dom.document.getElementById("content")
      content.innerHTML = ""
      content.appendChild(WebElements.waitingForGame.render)
    }
  }



  object WebElements {

    def enterName(client: SetClient) = div(id := "signInPanel") {
      form(`class` := "form-inline", "role".attr := "form")(
        div(id := "usernameForm", `class` := "form-group")(
          div(`class` := "input-group")(
            div(`class` := "input-group-addon", raw("&#9786;")),
            input(id := "username", `class` := "form-control", `type` := "text", placeholder := "Enter username")
          )
        ),
        span(style := "margin:0px 5px"),
        button(`class` := "btn btn-default", onclick := { () =>
          val input = $("#username").value().toString.trim
          if (input == "") {
            $("#usernameForm").addClass("has-error")
            dom.alert("Invalid username")
          } else {
             client.joinGame(input)
          }
          false
        })("Sign in")
      )
    }

    def waitingForGame = div(){
      "Waiting For Game..."
    }

//    def renderGame(cards)


  }


}

