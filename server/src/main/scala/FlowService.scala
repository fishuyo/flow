package flow
package server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
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
    path("ws"){
      handleWebSocketMessages(wsFlow)
    } ~
    pathPrefix(Remaining) { file =>
      encodeResponse {
        getFromResource("public/" + file)
      }
    }
  }

   def wsFlow = {
    NamedActorFlow.actorRef(out =>
      WebsocketActor.props(out),
      maybeName = Some(s"client.${seer.math.Random.int()}")
    )
  }


}