package flow
package server

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.server.Directives
// import $package$.shared.SharedMessages
// import $package$.twirl.Implicits._

class FlowService(implicit val system:ActorSystem) extends Directives {

  System() = system
  OSCApi.listen(12000) 
  hid.DeviceManager.startPolling()

  val route = {
    pathSingleSlash {
      getFromResource("public/index.html")
    } ~
    path("wsProtocol"){
      handleWebSocketMessages(wsProtocolFlow)
    } ~
    path("wsIJS"){
      handleWebSocketMessages(wsIJSFlow)
    } ~
    pathPrefix(Remaining) { file =>
      encodeResponse {
        getFromResource("public/" + file)
      }
    }
  }

  def wsProtocolFlow = {
    NamedActorFlow.actorRef(out =>
      WebsocketActor.props(out),
      maybeName = Some(s"client.${seer.math.Random.int()}")
    )
  }
  def wsIJSFlow = {
    NamedActorFlow.actorRef(out =>
      flow.ijs.InterfaceWSActor.props(out),
      maybeName = Some(s"client.ijs.${seer.math.Random.int()}")
    )
  }


}