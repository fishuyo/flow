
// adapted from https://github.com/meshelton/shue

package flow

//import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, RequestEntity, ResponseEntity}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.{ExecutionContext, Future}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Printer

object Hue {
  import HueProtocol._

  implicit val system = System()
  implicit val materializer = ActorMaterializer()
  implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  // hard coding hue bridge info for now.
  // flowServer --> "JSZ6EN1Zp599wZ81IHtttzpv3vJSgUX-cZlC5EMI"
  var ip = "192.168.1.124"
  var id = "JSZ6EN1Zp599wZ81IHtttzpv3vJSgUX-cZlC5EMI"

  val url = s"http://${ip}/api/$id"

  def apiRequest[T](request: HttpRequest)(implicit um: Unmarshaller[ResponseEntity, T], ec: ExecutionContext = null, mat: Materializer): Future[T] = {
    Http().singleRequest(request).flatMap(resp ⇒ Unmarshal(resp.entity).to[T])
  }
  
  import scala.concurrent.ExecutionContext.Implicits.global

  def getLights: Future[Map[String, Light]] = apiRequest[Map[String,Light]](HttpRequest(uri = s"$url/lights"))
  def getLight(id: Int): Future[Light] = apiRequest[Light](HttpRequest(uri = s"$url/lights/$id"))

  def setLightState(id:Int, lightState:SetLightState): Future[Map[String, Map[String, String]]] = Marshal(lightState)
      .to[RequestEntity]
      .flatMap(e ⇒ apiRequest[Map[String, Map[String, String]]](HttpRequest(uri = s"$url/lights/$id/state", method = PUT, entity = e)))

}