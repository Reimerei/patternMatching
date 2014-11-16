package models

import akka.actor._
import akka.event.LoggingReceive
import shared._
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

object Game {
  def props(gameId: Long): Props = Props(new Game(gameId))


  def createDeck: Seq[Card] = {
    val deck = for {
      color <- 0 to 2
      shape <- 0 to 2
      fill <- 0 to 2
      count <- 0 to 2
    } yield Card(List(color, shape, fill, count))
    Random.shuffle(deck)
  }

  def validateBits: Seq[Int] => Boolean =
    s => {
      val size: Int = s.toSet.size
      size == 1 || size == s.size
    }

  def validateEqualityRule(set: Seq[Card]): Boolean = {
    val n: Int = set.headOption.map(_.id.size).getOrElse(0)
    val bits: Seq[Seq[Int]] = for {
      i <- 0 until n
    } yield set.map(_.id(i))

    bits.forall(validateBits)
  }

  def validateDeckPresence(set: Seq[Card], deck: Set[Card]): Boolean = {
    set.toSet.subsetOf(deck)
  }

  def validate(set: Seq[Card], deck: Set[Card]): Boolean = {
    validateEqualityRule(set) && validateDeckPresence(set, deck)
  }


  def hasMoreSets(deck: Set[Card]): Boolean = {
    deck.subsets(3) exists { set => validateEqualityRule(set.toSeq)}
  }
}

class Game(gameId: Long) extends Actor with ActorLogging {

  val BOARD_SIZE: Int = 16
  val SET_SIZE: Int = 3
  val SECONDS: Int = 120
  val MAX_PLAYERS: Int = 5

  var players: Map[ActorRef, PlayerState] = Map()

  val starter = context.system.scheduler.scheduleOnce(30.seconds, self, "start")
  val tick = context.system.scheduler.schedule(1.seconds, 1.second, self, "tick")

  override def postStop() = {
    starter.cancel()
    tick.cancel()
  }

  override def receive: Receive = pending

  def pending: Receive = LoggingReceive {

    case "start" => startGame

    case "tick" =>
      publish(GameStatus(s"${players.size} players waiting to play"))

    case msg@JoinGameWithoutId(name) =>
      players += sender() -> PlayerState(name, 0, true)
      if (players.size >= MAX_PLAYERS){
        starter.cancel()
        startGame
      }

    case UserQuit =>
      log.debug("User quit")
      players.find(_._1 == sender).map {
        player =>
          players = players.updated(sender, players(sender).copy(connected = false))
          if (players.values.forall(!_.connected)) {
            self ! PoisonPill
          }
      }

    case msg =>
      log.debug(s"Unhandled message: $msg")
  }

  def startGame {
    val deck: Seq[Card] = Game.createDeck
    val state: GameStart = GameStart(deck.take(BOARD_SIZE).toSet, scoreCard, gameId)

    context.parent ! state
    publish(state)

    context.become(active(deck, SECONDS))
  }

  def active(deck: Seq[Card], timeLeft: Int): Receive = LoggingReceive {

    case "tick" =>
      if (timeLeft <= 0) {
        gameFinished
      }
      else {
        publish(GameStatus(s"Game will finish in: $timeLeft seconds"))
        context.become(active(deck, timeLeft - 1))
      }

    case Guess(set) =>

      log.debug(s"validate $set")
      if (Game.validate(set.toSeq, activeCards(deck))) {

        updateScore(sender)

        val deckUpdated: Seq[Card] = deck.diff(set.toSeq)

        val newCards : Set[Card] = deckUpdated.drop(BOARD_SIZE - SET_SIZE).take(SET_SIZE).toSet

        if (newCards.isEmpty) {
          publish(NoCardsLeft(set, scoreCard))
        }
        else {
          publish(SetCompleted(set, newCards, scoreCard))
        }

        if (!Game.hasMoreSets(activeCards(deckUpdated))) {
          gameFinished
        } else {
          context.become(active(deckUpdated, timeLeft))
        }
      }
      else {
        sender ! WrongGuess
      }

    case UserQuit =>
      log.debug("User quit")
      players.find(_._1 == sender).map {
        player =>
          players = players.updated(sender, players(sender).copy(connected = false))
          if (players.values.forall(!_.connected)) {
            self ! PoisonPill
          }
          else {
            players.filter(_._2.connected).keys.foreach(_ ! OtherUserQuit(Player(player._2.name)))
          }
      }
  }

  def gameFinished {
    context.parent ! GameFinished(scoreCard)
    publish(GameFinished(scoreCard))
    self ! PoisonPill
  }

  def updateScore(player: ActorRef): Unit =
    players = players.updated(player, players(player).copy(score = players(player).score + 1))

  def scoreCard: Map[Player, Int] = players.values.map(p => Player(p.name) -> p.score).toMap

  def publish(msg: Any): Unit = players.keys.foreach(_ ! msg)

  def activeCards(deck: Seq[Card]): Set[Card] = deck.take(BOARD_SIZE).toSet

  case class PlayerState(name: String, score: Int, connected: Boolean)

}
