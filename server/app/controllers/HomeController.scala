package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.streams._

import akka.actor._
import akka.stream._

import flow._
import hid._

import collection.mutable.HashMap
import collection.mutable.ListBuffer

//
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, materializer: Materializer) extends AbstractController(cc) {

  // Initialize services here, because I don't want to change everything to use play's DI
  // works since HomeController a Singleton
  // Hack.unsafeAddDir("lib")
  // Hack.unsafeAddDir("../lib")
  System() = system
  OSCApi.listen(12000) 
  DeviceManager.startPolling()



  def index() = Action { implicit request: Request[AnyContent] =>
    // val devices = DeviceManager.getRegisteredDevices //HashMap[String,ListBuffer[Device]]()
    // DeviceManager.shutdown //XXX
    // Ok(views.html.index(DeviceManager.devices))
    Ok(views.html.test())
  }

  def socket = WebSocket.accept[String, String] { request =>
    NamedActorFlow.actorRef(out =>
      WebsocketActor.props(out),
      maybeName = Some(s"client.${seer.math.Random.int()}")
    )
  }

  def ijsSocket(name:String) = WebSocket.accept[String, String] { request =>
    NamedActorFlow.actorRef(out =>
      ijs.InterfaceWSActor.props(out, name, request.remoteAddress),
      maybeName = Some(s"ijs.${seer.math.Random.int()}")
    )
  }

  def ijsSocket2(name:String) = WebSocket.accept[String, String] { request =>
    NamedActorFlow.actorRef(out =>
      ijs.InterfaceWSActor.props(out, name, request.remoteAddress),
      maybeName = Some(s"ijs.${seer.math.Random.int()}")
    )
  }

  def ijsTemplate(name:String) = Action { implicit request: Request[AnyContent] =>
    val template = """
<html>
<head>
  <script src="/assets/js/interface.js"></script>
  <script src="/assets/js/interface.client.js"></script>
</head>
<body>
  <script>
    panel = new Interface.Panel({ useRelativeSizesAndPositions:true })
    panel.background = 'black'

    Interface.OSC.receive = function( address, typetags, parameters ) {
      console.log( address, typetags, parameters );
      if(address == "/_eval"){
        eval(parameters[0])
      }
    }
  </script>
</body>
</html>
    """
    Ok(play.twirl.api.Html(template))
  }

}
