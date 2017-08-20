
package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom


object ConsoleWindow {



  object views {


    @dom
    def main = {
      <div class="row">
        <div class="col s12"> 
          Console Area         
        </div>
      </div>
    }

  }

}