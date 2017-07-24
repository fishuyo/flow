package controllers

import akka.actor._

object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))
}

class WebsocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}