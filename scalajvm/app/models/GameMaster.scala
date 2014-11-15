package models

import akka.actor.{Actor, ActorRef}
import shared.{GameStart, CreateGame}

import scala.util.Random


class GameMaster extends Actor {

  import shared.JoinGame

  var pendingGames: Set[ActorRef] = Set()

  override def receive: Receive = {

    case msg: JoinGame =>
      val game: ActorRef = findOrCreateGame()
      game forward msg

    case JoinGame(playerName, Some(gameId)) =>
    case CreateGame(player) =>

    case GameStart(_, _, _) =>
      pendingGames = pendingGames.filterNot(_ == sender)
  }


  def findOrCreateGame(): ActorRef = {
    if (pendingGames.isEmpty) {
      val gameId: Long = Random.nextLong()
      val game: ActorRef = context.actorOf(Game.props(gameId), name = s"game-$gameId")
      pendingGames += game
    }
    pendingGames.head
  }

}
