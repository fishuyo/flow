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
import com.typesafe.config.ConfigFactory

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

    // Get the project root directory (more reliable than relative paths)
    val config = ConfigFactory.load()
    val projectRoot = config.getString("project.root")
    val distPath = s"$projectRoot/frontend/client/dist"
    
    // logger.info(s"Project root: $projectRoot")
    // logger.info(s"Dist path: $distPath")
    
    // Check if dist directory exists
    val distDir = new File(distPath)
    if (!distDir.exists()) {
      logger.warn(s"Dist directory does not exist: $distPath")
    }
    
    // Serve static files from the client/dist directory (Vite build output)
    val staticFiles =
      pathPrefix("assets") {
        getFromDirectory(s"$distPath/assets")
      } ~
      path("favicon.ico") {
        getFromFile(s"$distPath/favicon.ico")
      }

    // Serve files from the root of client/dist (for Vite public dir files)
    val publicFiles =
      path(Remaining) { file =>
        val f = new File(distPath, file)
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
      } ~
      path("health") {
        get {
          complete(s"Server is running. Project root: $projectRoot, Dist path: $distPath")
        }
      }

    // SPA fallback: serve index.html for all other GET requests
    val spaFallback =
      get {
        extractUnmatchedPath { _ =>
          getFromFile(s"$distPath/index.html")
        }
      }

    val route: Route =
      respondWithHeaders(corsHeaders) {
        apiRoutes ~ staticFiles ~ publicFiles ~ spaFallback
      }

    // Start the server
    val address = config.getString("http.interface")
    val port = config.getInt("http.port")
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