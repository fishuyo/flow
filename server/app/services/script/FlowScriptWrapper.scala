
package flow
package script


object FlowScriptWrapper {

  def apply(code:String) = {

    s"""
      import akka.actor._
      import akka.stream._
      import akka.stream.scaladsl._

      import flow._
      import flow.hid._
      import flow.ijs._
      import flow.script._

      class FlowScript extends Script {

        implicit val system = System()
        implicit val materializer = ActorMaterializer()

        implicit def source2io[T,M](src:Source[T,M]) = IOSource(src)
        implicit val kill = KillSwitches.shared("hi")

        val Print = Sink.foreach(println(_:Any))

        $code

        override def unload(){
          kill.shutdown
        }
      }
      new FlowScript
    """

  }

}