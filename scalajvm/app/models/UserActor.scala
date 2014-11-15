package models

import akka.actor.{Props, ActorRef, Actor}
import play.api.Logger
import play.api.libs.json.{JsValue}
import org.scalajs.spickling._
import shared._
import org.scalajs.spickling.playjson._

object UserActor {
  PicklerRegistry.register[GameStart]
  PicklerRegistry.register[SetCompleted]
  PicklerRegistry.register(WrongGuess)
  PicklerRegistry.register[GameFinished]
  PicklerRegistry.register[Guess]
  PicklerRegistry.register[JoinGameWithoutId]
  PicklerRegistry.register[JoinGameWithId]
  PicklerRegistry.register[CreateGame]
  PicklerRegistry.register[GameCreated]
  PicklerRegistry.register(GameNotFound)

  def props(out : ActorRef) = Props(new UserActor(out))
}

//Handles pickling and unpickling
class UserActor(out : ActorRef) extends Actor {

  var currentGame : Option[ActorRef] = None

  def receive = {
    //from websocket
    case msg: JsValue =>
      val unpickled = PicklerRegistry.unpickle(msg)
      //pass it on
      unpickled match {
        case m : JoinGameWithId =>
          GameMaster.gameMaster ! m
        case m : JoinGameWithoutId =>
          GameMaster.gameMaster ! m
        case m : CreateGame =>
          GameMaster.gameMaster ! m
        case m: ClientSends => {
          //send it to the game
          currentGame match {
            case Some(g) => g ! m
            case None => Logger.info("Socket without a game received a game message")
          }
        }
      }

      //temporary
      val test = SetCompleted
      self ! test

      Logger.debug(s"Received message: $msg")

    case msg: ServerSend =>
      Logger.debug(s"Sent message: $msg")
      msg match {
        case _ : GameStart =>
          currentGame = Some(sender())
        case _ : GameFinished =>
          currentGame = None
      }
      val pickled: JsValue = PicklerRegistry.pickle(msg)
      out ! pickled


  }
}
