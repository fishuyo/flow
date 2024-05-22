package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.LinkingInfo

import scala.scalajs.js.timers.setInterval

import org.scalajs.dom
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding, Binding._
import com.yang_bo.html._


import flow.client._

@JSExportTopLevel("Main")
object Main {
  
  @JSExport
  def main(args: Array[String]): Unit = {

    // WebApp.addPage(MainPage)
    WebApp.addPage(OldPage)

    com.yang_bo.html.render(dom.document.body, WebApp.render)

    Socket.init()
    setInterval(1000){ Socket.send("keepalive") }

  }

}

