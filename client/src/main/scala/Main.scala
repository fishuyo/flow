package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.timers.setInterval

// import org.querki.jquery._

import com.thoughtworks.binding.Binding, Binding._
import org.lrng.binding.html, html._
// import slinky.core._
// import slinky.web.ReactDOM
import org.scalajs.dom._

@JSExportTopLevel("Main")
object Main {
  
  var ws:WebSocket = _

  @JSExport
  def main(args: Array[String]): Unit = {
    println("from scalajs Main hi!")

    // val container = Option(document.getElementById("root")).getOrElse {
    //   val elem = document.createElement("div")
    //   elem.id = "root"
    //   document.body.appendChild(elem)
    //   elem
    // }

    // ReactDOM.render(Home(), container)
    
    
    @html val mySpan = <span>{"hi"}</span>
    println(mySpan)

    // html.render(document.body, render())

    // CodeEditor.init("code")
    Socket.init()
    setInterval(1000){ Socket.send("keepalive") }

  }

  @JSExport
  def send(data:String){ ws.send(data) }
  
  // @html def render(): NodeBinding[HTMLDivElement] = {
  //   // <main> {"hello world" }</main>
  //   // <header>{ renderHeader.bind }</header>
  //   // <main>{ renderMain.bind }</main>
  //   // <footer>{ renderFooter.bind }</footer>
  //   <div>{"hi"}</div>
  // }

  // @html
  // def renderHeader = {
  //   <div class="blue-grey lighten-5">
  //     <ul id="slide-out" class="side-nav fixed blue-grey lighten-5">
  //       <li class="no-padding">
  //         { Devices.views.collapsibleList.bind }
  //       </li>
  //       <li class="no-padding">
  //         { Apps.views.collapsibleList.bind }
  //       </li> 
  //       <li class="no-padding">
  //         { Mappings.views.collapsibleList.bind }
  //       </li>
  //     </ul>

  //     <a id="menu-button-left" href="#" data:data-activates="slide-out" class="button-collapse hide-on-large-only"><i class="material-icons">menu</i></a>
  //   </div>
  // }

  // @html
  // def renderMain = {
  //   <div class="blue-grey darken-4">
  //     // <!-- <div class="fixed-action-btn click-to-toggle">
  //       <a class="btn-floating btn-large red">
  //         <i class="material-icons">menu</i>
  //       </a>
  //       <ul>
  //         <li><a class="btn-floating red"><i class="material-icons">insert_chart</i></a></li>
  //         <li><a class="btn-floating yellow darken-1"><i class="material-icons">format_quote</i></a></li>
  //         <li><a class="btn-floating green"><i class="material-icons">publish</i></a></li>
  //         <li><a class="btn-floating blue"><i class="material-icons">attach_file</i></a></li>
  //       </ul>
  //     </div> -->

  //     { CodeEditor.views.main.bind }
  //     // <!--{ ConsoleWindow.views.main.bind }-->
  //   </div>
  // }

  // @html def renderFooter = {
  //   <div class="page-footer">
  //     <div class="container">
  //       <div class="row">
  //         <div class="col l6 s12">
  //         </div>
  //         <div class="col l4 offset-l2 s12">

  //         </div>
  //       </div>
  //     </div>
  //     <div class="footer-copyright">
  //       <div class="container">
  //       AlloSphere Device Server
  //       <a class="grey-text text-lighten-4 right" href="#!">More Services</a>
  //       </div>
  //     </div>
  //   </div> 
  // }


  
}



// import slinky.core.annotations.react
// import slinky.web.html._
// import slinky.core.facade.Hooks._


// @react object Home {
//   case class Props()

//   val component = FunctionalComponent[Props] { props =>

//     div(
//       "hi"
//     )


//   }
// }
