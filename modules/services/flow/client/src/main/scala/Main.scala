package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.LinkingInfo

import scala.scalajs.js.timers.setInterval

import org.scalajs.dom
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding, Binding._
import com.yang_bo.html._


import flow.client.WebApp



@JSExportTopLevel("Main")
object Main {
  
  // var ws:dom.WebSocket = _

  @JSExport
  def main(args: Array[String]): Unit = {
    // println("Hello from scalajs Main function.")

    // if (LinkingInfo.developmentMode) {
    //   hot.initialize()
    // }

    // val container = Option(dom.document.getElementById("root")).getOrElse {
    //   val elem = dom.document.createElement("div")
    //   elem.id = "root"
    //   dom.document.body.appendChild(elem)
    //   elem
    // }

    com.yang_bo.html.render(dom.document.body, WebApp.render)

    CodeEditor.init("code")
    Socket.init()
    setInterval(1000){ Socket.send("keepalive") }

  }



}

