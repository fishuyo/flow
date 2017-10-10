
package flow

import protocol._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import org.scalajs.dom.document
import org.scalajs.dom.console
import org.scalajs.dom.raw._

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.dom

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror._

object CodeEditor {

  var editor:Editor = _
  var mapping:Mapping = _

  def init(id:String) = {
    val config: EditorConfiguration = EditorConfig.
      mode("text/x-scala").
      lineNumbers(true).
      theme("material").
      keyMap("sublime").
      tabSize(2).
      indentWithTabs(false)

    document.getElementById(id) match {
      case elem:HTMLTextAreaElement =>
        editor = CodeMirror.fromTextArea(elem, config)
        editor.setSize("100%","80vh")
        //editor.getDoc().setValue(demoSource)

      case _ => console.error("cannot find text area for the code!")
    }

    load(Mapping("untitled",demoSource))
  }

  def getCode() = {
    val m = mapping.copy(code = editor.getDoc().getValue(), modified = true)
    mapping = m    
  }

  def run() = {
    println("Run Code!")
    getCode()
    Socket.send(Run(mapping))
  }

  def stop() = {
    println("Stop!")
    Socket.send(Stop(mapping))
  }

  def load(m:Mapping) = {
    println("Load!")
    mapping = m
    editor.getDoc().setValue(m.code)
  }

  def save() = {
    println("Save!")
    getCode()
    Socket.send(Save(mapping))
    mapping = mapping.copy(modified = false)
  }

  def newMapping() = {
    val m = Mapping("unamed","",true)
    Mappings().get.prepend(m)
    load(m)
  }


  object views {

    @dom
    def textarea = <textarea id="code" name="scala"></textarea>

    @dom
    def main = {
      <nav>
        <div class="nav-wrapper">
          <!-- <a href="#!" class="brand-logo">Logo</a> -->
          <ul class="left hide-on-small">
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); run() }>
                <i class="material-icons left">play_arrow</i>
                Run
              </a>
            </li>
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); stop() }>
                <i class="material-icons left">stop</i>
                Stop
              </a>
            </li>
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); save() }>
                <i class="material-icons left">save</i>
                Save
              </a>
            </li>
            <li>
              <a href="#" onclick={ event:Event => event.preventDefault(); newMapping() }>
                <i class="material-icons left">add_circle_outline</i>
                New
              </a>
            </li>
          </ul>
        </div>
      </nav>

      <div class="row">
        <div class="col s12"> 
          { textarea.bind }          
        </div>
      </div>
    }

  }

  val demoSource = """

  OSC.connect("localhost",9010)
  def osc(adr:String) = Sink.foreach( OSC.send(adr, _:Any))

  val joy = DeviceManager.joysticks(0)
  //val app = AppManager("hydrogen")

  joy.leftX.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else f ) >> osc("/mx")
  joy.leftY.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else -f ) >> osc("/mz")
  joy.rightX.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else -0.01*f ) >> osc("/ty")
  joy.rightY.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else -0.01*f ) >> osc("/tx")

  joy.upAnalog >> osc("/my")
  joy.downAnalog.map(_ * -1) >> osc("/my")

  joy.R2Analog.map(_ * -0.01) >> osc("/tz")
  joy.L2Analog.map(_ * 0.01) >> osc("/tz")

  joy.rightClick.filter(_ == 1) >> osc("/wSlerp")
  
  joy.triangle >> osc("/pV")
  
  joy.accX.zip(joy.accY) >> Sink.foreach( (xy:(Float,Float)) => OSC.send("/pUU", -xy._1*180, xy._2*180, 0))

  joy.select >> osc("/halt")


  """



}