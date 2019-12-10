package flow

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.duration._

class FlowService(implicit val system:ActorSystem) extends Directives {

  implicit val materializer = ActorMaterializer()

  val route = {
    pathSingleSlash {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, index))
    } ~
    path("ws"){
      handleWebSocketMessages(
        NamedActorFlow.actorRef(
          out => WebsocketActor.props(out),
          maybeName = Some(s"client.${com.fishuyo.seer.util.Random.int()}")
        )
      )
    } ~
    path("ijs" / Segment){ name =>
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, ijs.Interface.template))
    } ~
    path("ijs" / Segment / "ws"){ name =>
      extractClientIP { ip => 
        handleWebSocketMessages(
          NamedActorFlow.actorRef(
            out => ijs.InterfaceWSActor.props(out, name, ip.value),
            maybeName = Some(s"ijs.${com.fishuyo.seer.util.Random.int()}")
          )
        )
      } 
    } ~
    pathPrefix("assets" / Remaining) { file =>
        // optionally compresses the response with Gzip or Deflate
        // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    }

  }


  val index = s"""
<!DOCTYPE html>
<html lang="en">
  <head>
    <title>Flow</title>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link type="text/css" rel="stylesheet" href="/assets/lib/materializecss/css/materialize.css"  media="screen,projection"/>

    <link rel="stylesheet" media="screen" href="/assets/lib/codemirror/lib/codemirror.css">
    <link rel="stylesheet" media="screen" href="/assets/lib/codemirror/theme/solarized.css">
    <link rel="stylesheet" media="screen" href="/assets/lib/codemirror/theme/bespin.css">
    <link rel="stylesheet" media="screen" href="/assets/lib/codemirror/theme/ambiance.css">
    <link rel="stylesheet" media="screen" href="/assets/lib/codemirror/theme/material.css">
    <link rel="stylesheet" media="screen" href="/assets/lib/codemirror/theme/monokai.css">

    <link rel="stylesheet" media="screen" href="/assets/stylesheets/main.css">
    <link rel="shortcut icon" type="image/png" href="/assets/images/favicon.png">

    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  </head>
  <body class="blue-grey darken-4">
    <p>...</p>

      ${scalajs.html.scripts("client", name => s"/assets/$name", name => getClass.getResource(s"/public/$name") != null).body}
    <!-- <script src="/assets/lib/cm/mode/clike/clike.js" type="text/javascript"></script> -->
    <!-- <script src="/assets/lib/cm/keymap/sublime.js" type="text/javascript"></script> -->

    <script src="/assets/js/main.js" type="text/javascript"></script>

    <!-- <script src="@routes.Assets.versioned("lib/cm/lib/codemirror.js")" type="text/javascript"></script> -->
    <!-- <script src="@routes.Assets.versioned("lib/cm/mode/clike/clike.js")" type="text/javascript"></script> -->
    <!-- <script src="@routes.Assets.versioned("lib/cm/keymap/sublime.js")" type="text/javascript"></script> -->

    <!-- <script src="@routes.Assets.versioned("js/main.js")" type="text/javascript"></script> -->

  </body>
</html>

  """
}