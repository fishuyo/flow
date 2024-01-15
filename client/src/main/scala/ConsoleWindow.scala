
package flow

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
// import com.thoughtworks.binding.dom
import org.lrng.binding.html, html.NodeBinding
import org.scalajs.dom.raw._

object ConsoleWindow {



  object views {


    @html
    def main = {
      <ul class="collapsible" data:data-collapsible="accordion">
        <li>
          <div class="collapsible-header"><i class="material-icons">filter_drama</i>Console</div>
          <div class="collapsible-body">
            <span>Lorem ipsum dolor sit amet.</span>
            <span>Lorem ipsum dolor sit amet.</span>
            <span>Lorem ipsum dolor sit amet.</span>
          </div>
        </li>
      </ul>
           
    }

  }

}