package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.LinkingInfo

import scala.scalajs.js.timers.setInterval

// import org.querki.jquery._

// import com.thoughtworks.binding.Binding, Binding._
// import org.lrng.binding.html, html._
// import slinky.core._
// import slinky.web.ReactDOM
import org.scalajs.dom

// import slinky.core._
// import slinky.web.ReactDOM
// import slinky.hot


import com.thoughtworks.binding.Binding, Binding._
// import org.lrng.binding.html, html.Binding, html.NodeBindingSeq
import com.yang_bo.html._

import org.scalajs.dom.raw._



@JSExportTopLevel("Main")
object Main {
  
  // var ws:dom.WebSocket = _

  @JSExport
  def main(args: Array[String]): Unit = {
    println("Hello from scalajs Main function.")

    // if (LinkingInfo.developmentMode) {
    //   hot.initialize()
    // }

    // val container = Option(dom.document.getElementById("root")).getOrElse {
    //   val elem = dom.document.createElement("div")
    //   elem.id = "root"
    //   dom.document.body.appendChild(elem)
    //   elem
    // }

    com.yang_bo.html.render(dom.document.body, render)

    CodeEditor.init("code")
    Socket.init()
    setInterval(1000){ Socket.send("keepalive") }


  }


  // @html def root: Binding[HTMLDivElement] = {
  //   val tags = Vars("initial-tag-1", "initial-tag-2")
  //   // render.value
  //   <div>
  //     <hr/>
  //     <h3>All tags:</h3>
  //     <ol>{ for (tag <- tags) yield <li>{ tag }</li> }</ol>
  //   </div>
  // }

  // @html
  // def renderHeader: Binding[HTMLDivElement] = {
  //   <div class="blue-grey lighten-5">
  //     <ul id="slide-out" class="side-nav fixed blue-grey lighten-5">
  //       <li class="no-padding">
  //         Devices
  //       </li>
  //       <li class="no-padding">
  //         Apps
  //       </li> 
  //       <li class="no-padding">
  //         Mappings
  //       </li>
  //     </ul>

  //     <a id="menu-button-left" href="#" data:data-activates="slide-out" class="button-collapse hide-on-large-only"><i class="material-icons">menu</i></a>
  //   </div>
  // }


  def render = html"""
    <div>
    <header>${ renderHeader }</header>
    <main>${ renderMain }</main>
    <footer>${ renderFooter }</footer>
    </div>
  """

  def renderHeader = html"""
    <div class="blue-grey lighten-5">
      <ul id="slide-out" class="sidenav sidenav-fixed blue-grey lighten-5">
        <li class="no-padding">
          ${ Devices.views.collapsibleList }
        </li>
        <li class="no-padding">
          ${ Apps.views.collapsibleList }
        </li> 
        <li class="no-padding">
          ${ Mappings.views.collapsibleList }
        </li>
      </ul>

      <a id="menu-button-left" href="#" data:data-target="slide-out" class="sidenav-trigger show-on-large"><i class="material-icons">menu</i></a>
    </div>
  """

  def renderMain = html"""
    <div class="blue-grey darken-4">
      <!-- <div class="fixed-action-btn click-to-toggle">
        <a class="btn-floating btn-large red">
          <i class="material-icons">menu</i>
        </a>
        <ul>
          <li><a class="btn-floating red"><i class="material-icons">insert_chart</i></a></li>
          <li><a class="btn-floating yellow darken-1"><i class="material-icons">format_quote</i></a></li>
          <li><a class="btn-floating green"><i class="material-icons">publish</i></a></li>
          <li><a class="btn-floating blue"><i class="material-icons">attach_file</i></a></li>
        </ul>
      </div> -->

      ${ CodeEditor.views.main }
      // <!--{ ConsoleWindow.views.main.bind }-->
    </div>
  """

  def renderFooter = html"""
    <div class="page-footer">
      <div class="container">
        <div class="row">
          <div class="col l6 s12">
          </div>
          <div class="col l4 offset-l2 s12">

          </div>
        </div>
      </div>
      <div class="footer-copyright">
        <div class="container">
        AlloSphere Device Server
        <a class="grey-text text-lighten-4 right" href="#!">More Services</a>
        </div>
      </div>
    </div> 
  """
  
}

