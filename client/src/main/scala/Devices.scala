package flow

import flow.protocol.Device
import flow.protocol.IOPort

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
// import com.thoughtworks.binding.dom
import org.lrng.binding.html, html.NodeBinding
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

  def deviceCount = Binding {
    devices.value.length //map(_.count)
  }

  object views {

    @html
    def collapsibleList = {
      <ul class="collapsible expandable">
        <li>
          <a class="collapsible-header">
            Devices
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> { deviceCount.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible expandable">
              { deviceList }
            </ul>
          </div>
        </li>
      </ul>
    }
    
    @html
    def deviceList = {
      for(ds <- devices) yield ds match {
          // case Nil => <!-- empty -->
          // case d :: Nil =>
          case Device(io, 1) =>
            <li>
              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="truncate">{io.name}</span>
              </a>
              <div class="collapsible-body">
                <ul>{ device(ds) }</ul>
              </div>
            </li>
          case Device(io, count) =>
          // case d :: xs =>
            <li>
              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="badge">{count.toString}</span>
                <span class="truncate">{io.name}</span>
              </a>
              <div class="collapsible-body">
                <ul>{ device(ds) }</ul>
              </div>
            </li>
      }
    }

    @html
    def device(d:Device) = {
      for(src <- Constants(d.io.sources: _*)) yield src match {
        case IOPort(name,types) =>
          <li>
            <a href="#!">
              <i class="material-icons blue-text tiny">gamepad</i>
              { name }
              <div class="secondary-content"><i class="material-icons red-text text-lighten-2 tiny">remove_red_eye</i></div>
            </a>
          </li>

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
}