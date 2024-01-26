package flow
package server

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import com.typesafe.config.ConfigFactory

object WebServer {

  def main(args: Array[String]): Unit = {

    java.lang.System.setProperty("jna.nosys", "true")

    implicit val system = ActorSystem("server-system")

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")

    val service = new FlowService()

    Http().newServerAt(interface, port).bind(service.route)

    println(s"Server online at http://$interface:$port")
  }
}