package models

import akka.actor.{Actor, ActorRef, Props}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import shared._

import scala.util.Random


private class GameMaster extends Actor {

  import shared.JoinGame

  var pendingGames: Set[ActorRef] = Set()

  override def receive: Receive = {

    case msg: JoinGameWithoutId =>
      val game: ActorRef = findOrCreateGame()
      game forward msg

    case msg@JoinGameWithId(playerName, gameId) =>
      context.children.find(_.path.name == s"game-$gameId").map(game => game forward msg).getOrElse(sender ! GameNotFound)

    case CreateGame(player) =>
      val gameId = nextId
      context.actorOf(Game.props(gameId), name = s"game-$gameId")
      sender ! GameCreated(gameId)

    case GameStart(_, _, _) =>
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

  def nextId: Long = Random.nextLong()

}

object GameMaster {
  val gameMaster = Akka.system.actorOf(Props[GameMaster])
}