package models

import akka.actor.{Props, ActorRef, Actor}
import shared._

import scala.util.Random

object Game {
  def props(gameId: Long): Props = Props(new Game(gameId))
}

class Game(gameId: Long) extends Actor {

  val BOARD_SIZE: Int = 12

  var players: Map[ActorRef, (String, Int)] = Map()


  override def receive: Receive = pending

  def pending: Receive = {
    case JoinGame(name, _) =>
      players += sender() -> (name, 0)
      if (players.size == 2){
        val deck: Seq[Card] = createDeck
        context.become(active(deck))
        val state: GameStart = GameStart(deck.take(BOARD_SIZE).toSet, scoreCard, gameId)
        context.parent ! state
        publish(state)
      }
  }

  def active(deck: Seq[Card]): Receive = {
    case Guess(set) =>
      if (validate(set)) {
        updateScore(sender)
        context.become(active(deck.drop(3)))
        publish(SetCompleted(set, scoreCard))
        if (!hasMoreSets)
          publish(GameFinished(scoreCard))
      }
      else {
        sender ! WrongGuess
      }
  }

  def updateScore(player: ActorRef): Unit =
    players = players.updated(player, players(player).copy(_2 = players(player)._2 + 1))

  def createDeck: Seq[Card] = {
    val deck = for {
      color <- 0 to 2
      shape <- 0 to 2
      fill <- 0 to 2
      count <- 0 to 2
    } yield Card(Seq(color, shape, fill, count))
    Random.shuffle(deck)
  }

  def scoreCard: Map[Player, Int] = players.values.map(p => Player(p._1) -> p._2).toMap

  def validate(set: Set[Card]): Boolean = false // TODO

  def hasMoreSets: Boolean = true // TODO

  def publish(msg: Any): Unit = players.keys.foreach(_ ! msg)

}
