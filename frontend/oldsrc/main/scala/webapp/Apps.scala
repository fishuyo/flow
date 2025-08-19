package flow

import flow.protocol.AppConfig
import flow.protocol.IOPort

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import com.yang_bo.html._

import org.scalajs.dom.raw._

object Apps {

  val apps = Vars.empty[AppConfig]

  def apply() = apps

  def set(ds:Seq[AppConfig]) = {
    apps.value.clear
    apps.value ++= ds
  }

  object views {

    def collapsibleList = html"""
      <ul class="collapsible expandable">
        <li>
          <a class="collapsible-header">
            Apps
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> ${ apps.length.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible expandable">
              ${ appList }
            </ul>
          </div>
        </li>
      </ul>
    """
    
    def appList = for(ap <- apps) yield ap match {
      case a:AppConfig =>
        html"""<li>
          <a class="collapsible-header">
            <i class="material-icons">arrow_drop_down</i>
            <span class="truncate">${a.io.name}</span>
          </a>
          <div class="collapsible-body">
            <ul>${ app(a) }</ul>
          </div>
        </li>"""
    }
    

    def app(app:AppConfig) = {
      for(src <- Constants(app.io.sources: _*)) yield src match {
        case IOPort(name,types) =>
          html"""<li>
            <a href="#!">
              <i class="material-icons blue-text tiny">arrow_back</i>
              ${ name }
            </a>
          </li>"""
      }
      for(sink <- Constants(app.io.sinks: _*)) yield sink match {
        case IOPort(name,types) =>
          html"""<li>
            <a href="#!">
              <i class="material-icons blue-text tiny">arrow_forward</i>
              ${ name }
            </a>
          </li>"""
      }
    }
  } 
}