
package flow

import protocol._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.Dynamic.global
import org.scalajs.dom.document
import org.scalajs.dom.window
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom


object Toolbar {

  object views {

    @dom
    def codeEditorNav = {
      <nav>
        <div class="nav-wrapper">
          <!-- <a href="#!" class="brand-logo">Logo</a> -->
          <ul class="left hide-on-small">
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); CodeEditor.run() }>
                <i class="material-icons left">play_arrow</i>
                Run
              </a>
            </li>
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); CodeEditor.stop() }>
                <i class="material-icons left">stop</i>
                Stop
              </a>
            </li>
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); CodeEditor.save() }>
                <i class="material-icons left">save</i>
                Save
              </a>
            </li>
            <li>
              <a class="waves-effect waves-light modal-trigger" href="#newModal" onclick={ event:Event => 
                event.preventDefault() 
                val input = document.getElementById("mappingName").asInstanceOf[HTMLInputElement]
                input.value = ""
              }>
                <i class="material-icons left">add_circle_outline</i>
                New
              </a>
            </li>
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); CodeEditor.stopAll() }>
                <i class="material-icons left">cancel</i>
                Stop All
              </a>
            </li>
          </ul>
        </div>
      </nav>


      <!-- Modal Structure -->
      <div id="newModal" class="modal">
        <div class="modal-content">
          <h4>New Mapping</h4>
          <div class="row">
            <div class="input-field col s6">
              <input value="" id="mappingName" type="text" class="validate" />
              <label class="active" for="mappingName">Mapping Name</label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <a href="#!" class="modal-action modal-close waves-effect waves-red btn-flat">Cancel</a>
          <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat" onclick={ event:Event => 
                event.preventDefault() 
                val input = document.getElementById("mappingName").asInstanceOf[HTMLInputElement]
                if(input.value != "") CodeEditor.newMapping(input.value)
                else window.alert("Could not create mapping: Invalid name.")
          }>Create</a>
        </div>
      </div>
    }

  }

}
