package example

import org.scalajs.dom
import org.scalajs.jquery.{ jQuery => $, JQueryStatic }
import org.scalajs.spickling.PicklerRegistry
import org.scalajs.spickling.jsany._
import shared._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ global => g }
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom._
import scalatags.JsDom.all._

import Pickles._

object StdGlobalScope extends js.GlobalScope {
  def buildCardSvg(color: Int, shape: Int, pattern: Int, count: Int): scalatags.JsDom.Modifier = ???
}

@JSExport
object SetJS {

  @JSExport
  def main(wsUrl: String) = {
    val setClient = new SetClient(wsUrl)
  }

  class SetClient(url: String) {

    val scoreCardId = "score-card"

    Pickles.register()

    val socket = new dom.WebSocket(url)
    socket.onmessage = receive _
    socket.onopen = onStart _

    def send(message: ClientSends) = {
      val json = PicklerRegistry.pickle(message)
      socket.send(JSON.stringify(json))
    }

    def receive(e: dom.MessageEvent) = {
      val json = js.JSON.parse(e.data.toString).asInstanceOf[js.Any]
      PicklerRegistry.unpickle(json) match {
        case GameStart(cards, scoreCard, gameId) =>
          val content = dom.document.getElementById("content")
          content.innerHTML = ""
          content.appendChild(WebElements.displayGame(cards).render)
          content.appendChild(div(id := scoreCardId) {}.render)
          updateScoreCard(scoreCard)
        case SetCompleted(completedSet, newCards, scoreCard) =>
          updateBoard(completedSet, newCards)
          updateScoreCard(scoreCard)
        case x => println("unknown message: " + println(x))
      }
    }

    def onStart(e: dom.Event) = {
      val content = dom.document.getElementById("content")
      content.appendChild(WebElements.enterName().render)
    }

    def joinGame(name: String) = {
      this.send(JoinGameWithoutId(name))
      val content = dom.document.getElementById("content")
      content.innerHTML = ""
      content.appendChild(WebElements.waitingForGame.render)
      val cards = Set(Card(List(1, 2, 3, 4)))
      content.appendChild(WebElements.displayGame(cards).render)
    }

    def updateBoard(completedSet: Set[Card], newCards: Set[Card]) = {
      //TODO
    }

    def updateScoreCard(scoreCard: Map[Player, Int]) = {
      val scorecard = dom.document.getElementById(scoreCardId)
      scorecard.innerHTML = WebElements.scorecard(scoreCard).render.outerHTML
    }

    object WebElements {

      def enterName() = div(id := "signInPanel") {
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
              joinGame(input)
            }
            false
          })("Sign in")
        )
      }

      def waitingForGame = div() {
        "Waiting For Game..."
      }

      def singleCard(card: Card) = div(`class` := "card", onclick := { () =>

        if ($("this").hasClass("select")) {
          $("this").removeClass("select")
        } else {
          $("this").addClass("select")
        }

        if ($(".select").length == 3) {
          println($(".select").attr("value"))
          // todo send to backend

          $(".select").removeClass("select")
        }
      }
    ) {
//      card.id.mkString(", ")
        StdGlobalScope.buildCardSvg(card.id(0), card.id(1), card.id(2), card.id(3))

    }

      def displayGame(cards: Set[Card]) = div(`class` := "board") {
        cards.toSeq.map(singleCard)
      }

      def scorecard(scoreCard: Map[Player, Int]) = {
        val headers = tr(th("Player"), th("Score"))
        val data = scoreCard.map {
          case (player, score) =>
            tr(td(player.name), td(score))
        }
        table(tbody(List(headers) ++ data))
      }

    }

  }
}

