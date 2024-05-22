package flow
package client

import com.yang_bo.html.*

object OldPage extends Page {

  override val name = "old"

  def render = html"""
    ${ renderHeader }
    ${ renderMain }
    ${ renderFooter }
  """


  def renderHeader = html"""
    <div class="blue-grey lighten-5">
      <ul id="slide-out" class="sidenav sidenav-fixed blue-grey lighten-5">
        <li class="no-padding">
          ${ Devices.views.collapsibleList }
        </li>
        <li class="no-padding">
          ${ Apps.views.collapsibleList }
        </li> 
        <li class="no-padding">
          ${ Mappings.views.collapsibleList }
        </li>
      </ul>

      <a id="menu-button-left" href="#" data-target="slide-out" class="sidenav-trigger hide-on-large-only"><i class="material-icons">menu</i></a>
    </div>
  """

  def renderMain = html"""
    <div class="blue-grey darken-4">
      ${ CodeEditor.views.main }
    </div>
  """

  def renderFooter = html"""
    <div class="page-footer">
      <div class="container">
        <div class="row">
          <div class="col l6 s12">
          </div>
          <div class="col l4 offset-l2 s12">

          </div>
        </div>
      </div>
      <div class="footer-copyright">
        <div class="container">
        AlloSphere Device Server
        <a class="grey-text text-lighten-4 right" href="#!">More Services</a>
        </div>
      </div>
    </div> 
  """
  

}
