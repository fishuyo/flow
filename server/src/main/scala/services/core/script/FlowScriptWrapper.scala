
package flow
package script

object FlowScriptWrapper {

  val header = """
import org.apache.pekko.actor._
import org.apache.pekko.stream._
import org.apache.pekko.stream.scaladsl._

import scala.concurrent.duration._
import scala.math._

import seer.math._

import flow._
import flow.ijs._
import flow.script._


class FlowScript extends Script {

  implicit val system:ActorSystem = System()
  implicit val materializer:ActorMaterializer = ActorMaterializer()

  implicit def source2io[T,M](src:Source[T,M]):IOSource[T,M] = IOSource(src)
  implicit val kill:SharedKillSwitch = KillSwitches.shared("script")

  implicit def d2f(d:Double):Float = d.toFloat

  val Print = Sink.foreach(println(_:Any))
  val Null = Sink.foreach(nop(_:Any))

  def nop(v:Any):Unit = {}

  override def load() = {
"""

  val footer = """
  }
  override def unload() = {
    kill.shutdown()
  }
}
new FlowScript
"""
  
  def apply(code:String) = header + code + footer

  def headerLength = header.count(_ == '\n')
  def transformLineNumber(line:Int) = line - headerLength - 1

}