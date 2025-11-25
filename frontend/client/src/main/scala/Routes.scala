package client

import com.raquo.laminar.api.L.{*, given}
import com.raquo.waypoint._
import upickle.default._
import org.scalajs.dom

sealed abstract class Page(val title: String, val path: String = "")
    derives ReadWriter
case object ComponentsTestPage extends Page("Components")
case object ProjectorRemote extends Page("Projector Remote")
case object Multishell extends Page("Multishell")

val routes = List(
  Route.static(ComponentsTestPage, root / endOfSegments),
  Route.static(ProjectorRemote, root / "projectors" / endOfSegments),
  Route.static(Multishell, root / "sh" / endOfSegments)
)

object Routes
    extends Router[Page](
      routes = routes,
      getPageTitle =
        page =>
          page.title, // (document title, displayed in the browser history, and in the tab next to favicon)
      serializePage =
        page =>
          write(page), // serialize page data for storage in History API log
      deserializePage = pageStr => read(pageStr) // deserialize the above
    ) {

  val splitter = SplitRender[Page, HtmlElement](currentPageSignal)
    .collectStatic(ComponentsTestPage) { pages.ComponentsTestPage() }
    .collectStatic(ProjectorRemote) { pages.ProjectorRemote() }
    .collectStatic(Multishell) { pages.Multishell() }

}
