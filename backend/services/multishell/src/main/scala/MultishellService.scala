package multishell.service

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives
import org.apache.pekko.stream.Materializer
import flow.util.NamedActorFlow
import scala.concurrent.ExecutionContext
import seer.math.Random

class MultishellService(implicit val system: ActorSystem, val ec: ExecutionContext, val mat: Materializer) extends Directives {

  println("[MultishellService] Service created")

  val route = {
    pathPrefix("multishell") {
      path("ws") {
        extractRequest { request =>
          println(s"[MultishellService] WebSocket connection request received at: ${request.uri}")
          handleWebSocketMessages(wsFlow)
        }
      } ~
      path(Remaining) { remaining =>
        println(s"[MultishellService] Unmatched multishell path: $remaining")
        complete(s"Multishell endpoint - unmatched path: $remaining")
      }
    }
  }

  def wsFlow = {
    println("[MultishellService] Creating WebSocket flow")
    NamedActorFlow.actorRef(
      out => {
        println("[MultishellService] Creating MultishellWSActor")
        MultishellWSActor.props(out)
      },
      maybeName = None  // Don't use a name to avoid top-level actor creation issues
    )
  }
}

