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

  def receive = {
    //from websocket
    case msg : JsValue => {
      //val unpickled = PicklerRegistry.unpickle(msg) //TODO
      //pass it on
      Logger.debug(s"Received message: $msg")
    }
    //from game logic
    case msg : Any => { //TODO: Any -> ServerSend
      //TODO: handle pickling error?
      val pickled : JsValue = PicklerRegistry.pickle(msg)
      out ! pickled
    }
  }

}
