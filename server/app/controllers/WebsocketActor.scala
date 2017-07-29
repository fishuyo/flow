package controllers

import akka.actor._


import com.fishuyo.seer.interface._
import com.fishuyo.seer.dynamic._
import com.fishuyo.seer.dynamic.ScriptLoaderActor._

object WebsocketActor {
  def props(out: ActorRef) = Props(new WebsocketActor(out))
}

class WebsocketActor(out: ActorRef) extends Actor {

  val script = ScriptManager()

  def receive = {
    // case msg:String if msg == "init" =>
    case msg:String if msg == "keepalive" => ()
    case msg:String => script ! Code(FlowScriptWrapper(msg)); script ! Reload
    // case msg => out ! ("I received your message: " + msg)
  }
}