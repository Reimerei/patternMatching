package models

import akka.actor._
import akka.event.LoggingReceive
import akka.persistence.PersistentActor
import play.api.Play.current
import play.api.libs.concurrent.Akka
import shared._

import scala.util.Random


private class GameMaster extends PersistentActor with ActorLogging {

  
  var userRating: Map[String, Int] = Map()
  
  var pendingGames: Set[ActorRef] = Set()

  override def persistenceId: String = "GameMaster"

  override def receiveCommand: Receive = LoggingReceive {

    case msg: JoinGameWithoutId =>
      log.debug(s"$msg")
      val game: ActorRef = findOrCreateGame()
      game forward msg

    case msg@JoinGameWithId(playerName, gameId) =>
      context.children.find(_.path.name == s"game-$gameId").map(game => game forward msg).getOrElse(sender ! GameNotFound)

    case CreateGame(player) =>
      val gameId = nextId
      context.actorOf(Game.props(gameId), name = s"game-$gameId")
      sender ! GameCreated(gameId)

    case msg: GameStart =>
      log.debug(s"Game started")
      pendingGames = pendingGames.filterNot(_ == sender)

    case msg @ GameFinished(score) => 
      persist(msg)(x => updateRating(score))

    case Terminated(child) =>
      log.debug("Game terminated")
      pendingGames = pendingGames.filterNot(_ == child)
  }


  def findOrCreateGame(): ActorRef = {
    if (pendingGames.isEmpty) {
      val gameId = nextId
      val game: ActorRef = context.actorOf(Game.props(gameId), name = s"game-$gameId")
      pendingGames += game
    }
    pendingGames.head
  }

  def nextId: Long = math.abs(Random.nextLong())

  override def receiveRecover: Receive = {
    case GameFinished(score) => updateRating(score)
  }

  def updateRating(score: Map[Player, Int]): Unit = {
    log.debug(s"Update rating")
  }
}

object GameMaster {
  val gameMaster = Akka.system.actorOf(Props[GameMaster])
}