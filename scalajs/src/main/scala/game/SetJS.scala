package game


/**
 * User: Björn Reimer
 * Date: 15.11.14
 * Time: 20:40
 */

import org.scalajs.dom
import org.scalajs.jquery.{ jQuery => $, JQueryEventObject, JQueryStatic }
import org.scalajs.spickling.PicklerRegistry
import org.scalajs.spickling.jsany._
import shared._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ global => g }
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

import Pickles._

object StdGlobalScope extends js.GlobalScope {
  def buildCardSvg(className: String, color: Int, shape: Int, pattern: Int, count: Int, isSelected: Boolean): Any = ???
}

@JSExport
object SetJS {

  @JSExport
  def main(wsUrl: String) = {
    Pickles.register()
    val setClient = new SetClient(wsUrl)
  }

  class SetClient(url: String) {

    val scoreCardId = "score-card"
    val boardId = "game-board"
    val gameFinishedMessageId = "game-finished-message"

    private var playerName = ""
    private var cardsInPlay : List[Card] = Nil
    private var scoreCard = Map[Player, Int]()

    var selectedCards: Seq[Int] = Seq()


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
          content.appendChild(div(id := gameFinishedMessageId).render)
          content.appendChild(div(id := boardId){}.render)
          content.appendChild(div(id := scoreCardId) {}.render)
          render()
        case SetCompleted(completedSet, newCards, updatedScoreCard) =>
          updateSets(completedSet, newCards, updatedScoreCard)
        case NoCardsLeft(completedSet, updatedScoreCard) =>
          updateSets(completedSet, Set.empty, updatedScoreCard)
        case GameFinished(finalScoreCard) =>
          scoreCard = finalScoreCard
          gameFinished()
        case WrongGuess =>
          wrongGuess()
        case x => println("unknown message: " + println(x))
      }
    }

    def updateSets(completedSet : Set[Card], newCards : Set[Card], updatedScoreCard : Map[Player, Int]) = {
      //TODO: optimize to only map over cardsInPlay once
      val paddedNewCards : List[Option[Card]] =
        if(completedSet.size == newCards.size) newCards.map(Some(_)).toList
        else {
          val pad = completedSet.size - newCards.size
          newCards.map(Some(_)).toList ::: List.fill(pad)(None)
        }
      completedSet.zip(paddedNewCards).foreach {
        case (oldCard, newCard : Option[Card]) => {
          cardsInPlay = cardsInPlay.flatMap(c => if (c == oldCard) newCard else Some(c))
        }
      }
      scoreCard = updatedScoreCard
      selectedCards = Seq()
      render()
    }

    def onStart(e: dom.Event) = {
      val content = dom.document.getElementById("content")
      content.appendChild(WebElements.enterName().render)
    }

    def joinGame(name: String) = {
      playerName = name
      this.send(JoinGameWithoutId(name))
      val content = dom.document.getElementById("content")
      content.innerHTML = ""
      content.appendChild(WebElements.waitingForGame.render)
    }

    def gameFinished() = {
      val (winner, _) = scoreCard.maxBy{case(player, score) => score}
      val message = if(winner.name == playerName) "You won!" else "You lost!"

      val finishMessage = dom.document.getElementById(gameFinishedMessageId)
      finishMessage.innerHTML = message

      render()
    }

    def wrongGuess() = {
      selectedCards = Seq()
      render()
    }

    def render(): Any = {
      val board = dom.document.getElementById(boardId)
      board.innerHTML = ""
      board.appendChild(WebElements.displayGame(cardsInPlay).render)
      updateScoreCard()
      cardsInPlay.zipWithIndex.foreach{
        case (card, j) =>
          StdGlobalScope.buildCardSvg("c_" + j, card.id(0), card.id(1), card.id(2), card.id(3), selectedCards.contains(j))
      }
    }

    def cardSelected(index: Int) = {
      if (selectedCards.contains(index)) {
        selectedCards = selectedCards.filterNot(_.equals(index))
      } else {
        selectedCards +:= index
        if (selectedCards.length == 3) {
          send(Guess(cardsInPlay.zipWithIndex.filter(card => selectedCards.contains(card._2)).map(_._1).toSet))
          println("send guess!")
        }
      }
      render()
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

      def singleCard(card: Card, index: Int) = div(`class` := "c_" + index + " col-xs-3", onclick := { () =>
        println("clicked")
        cardSelected(index)
      }) { }

      def displayGame(cards: List[Card]) = div(`class` := "row") {
        cards.zipWithIndex.map { case (card,j) =>
          singleCard(card, j) }
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


