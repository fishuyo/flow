package flow

import flow.protocol.Device
import flow.protocol.IOPort

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
// import com.thoughtworks.binding.dom
// import org.lrng.binding.html, html.NodeBinding
import com.yang_bo.html._

import org.scalajs.dom.raw._

/* Devices Views */
// TODO add different categories?
// i.e.
// 1. Connected Devices (ready to use)
// 2. Implemented Devices
// 2. Unknown Devices (unimplemented)

object Devices {

  val devices = Vars.empty[Device]

  def apply() = devices

  def set(ds:Seq[Device]) = {
    devices.value.clear
    devices.value ++= ds
  }

  object views {

    def collapsibleList = html"""
      <ul class="collapsible expandable">
        <li>
          <a class="collapsible-header">
            Devices
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> ${ devices.length.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible expandable">
              ${ deviceList }
            </ul>
          </div>
        </li>
      </ul>
    """
    
    def deviceList = 
      for(ds <- devices) yield ds match {
          // case Nil => <!-- empty -->
          // case d :: Nil =>
          case Device(io, 1) =>
            html"""<li>
              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="truncate">${io.name}</span>
              </a>
              <div class="collapsible-body">
                <ul>${ device(ds) }</ul>
              </div>
            </li>"""
          case Device(io, count) =>
          // case d :: xs =>
            html"""<li>
              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="badge">${count.toString}</span>
                <span class="truncate">${io.name}</span>
              </a>
              <div class="collapsible-body">
                <ul>${ device(ds) }</ul>
              </div>
            </li>"""
      }

    def device(d:Device) =
      for(src <- Constants(d.io.sources: _*)) yield src match {
        case IOPort(name,types) =>
          html"""<li>
            <a href="#!">
              <i class="material-icons blue-text tiny">gamepad</i>
              ${ name }
              <div class="secondary-content"><i class="material-icons red-text text-lighten-2 tiny">remove_red_eye</i></div>
            </a>
          </li>"""

        // case Button(name, pin, value) => 
        //   <li><a href="#!"><i class="material-icons blue-text tiny">gamepad</i>@name<div class="secondary-content"><i class="material-icons red-text text-lighten-2 tiny">remove_red_eye</i></div></a></li>
        
        // case ButtonEx(name, pin, value) =>  
        //   <li><a href="#!"><i class="material-icons blue-text tiny">gamepad</i>@name</a></li>
        
        // case Analog(name, pin) => 
        //   <li><a href="#!"><i class="material-icons blue-text tiny">radio_button_checked</i>@name</a></li>
        
        // case AnalogSigned(name, pin) => 
        //   <li><a href="#!"><i class="material-icons blue-text tiny">radio_button_checked</i>@name</a></li>
      }
    
  }
}