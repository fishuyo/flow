package flow

import flow.protocol.AppConfig

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import com.thoughtworks.binding.dom

object Apps {

  val apps = Vars.empty[AppConfig]

  def apply() = apps

  def set(ds:Seq[AppConfig]) = {
    apps.value.clear
    apps.value ++= ds
  }


  def appCount = Binding {
    apps.bind.length
  }

  object views {

    @dom
    def collapsibleList = {
      <ul class="collapsible collapsible-accordion">
        <li>
          <a class="collapsible-header">
            Apps
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> { appCount.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible collapsible-accordion" data:data-collapsible="accordion">
              { appList.bind }
            </ul>
          </div>
        </li>
      </ul>
    }
    
    @dom
    def appList = {
      for(ap <- apps) yield ap match {
          case a:AppConfig =>
            <li>
              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="truncate">{a.name}</span>
              </a>
              <div class="collapsible-body">
                <ul>{ app(a).bind }</ul>
              </div>
            </li>
      }
    }

    @dom
    def app(app:AppConfig) = {
      for(src <- Constants(app.sources: _*)) yield src match {
        case name =>
          <li>
            <a href="#!">
              <i class="material-icons blue-text tiny">arrow_back</i>
              { name }
            </a>
          </li>
      }
      for(sink <- Constants(app.sinks: _*)) yield sink match {
        case name =>
          <li>
            <a href="#!">
              <i class="material-icons blue-text tiny">arrow_forward</i>
              { name }
            </a>
          </li>
      }
    }
    
  }
}