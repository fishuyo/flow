package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.timers.setInterval
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import org.querki.jquery._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom


object Main extends js.JSApp {
  
  var ws:WebSocket = _

  def main(): Unit = {
    println("from scalajs Main hi!")
    
    dom.render(document.body, render)

    CodeEditor.init("code")
    Socket.init()
    setInterval(1000){ Socket.send("keepalive") }


  }

  @JSExport
  def send(data:String){ ws.send(data) }
  
  @dom
  def render = {
    <header>{ renderHeader.bind }</header>
    <main>{ renderMain.bind }</main>
    <footer>{ renderFooter.bind }</footer>
  }

  @dom
  def renderHeader = {
    <div class="blue-grey lighten-5">
      <ul id="slide-out" class="side-nav fixed blue-grey lighten-5">
        <li class="no-padding">
          { Devices.views.collapsibleList.bind }
        </li> 
        <li class="no-padding">
          { Mappings.views.collapsibleList.bind }
        </li>
      </ul>

      <a id="menu-button-left" href="#" data:data-activates="slide-out" class="button-collapse hide-on-large-only"><i class="material-icons">menu</i></a>
    </div>
  }

  @dom
  def renderMain = {
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

      { CodeEditor.views.main.bind }
      { ConsoleWindow.views.main.bind }
    </div>
  }

  @dom
  def renderFooter = {
    <div class="page-footer">
      <div class="container">
        <div class="row">
          <div class="col l6 s12">
            <!-- <h5 class="white-text">Footer Content</h5> -->
            <!-- <p class="grey-text text-lighten-4">You can use rows and columns here to organize your footer content.</p> -->
          </div>
          <div class="col l4 offset-l2 s12">
  <!--                 <h5 class="white-text">Links</h5>
            <ul>
              <li><a class="grey-text text-lighten-3" href="#!">Link 1</a></li>
              <li><a class="grey-text text-lighten-3" href="#!">Link 2</a></li>
              <li><a class="grey-text text-lighten-3" href="#!">Link 3</a></li>
              <li><a class="grey-text text-lighten-3" href="#!">Link 4</a></li>
            </ul> -->
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
  }


  
}