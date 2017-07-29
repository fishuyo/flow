package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.streams._

import akka.actor._
import akka.stream._

import com.fishuyo.seer.hid._

import collection.mutable.HashMap
import collection.mutable.ListBuffer


@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, materializer: Materializer) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    val devices = DeviceManager.getRegisteredDevices //HashMap[String,ListBuffer[Device]]()
    // DeviceManager.shutdown
    Ok(views.html.index(DeviceManager.devices))
    // Ok(views.html.test())
  }


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      WebsocketActor.props(out)
    }
  }

}
