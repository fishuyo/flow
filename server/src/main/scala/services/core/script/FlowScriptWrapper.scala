
package flow
package script

object FlowScriptWrapper {

  val header = """
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.math._

import seer.math._

import flow._
import flow.ijs._
import flow.script._


class FlowScript extends Script {

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  implicit def source2io[T,M](src:Source[T,M]) = IOSource(src)
  implicit val kill = KillSwitches.shared("script")

  implicit def d2f(d:Double) = d.toFloat

  val Print = Sink.foreach(println(_:Any))

  override def load(){
"""

  val footer = """
  }
  override def unload(){
    kill.shutdown
  }
}
new FlowScript
"""
  
  def apply(code:String) = header + code + footer

  def headerLength = header.count(_ == '\n')
  def transformLineNumber(line:Int) = line - headerLength - 1

}