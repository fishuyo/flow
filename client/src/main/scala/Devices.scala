package com.fishuyo

import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom


case class Device(name:String)

object Devices {

  val devices = Vars.empty[Device]
  
  def apply() = devices

  @dom
  def renderDevices = {
    <p>Device 1</p>
  }

  @dom
  def renderLength = {
    { devices.length.bind.toString }
  }
}