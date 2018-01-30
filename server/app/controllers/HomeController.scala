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
      maybeName = Some(s"client.${com.fishuyo.seer.util.Random.int()}")
    )
  }

  def ijsSocket(name:String) = WebSocket.accept[String, String] { request =>
    NamedActorFlow.actorRef(out =>
      ijs.InterfaceWSActor.props(out, name, request.remoteAddress),
      maybeName = Some(s"ijs.${com.fishuyo.seer.util.Random.int()}")
    )
  }
  // def ijsSocket(name:String) = WebSocket.acceptOrResult[String, String] { request =>
  //   println(request)
  //   Future.successful( AppConfig.apps.find( _.name == name ) match {
  //     case None => Left(NotFound)
  //     case Some(app) => Right(ActorFlow.actorRef(out => OSCActor.props(out, oscManager, app.oscConfig,request.remoteAddress )))
  //   })
  // }

}
