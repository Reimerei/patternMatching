package models

import akka.actor.{Props, ActorRef, Actor}
import pickles.Pickles
import play.api.Logger
import play.api.libs.json.{JsValue}
import org.scalajs.spickling._
import shared._
import org.scalajs.spickling.playjson._

import scala.collection.immutable.HashMap.HashTrieMap
import scala.collection.immutable.Map.{Map4, Map3, Map2, Map1}

object UserActor {

  //hacky initialisation
  Pickles.register

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
        case _ => Logger.warn(s"Unknown message $msg")
      }

      Logger.debug(s"Received message: $msg")

    case msg: ServerSend =>
      msg match {
        case GameStart(_, _, _) =>
          currentGame = Some(sender())
        case GameFinished(_) =>
          currentGame = None
        case _ =>
      }
      val pickled: JsValue = PicklerRegistry.pickle(msg)
      out ! pickled

      Logger.debug(s"Sent message: $msg")

  }
}
