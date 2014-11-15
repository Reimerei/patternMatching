package models

import akka.actor.{Props, ActorRef, Actor}
import shared._

import scala.util.Random

object Game {
  def props(gameId: Long): Props = Props(new Game(gameId))

  def validateBits: Seq[Int] => Boolean =
    s => {
      val size: Int = s.toSet.size
      size == 1 || size == s.size
    }

  def validate(set: Seq[Card], deck: Set[Card]): Boolean = {
    val n: Int = set.headOption.map(_.id.size).getOrElse(0)
    val bits: Seq[Seq[Int]] = for {
      i <- 0 until n
    } yield set.map(_.id(i))

    bits.forall(validateBits)
  }


  def hasMoreSets(deck: Set[Card]): Boolean = true // TODO
}

class Game(gameId: Long) extends Actor {

  val BOARD_SIZE: Int = 12
  val SET_SIZE: Int = 3

  var players: Map[ActorRef, (String, Int)] = Map()


  override def receive: Receive = pending

  def pending: Receive = {
    case JoinGame(name, _) =>
      players += sender() -> (name, 0)
      if (players.size == 2){
        val deck: Seq[Card] = createDeck
        context.become(active(deck))
        val state: GameStart = GameStart(activeCards(deck), scoreCard, gameId)
        context.parent ! state
        publish(state)
      }
  }

  def active(deck: Seq[Card]): Receive = {
    case Guess(set) =>
      if (Game.validate(set.toSeq, activeCards(deck))) {
        updateScore(sender)
        context.become(active(deck.drop(3)))
        publish(SetCompleted(set, scoreCard))
        if (!Game.hasMoreSets(activeCards(deck)))
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


  def publish(msg: Any): Unit = players.keys.foreach(_ ! msg)

  def activeCards(deck: Seq[Card]): Set[Card] = deck.take(BOARD_SIZE).toSet

}
