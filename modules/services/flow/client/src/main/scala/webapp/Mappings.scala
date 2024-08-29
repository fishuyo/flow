package flow

import protocol._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding._
// import com.thoughtworks.binding.dom
// import org.lrng.binding.html, html.NodeBinding
import com.yang_bo.html._

import org.scalajs.dom.raw._
import collection.mutable.HashMap

object Mappings {

  val mappings_ = HashMap[String,Mapping]()
  val mappings = Vars.empty[Mapping]
  // val count = Var[Int](0)

  def apply(name:String) = mappings_(name)
  def update(name:String,m:Mapping) = {
    mappings_(name) = m
    mappings.value.clear 
    mappings.value ++= mappings_.values.toSeq.sortBy(_.name)
    // if(m.name == CodeEditor.mapping.name) CodeEditor.setErrorMarkers(m) // XXX
  }

  def ++=(seq:Seq[Mapping]) = {
    seq.foreach { case m => mappings_(m.name) = m }
    mappings.value.clear 
    mappings.value ++= mappings_.values.toSeq.sortBy(_.name)
    // count.value = seq.length
  }


  object views {

    def collapsibleList = html"""
      <ul class="collapsible expandable">
        <li>
          <a class="collapsible-header">
            Mappings
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right">${ mappings.length.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible expandable">
              ${ mappingList }
            </ul>
          </div>
        </li>
      </ul>
    """

    def mappingList =
      for(m <- mappings) yield m match {
          case Mapping(name, code, modified, running, errors) =>
            html"""<li> 
              <a href="#" onclick=${ (event:Event) => {event.preventDefault(); CodeEditor.load(m)} }>
                ${ name }  
                ${ if(errors.length > 0) html"""<i class="material-icons">error</i>"""
                  else if(modified) html"""<i class="material-icons">edit</i>"""
                  else if(running) html"""<i class="material-icons">directions_run</i>"""
                  else html"""<i></i>""" 
                }
              </a>
 <!--              <a class="collapsible-header">
                <i class="material-icons">arrow_drop_down</i>
                <span class="truncate">(untitled)</span>
              </a>
              <div class="collapsible-body">
                <ul>{ mapping(m).bind }</ul>
              </div> -->
            </li>"""

      }

    // @html
    // def mapping(m:Mapping) = {
      
    // }

  }
}