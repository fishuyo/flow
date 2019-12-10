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

import collection.mutable.HashMap

object Interfaces {

  val data = Vars.empty[String]

  def set(seq:Seq[String]) = {
    data.value.clear 
    data.value ++= seq
  }

  def count = Binding {
    data.bind.length
  }


  object views {

    @dom
    def collapsibleList = {
      <ul class="collapsible collapsible-accordion">
        <li>
          <a class="collapsible-header">
            Interfaces
            <i class="material-icons">arrow_drop_down</i>
            <span class="badge right"> { count.bind.toString } </span>
          </a>
          <div class="collapsible-body">
            <ul class="collapsible collapsible-accordion" data:data-collapsible="accordion">
              { dataList.bind }
            </ul>
          </div>
        </li>
      </ul>
    }

    @dom
    def dataList = {
      for(m <- data) yield m match {
          case name =>
            <li> 
              <a href={s"ijs/$name"} target="_blank">
                { name }  
              </a>
            </li>

      }
    }
  }
}