package flow

import akka.actor._

object System {
  var system:ActorSystem = _
  def apply() = system
  def update(s:ActorSystem) = system = s
}