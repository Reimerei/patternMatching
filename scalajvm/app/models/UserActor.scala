package models

import akka.actor.{Props, ActorRef, Actor}
import play.api.libs.json.JsValue
import shared._

object UserActor {
  def props(out : ActorRef) = Props(new UserActor(out))
}

//Handles pickling and unpickling
class UserActor(out : ActorRef) extends Actor {

  def receive = {
    //from websocket
    case msg : JsValue => {
      val unpickled = ??? //TODO
      //pass it on
    }
    //from game logic
    case msg : Any => {
      //TODO: handle pickling error?
      val pickled : JsValue = ???
      out ! pickled
    }
  }

}
