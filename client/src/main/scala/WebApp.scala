
package flow
package client

import com.thoughtworks.binding.Binding, Binding._
import com.thoughtworks.binding.LatestEvent
import com.yang_bo.html.*

import org.scalajs.dom.*

import flow.client.components._

import collection.mutable.HashMap


trait Page {
  val name = "page"
  def route = s"#$name"
  def render:BindingSeq[Node]
  def onLoad() = {}
  def onUnload() = {}
}

object page404 extends Page {
  override val name = "404"
  def render = html"""
    <div>Page not found</div>
  """
}

object WebApp {

  val pages = HashMap[String, Page]()
  var lastPage:Page = page404


  val route = Binding {
    LatestEvent.hashchange(window).bind
    // println(window.location.hash)
    window.location.hash
  }

  val currentPage = Binding {

    val path = route.bind
    val page = pages.getOrElse(path, MainPage)

    if(page != lastPage){
      lastPage.onUnload()
      lastPage = page 
      page.onLoad()
    }
    page
  }


  def addPage(p:Page) = {

    // pages(s"#${p.name}") = p
    pages(p.route) = p
  }



  def render = html"""
    ${renderHeader}
    ${renderMain}
    ${renderFooter}
  """


  def renderHeader = html"""
    <header>

    </header>
  """


  def renderMain = html"""
    <main>
    ${currentPage.bind.render}
    </main>
  """

  def renderFooter = html"""
    <footer>
    </footer>
  """
}


