package flow
package service

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives
// import $package$.shared.SharedMessages
// import $package$.twirl.Implicits._

import org.webjars.WebJarAssetLocator
import util.DirectivesWebJars._
import util.NamedActorFlow


class FlowService(implicit val system:ActorSystem) extends Directives {

  System() = system
  OSCApi.listen(12000) 
  hid.DeviceManager.startPolling()

  private val webJarAssets = new WebJarAssetLocator()

  val route = {
    pathPrefix("flow"){
      pathSingleSlash {
        getDirectoryFromWebjar(webJarAssets, "flow_service")
      } ~
      path("wsProtocol"){
        handleWebSocketMessages(wsProtocolFlow)
      } ~
      path("ui" / Segment / "ws"){ (name:String) =>
        handleWebSocketMessages(wsIJSFlow(name))
      } ~
      pathPrefix(Remaining) { file =>
        encodeResponse {
          getFromWebjar(webJarAssets, "flow_service", file)
        }
      }
    }
  }

  def wsProtocolFlow = {
    NamedActorFlow.actorRef(out =>
      WebsocketActor.props(out),
      maybeName = Some(s"client.${seer.math.Random.int()}")
    )
  }
  def wsIJSFlow(name:String) = {
    NamedActorFlow.actorRef(out =>
      flow.ijs.InterfaceWSActor.props(out, name),
      maybeName = Some(s"client.ijs.$name.${seer.math.Random.int()}")
    )
  }


}


