package models

import akka.actor.{Props, ActorRef, Actor}
import play.api.libs.json.JsValue

object UserActor {
  def props(out : ActorRef) = Props(new UserActor(out))
}

class UserActor(out : ActorRef) extends Actor {

  def receive = {
    //from websocket
    case msg : JsValue => {
      //TODO: unpickle
    }
  }

}
