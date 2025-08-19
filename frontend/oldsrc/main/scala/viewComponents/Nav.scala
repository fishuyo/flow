
package flow
package client
package components

import com.thoughtworks.binding.Binding, Binding._
import com.yang_bo.html._

class Nav {

  var title = html"""<a href="#" class="brand-logo"> </a>"""

  // var links = 

  def render = html"""
    <nav>
      <div class="nav-wrapper">
        ${title}
        <ul id="nav-mobile" class="right hide-on-med-and-down">
          <li><a href="sass.html">Sass</a></li>
          <li><a href="badges.html">Components</a></li>
          <li><a href="collapsible.html">JavaScript</a></li>
        </ul>
      </div>
    </nav>
  """


}