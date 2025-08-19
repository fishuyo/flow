package flow

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.model.headers._
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration.Duration
import scala.io.StdIn
import java.io.File
import org.slf4j.LoggerFactory
import scala.util.{Success, Failure}

object Server {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "flow-server")
    implicit val executionContext: ExecutionContext = system.executionContext

    // CORS settings
    val corsHeaders = List(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.OPTIONS),
      `Access-Control-Allow-Headers`("Content-Type", "Authorization"),
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Max-Age`(1800)
    )

    // Serve static files from the client/dist directory (Vite build output)
    val staticFiles =
      pathPrefix("assets") {
        getFromDirectory("frontend/client/dist/assets")
      } ~
      path("favicon.ico") {
        getFromFile("frontend/client/dist/favicon.ico")
      }

    // Serve files from the root of client/dist (for Vite public dir files)
    val publicFiles =
      path(Remaining) { file =>
        val f = new File("frontend/client/dist", file)
        if (f.exists && f.isFile) getFromFile(f)
        else reject
      }

    // API routes (expand as needed)
    val apiRoutes =
      pathPrefix("api") {
        path("projectors") {
          get {
            complete("projectorAPI endpoint")
          }
        } ~
        complete("API endpoint")
      }

    // SPA fallback: serve index.html for all other GET requests
    val spaFallback =
      get {
        extractUnmatchedPath { _ =>
          getFromFile("frontend/client/dist/index.html")
        }
      }

    val route: Route =
      respondWithHeaders(corsHeaders) {
        apiRoutes ~ staticFiles ~ publicFiles ~ spaFallback
      }

    // Start the server
    val address = "0.0.0.0"
    val port = 8080
    val bindingFuture = Http().newServerAt(address, port).bind(route)
    
    bindingFuture.onComplete {
      case Success(binding) =>
        logger.info(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
        
        // Add shutdown hook for graceful termination
        sys.addShutdownHook {
          logger.info("Shutting down server...")
          binding.unbind().onComplete { _ =>
            logger.info("Server stopped")
            system.terminate()
          }
        }
        
      case Failure(ex) =>
        logger.error("Failed to bind server", ex)
        system.terminate()
    }

    logger.info(s"Starting server at http://$address:$port/")
    // Wait for the ActorSystem to terminate
    Await.result(system.whenTerminated, Duration.Inf)
  }
}