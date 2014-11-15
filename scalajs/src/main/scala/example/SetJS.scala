package example

import org.scalajs.dom
import org.scalajs.jquery.{jQuery => $, JQueryEventObject, JQueryStatic}
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
  def buildCardSvg(color: Int, shape: Int, pattern: Int, count: Int): Any = ???
}

@JSExport
object SetJS {

  @JSExport
  def main(wsUrl: String) = {
    val setClient = new SetClient(wsUrl)
  }

  class SetClient(url: String) {

    val scoreCardId = "score-card"
    val boardId = "game-board"

    private var cardsInPlay : List[Card] = Nil
    private var scoreCard = Map[Player, Int]()

    var selectedCards: Seq[Int] = Seq()

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
        case GameStart(cards, updatedScoreCard, gameId) =>
          cardsInPlay = cards.toList
          scoreCard = updatedScoreCard
          val content = dom.document.getElementById("content")
          content.innerHTML = ""
          content.appendChild(div(id := boardId){}.render)
          content.appendChild(div(id := scoreCardId) {}.render)
          render()
        case SetCompleted(completedSet, newCards, updatedScoreCard) =>
          //TODO: optimize to only map over cardsInPlay once
          completedSet.zip(newCards).foreach{
            case (oldCard, newCard) => cardsInPlay = cardsInPlay.map{c => if(c == oldCard) newCard else oldCard}
          }
          scoreCard = updatedScoreCard
          render()
        case GameFinished =>
          gameFinished()
        case WrongGuess =>
          wrongGuess()
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
    }

    def gameFinished() = {
      //TODO
    }

    def wrongGuess() = {
      //TODO
    }

    def render() = {
      val board = dom.document.getElementById(boardId)
      board.innerHTML = WebElements.displayGame(cardsInPlay).render.outerHTML
      updateScoreCard()
    }

    def cardSelected(index: Int) = {
      if(selectedCards.contains(index)) {
        selectedCards = selectedCards.filterNot(_.equals(index))
      } else {
        selectedCards +:= index
        if(selectedCards.length == 3) {
//          send(Guess(cardsInPlay.zipWithIndex.filter(card => selectedCards.contains(card._1))))
          println("send guess!")
        }
      }
      println("selected cards: " + selectedCards)
    }

    def updateScoreCard() = {
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

      def singleCard(card: Card, index: Int) = div(`class` := "c_" + index, value := index, onclick := { () =>
        cardSelected(index)
      }) {
      card.id.mkString(", ")
//        StdGlobalScope.buildCardSvg(card.id(0), card.id(1), card.id(2), card.id(3))

    }

      def singleCardSvg(card: Card) = StdGlobalScope.buildCardSvg(card.id(0), card.id(1), card.id(2), card.id(3))


      def displayGame(cards: List[Card]) = div(`class` := "board") {
        cards.zipWithIndex.map{case(i, card) => singleCard(i, card)}
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

