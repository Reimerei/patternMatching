package models

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.event.LoggingReceive
import play.api.Play.current
import play.api.libs.concurrent.Akka
import shared._

import scala.util.Random


private class GameMaster extends Actor with ActorLogging {

  var pendingGames: Set[ActorRef] = Set()

  override def receive: Receive = LoggingReceive {

    case msg: JoinGameWithoutId =>
      log.debug(s"$msg")
      val game: ActorRef = findOrCreateGame()
      log.debug(s"$game")
      game forward msg

    case msg@JoinGameWithId(playerName, gameId) =>
      context.children.find(_.path.name == s"game-$gameId").map(game => game forward msg).getOrElse(sender ! GameNotFound)

    case CreateGame(player) =>
      val gameId = nextId
      context.actorOf(Game.props(gameId), name = s"game-$gameId")
      sender ! GameCreated(gameId)

    case msg: GameStart =>
      log.debug(s"$msg")
      pendingGames = pendingGames.filterNot(_ == sender)
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

}

object GameMaster {
  val gameMaster = Akka.system.actorOf(Props[GameMaster])
}