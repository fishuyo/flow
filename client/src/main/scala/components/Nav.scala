
package flow
package client
package components

import com.thoughtworks.binding.Binding, Binding._
import com.yang_bo.html._


// case class NavLink(text:String, route:String, icon:String)

object Nav {

  def apply(title:String, pages:Seq[Page]) = html"""
    <nav>
      <div class="nav-wrapper">
        <a href="#" class="brand-logo">${title}</a>
        <ul id="nav-mobile" class="right hide-on-med-and-down">
        ${
          for(p <- pages) yield {
            html"""<li><a href="${p.route}">${p.name}</a></li>"""
          }
        }
        </ul>
      </div>
    </nav>
  """
}