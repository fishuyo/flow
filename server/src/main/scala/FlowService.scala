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

  val publicPath = Config("publicPath")

  val route = {
    pathSingleSlash {
      getFromResource("public/index.html")
    } ~
    path("wsProtocol"){
      handleWebSocketMessages(wsProtocolFlow)
    } ~
    path("ui" / Segment / "ws"){ (name:String) =>
      handleWebSocketMessages(wsIJSFlow(name))
    } ~
    // pathPrefix(Remaining) { file =>
    //   encodeResponse {
    //     getFromResource("public/" + file)
    //   }
    // } ~
    pathPrefix(Remaining) { file =>
      encodeResponse {
        getFromFile(publicPath + "/" + file)
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