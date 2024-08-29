
package flow
package client

import com.thoughtworks.binding.Binding, Binding._
import com.yang_bo.html._

import flow.client.components._

object WebApp {


  def render = html"""
    <header>${ renderHeader }</header>
    <main>${ renderMain }</main>
    <footer>${ renderFooter }</footer>
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
      <!-- <div class="fixed-action-btn click-to-toggle">
        <a class="btn-floating btn-large red">
          <i class="material-icons">menu</i>
        </a>
        <ul>
          <li><a class="btn-floating red"><i class="material-icons">insert_chart</i></a></li>
          <li><a class="btn-floating yellow darken-1"><i class="material-icons">format_quote</i></a></li>
          <li><a class="btn-floating green"><i class="material-icons">publish</i></a></li>
          <li><a class="btn-floating blue"><i class="material-icons">attach_file</i></a></li>
        </ul>
      </div> -->

      ${ CodeEditor.views.main }
      // <!--{ ConsoleWindow.views.main.bind }-->
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
