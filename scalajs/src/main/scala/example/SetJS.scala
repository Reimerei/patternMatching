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

import Pickles._

@JSExport
object SetJS {


  @JSExport
  def main(wsUrl: String) = {
    val setClient = new SetClient(wsUrl)
  }

  class SetClient(url: String) {

    Pickles.register()

    val socket = new dom.WebSocket(url)
    socket.onmessage = receive _
    socket.onopen = onStart _

    def send(message: ClientSends) = {
      val json = PicklerRegistry.pickle(message)
      socket.send(JSON.stringify(json))
    }

    def receive(e: dom.MessageEvent) = {
      println("received: " + e.data.toString)
      val json = js.JSON.parse(e.data.toString).asInstanceOf[js.Any]
      PicklerRegistry.unpickle(json) match {
        case GameStart(cards, scoreCard, gameId) =>
          val content = dom.document.getElementById("content")
          content.innerHTML = ""
          content.appendChild(WebElements.displayGame(cards).render)
        case SetCompleted(newCards, scoreCard) =>
          updateBoard(newCards)
          updateScoreCard(scoreCard)
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
      val cards = Set(Card(List(1,2,3,4)))
      content.appendChild(WebElements.displayGame(cards).render)
    }

    def updateBoard(newCards : Set[Card]) = {
      //TODO
    }

    def updateScoreCard(scoreCard : Map[Player, Int]) = {
      //TODO
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

    def singleCard(card: Card) = div(`class` := "card" ){
       card.id.mkString(", ")

    }

    def displayGame(cards: Set[Card])  = div(`class` := "board"){
      cards.toSeq.map(singleCard)
    }

  }



}

