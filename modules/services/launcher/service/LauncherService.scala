package flow
package service

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives

import org.webjars.WebJarAssetLocator
import util.DirectivesWebJars._
import util.NamedActorFlow


class LauncherService(implicit val system:ActorSystem) extends Directives {

  private val webJarAssets = new WebJarAssetLocator()

  val route = {
    pathPrefix("launcher"){
      pathSingleSlash {
        getDirectoryFromWebjar(webJarAssets, "launcher_service")
      } ~
      // path("wsProtocol"){
      //   handleWebSocketMessages(wsProtocolFlow)
      // } ~
      pathPrefix(Remaining) { file =>
        encodeResponse {
          getFromWebjar(webJarAssets, "launcher_service", file)
        }
      }
    }
  }

  // def wsProtocolFlow = {
  //   NamedActorFlow.actorRef(out =>
  //     WebsocketActor.props(out),
  //     maybeName = Some(s"client.${seer.math.Random.int()}")
  //   )
  // }


}


