package flow

import flow.protocol.Device
import flow.protocol.IOPort

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import com.thoughtworks.binding.dom

object Devices {

  val devices = Vars.empty[Device]

  def apply() = devices

  def set(ds:Seq[Device]) = {
    devices.value.clear
    devices.value ++= ds
  }

  def deviceCount = Binding {
    devices.bind.map(_.count).sum
  }

  object views {

    @dom
    def collapsibleList = {
      <ul class="collapsible collapsible-accordion">
        <li>
          <a class="collapsible-header">
            Devices
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> { deviceCount.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible collapsible-accordion" data:data-collapsible="accordion">
              { deviceList.bind }
            </ul>
          </div>
        </li>
      </ul>
    }
    
    @dom
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
                <ul>{ device(ds).bind }</ul>
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
                <ul>{ device(ds).bind }</ul>
              </div>
            </li>
      }
    }

    @dom
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