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
  PicklerRegistry.register[JoinGame]
  PicklerRegistry.register[CreateGame]

  def props(out : ActorRef) = Props(new UserActor(out))
}

//Handles pickling and unpickling
class UserActor(out : ActorRef) extends Actor {

  var currentGame : Option[ActorRef] = None

  def receive = {
    //from websocket
    case msg : JsValue => {
      val unpickled = PicklerRegistry.unpickle(msg)
      //pass it on
      unpickled match {
        case m @(JoinGame | CreateGame) => {
          //send it to the game master
          GameMaster.gameMaster ! m
        }
        case m : ClientSends => {
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
    }
    //from game logic
    case msg : Any => {
      val pickled : JsValue = PicklerRegistry.pickle(msg)
      out ! pickled
    }
  }

}
