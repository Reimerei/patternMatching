package models

import akka.actor.{Props, Actor, ActorRef}
import play.api.libs.concurrent.Akka
import shared.{GameStart, CreateGame}
import play.api.Play.current
import scala.util.Random


private class GameMaster extends Actor {

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

object GameMaster {
  val gameMaster = Akka.system.actorOf(Props[GameMaster])
}