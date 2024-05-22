
package flow
package client

import com.yang_bo.html.*

import flow.client.components.*

object MainPage extends Page {

  override val name = "flow"
  override val route = ""
  
  override def render = html"""
    ${ Nav("Flow", WebApp.pages.values.toSeq) }
    <div>Main Page</div>
  """
}