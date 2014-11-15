package models

import akka.actor.{Props, ActorRef, Actor}

object UserActor {
  def props(out : ActorRef) = Props(new UserActor(out))
}

class UserActor(out : ActorRef) extends Actor {

  def receive = {
    case msg : String => ???
  }

}
