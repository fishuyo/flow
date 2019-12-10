
package flow

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import scala.io.StdIn

object FlowServer {
  def main(args: Array[String]) {
    kamon.Kamon.init()

    implicit val system = ActorSystem("flow")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")
    val oscport = config.getInt("osc.port")

    val service = new FlowService()

    System() = system
    OSCApi.listen(oscport) 
    hid.DeviceManager.startPolling()  

    val bindingFuture = Http().bindAndHandle(service.route, interface, port)

    println(Console.GREEN + s"\nServer online at http://$interface:$port/\nPress RETURN to stop..\n")
    println(Console.RESET)

    StdIn.readLine() // let it run until user presses return
    
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

    // java.lang.System.exit(0)
  }
}