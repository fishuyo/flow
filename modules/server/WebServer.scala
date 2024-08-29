package flow
package server

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import com.typesafe.config.ConfigFactory

object WebServer {

  def main(args: Array[String]): Unit = {

    // avoid system jna 
    java.lang.System.setProperty("jna.nosys", "true")

    implicit val system = ActorSystem("server-system")

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")

    val services = new Services()
    Http().newServerAt(interface, port).bind(services.routes)

    println(s"Server online at http://$interface:$port")
  }
}