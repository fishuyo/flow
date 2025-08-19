// package flow
// package server

// import org.apache.pekko.actor.ActorSystem
// import org.apache.pekko.http.scaladsl.server.Directives

// import org.webjars.WebJarAssetLocator
// import util.DirectivesWebJars._


// class Services(implicit val system:ActorSystem) extends Directives {

//   val flowService = new flow.service.FlowService()
//   val launcherService = new flow.service.LauncherService()

//   private val webJarAssets = new WebJarAssetLocator()

//   val server = {
//     pathSingleSlash {
//       getDirectoryFromWebjar(webJarAssets, "server")
//     } ~
//     pathPrefix("assets" / Remaining) { file =>
//       encodeResponse {
//         getFromWebjar(webJarAssets, "server", file)
//       }
//     }
//   }

//   val routes = {
//     server ~
//     flowService.route ~
//     launcherService.route
//   }


// }


