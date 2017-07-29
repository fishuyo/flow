package com.fishuyo

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom

object Main extends js.JSApp {
  
  case class Contact(name: Var[String], email: Var[String])

  var ws:WebSocket = _

  val data = Vars.empty[Contact]

  def main(): Unit = {
    println("from scalajs Main hi!")
    connectWS()
    // dom.render(document.body, renderHeader)

    // val nameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
    // joinButton.onclick = { (event: MouseEvent) =>
    //   joinChat(nameField.value)
    //   event.preventDefault()
    // }
    // nameField.focus()
    // nameField.onkeypress = { (event: KeyboardEvent) =>
    //   if (event.keyCode == 13) {
    //     joinButton.click()
    //     event.preventDefault()
    //   }
    // }
  }

  @dom
  def render = {
    { renderHeader.bind }
    { renderMain.bind }
    { renderFooter.bind }
  }

  @dom
  def renderHeader = {
  <header class="blue-grey lighten-5">
    <ul id="slide-out" class="side-nav fixed blue-grey lighten-5">
      <li class="no-padding">
        <ul class="collapsible collapsible-accordion">
          <li>
            <a class="collapsible-header">Devices<i class="material-icons">arrow_drop_down</i><span class="badge right">{Devices.devices.length.bind.toString}</span></a>
            <div class="collapsible-body">
              <ul class="collapsible collapsible-accordion" data:data-collapsible="accordion">
                { Devices.renderDevices.bind }
              </ul>
            </div>
          </li>
        </ul>
      </li>

      <li class="no-padding">
        <ul class="collapsible collapsible-accordion">
          <li>
            <a class="collapsible-header">Apps<i class="material-icons">arrow_drop_down</i><span class="badge right">1</span></a>
            <div class="collapsible-body">
              <ul>
                <li><a href="#!">First<i class="material-icons right">add</i></a></li>
                <li><a href="#!">Second</a></li>
                <li><a href="#!">Third</a></li>
                <li>
                  <ul class="collapsible collapsible-accordion" data:data-collapsible="accordion">
                    <li>
                      <a class="collapsible-header">App 4<i class="material-icons">arrow_drop_down</i></a>
                      <div class="collapsible-body">
                        <ul>
                          <li><a href="#!">First</a></li>
                          <li><a href="#!">Second</a></li>
                          <li><a href="#!">Third</a></li>
                          <li><a href="#!">Fourth</a></li>
                        </ul>
                      </div>
                    </li>
                    <li>
                      <a class="collapsible-header">App 5<i class="material-icons">arrow_drop_down</i></a>
                      <div class="collapsible-body">
                        <ul>
                          <li><a href="#!">First</a></li>
                          <li><a href="#!">Second</a></li>
                          <li><a href="#!">Third</a></li>
                          <li><a href="#!">Fourth</a></li>
                        </ul>
                      </div>
                    </li>
                  </ul>
                </li>
              </ul>
            </div>
          </li>
        </ul>
      </li>
    </ul>

    <a href="#" data:data-activates="slide-out" class="button-collapse hide-on-large-only"><i class="material-icons">menu</i></a>
  </header>
  }

  @dom
  def renderMain = {

  }

  @dom
  def renderFooter = {

  }

  @JSExport
  def send(data:String){ ws.send(data) }
  
  def connectWS(): Unit = {
    ws = new WebSocket(getWebsocketUri())
    
    ws.onopen = { (event: Event) =>
      // playground.insertBefore(p("ws connection was successful!"), playground.firstChild)
      // sendButton.disabled = false

      // val messageField = dom.document.getElementById("message").asInstanceOf[HTMLInputElement]
      // messageField.focus()
      // messageField.onkeypress = { (event: KeyboardEvent) =>
      //   if (event.keyCode == 13) {
      //     sendButton.click()
      //     event.preventDefault()
      //   }
      // }
      // sendButton.onclick = { (event: Event) =>
      //   ws.send(messageField.value)
      //   messageField.value = ""
      //   messageField.focus()
      //   event.preventDefault()
      // }

      // ws.send("init")
      event
    }

    ws.onerror = { (event: ErrorEvent) =>
      // playground.insertBefore(p(s"Failed: code: ${event.colno}"), playground.firstChild)
      // joinButton.disabled = false
      // sendButton.disabled = true
    }

    ws.onmessage = { (event: MessageEvent) =>
      println(event.data.toString)
      // val wsMsg = read[Protocol.Message](event.data.toString)

      // wsMsg match {
      //   case Protocol.ChatMessage(sender, message) => writeToArea(s"$sender said: $message")
      //   case Protocol.Joined(member, _)            => writeToArea(s"$member joined!")
      //   case Protocol.Left(member, _)              => writeToArea(s"$member left!")
      // }
    }
    ws.onclose = { (event: Event) =>
      println("ws close")
      // playground.insertBefore(p("Connection to ws lost. You can try to rejoin manually."), playground.firstChild)
      // joinButton.disabled = false
      // sendButton.disabled = true
    }

    // def writeToArea(text: String): Unit = playground.insertBefore(p(text), playground.firstChild)
  }

  def getWebsocketUri(): String = {
    val wsProtocol = if (document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${document.location.host}/ws"
  }
}