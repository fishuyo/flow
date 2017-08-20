package flow

import protocol._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
import com.thoughtworks.binding.dom


object Mappings {

  val mappings = Vars.empty[Mapping]

  def mappingCount = Binding {
    mappings.bind.length
  }

  object views {

    @dom
    def collapsibleList = {
      <ul class="collapsible collapsible-accordion">
        <li>
          <a class="collapsible-header">
            Mappings
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> { mappingCount.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible collapsible-accordion" data:data-collapsible="accordion">
              { mappingList.bind }
            </ul>
          </div>
        </li>
      </ul>
    }

    @dom
    def mappingList = {
      for(m <- mappings) yield m match {
          case Mapping(name, code) =>
            <li> 
              <a href="#" onclick={ event:Event => event.preventDefault(); CodeEditor.load(m) }>
                {name}
              </a>
 <!--              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="truncate">(untitled)</span>
              </a>
              <div class="collapsible-body">
                <ul>{ mapping(m).bind }</ul>
              </div> -->
            </li>

      }
    }

    // @dom
    // def mapping(m:Mapping) = {
      
    // }

  }
}