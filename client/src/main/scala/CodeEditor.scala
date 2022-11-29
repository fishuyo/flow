
// package flow

// import protocol._

// import scala.scalajs.js
// import scala.scalajs.js.annotation._
// import scala.scalajs.js.Dynamic.global
// import org.scalajs.dom.document
// import org.scalajs.dom.window
// import org.scalajs.dom.console
// import org.scalajs.dom.raw._

// import com.thoughtworks.binding.Binding
// import com.thoughtworks.binding.Binding.{Var, Vars}
// // import com.thoughtworks.binding.dom
// import org.lrng.binding.html, html.NodeBinding
// import org.scalajs.dom.raw._

// // import org.denigma.codemirror.extensions.EditorConfig
// // import org.denigma.codemirror._

// // import org.querki.jquery._
// // import com.definitelyscala.materializecss.{JQuery => JQ}

// object CodeEditor {

//   var editor:Editor = _
//   var mapping:Mapping = _

//   def init(id:String) = {
//     val config: EditorConfiguration = EditorConfig.
//       mode("text/x-scala").
//       lineNumbers(true).
//       theme("material").
//       keyMap("sublime").
//       tabSize(2).
//       indentWithTabs(false).
//       gutters(js.Array("errors")).
//       extraKeys(js.Dictionary[js.Function1[Editor,Unit]](
//         //"Tab" -> ((cm:Editor) => println("tab tab")),
//         "Cmd-S" -> ((cm:Editor) => save()),
//         "Cmd-Enter" -> ((cm:Editor) => run())
//       ))

//     document.getElementById(id) match {
//       case elem:HTMLTextAreaElement =>
//         editor = CodeMirror.fromTextArea(elem, config)
//         editor.setSize("100%","80vh")

//       case _ => console.error("cannot find text area for the code!")
//     }

//     // editor.on("gutterClick", (cm:Editor, n:Int) => {
//     //   var info = cm.lineInfo(n);
//     //   cm.setGutterMarker(n, "errors", if(info.gutterMarkers == null) makeMarker() else null);
//     //   ()
//     // });

//     load(Mapping("untitled",""))
//   }

//   def makeMarker(msg:String) = {
//     val text = HtmlEscape(msg) //msg.split('\n').mkString("<br/>"))
//     var marker = document.createElement("div").asInstanceOf[HTMLDivElement];
//     marker.innerHTML = s"""<a class="tooltipped" style="color:#822;" data-position="right" data-delay="50" data-html="true" data-tooltip="$text">●</a>"""
//     marker
//   }

//   def getCode() = {
//     val code_ = editor.getDoc().getValue()
//     if(mapping.code != code_){
//       mapping = mapping.copy(code = code_, modified = true)
//       Mappings(mapping.name) = mapping          
//     }
//   }

//   def run(){
//     println("Run Code!")
//     getCode()
//     if(!mapping.running){
//       mapping = mapping.copy(running = true)
//       Mappings(mapping.name) = mapping
//     }
//     Socket.send(Run(mapping))
//   }

//   def stop(){
//     println("Stop!")
//     if(mapping.running){
//       mapping = mapping.copy(running = false)
//       Mappings(mapping.name) = mapping
//     }
//     Socket.send(Stop(mapping))
//   }

//   def load(m:Mapping){
//     println("Load!")
//     mapping = m
//     editor.getDoc().setValue(m.code)
//     setErrorMarkers(m)
//   }

//   def save(){
//     println("Save!")
//     getCode()
//     if(mapping.modified){
//       Socket.send(Save(mapping))
//       mapping = mapping.copy(modified = false)
//       Mappings(mapping.name) = mapping          
//     }
//   }

//   def newMapping(name:String){
//     getCode()
//     val m = Mapping(name,"",true)
//     Mappings(name) = m
//     load(m)
//   }

//   def stopAll(){
//     Socket.send(StopAll)
//     Mappings.mappings_.foreach{ case (s,m) => 
//       Mappings.update(s, m.copy(running = false))
//     }
//   }

//   def setErrorMarkers(m:Mapping){
//     editor.clearGutter("errors")
//     m.errors.foreach { case MappingError(line,msg) =>
//       var info = editor.lineInfo(line)
//       editor.setGutterMarker(line, "errors", makeMarker(msg));
//     }
//     global.jQuery(".tooltipped").tooltip()
//   }


//   object views {

//     @html
//     def textarea = <textarea id="code" name="scala"></textarea>

//     @html
//     def main = {
//       <nav>
//         <div class="nav-wrapper">
//           <!-- <a href="#!" class="brand-logo">Logo</a> -->
//           <ul class="left hide-on-small">
//             <li>
//               <a href="#" onclick={ event:Event => event.preventDefault(); run() }>
//                 <i class="material-icons left">play_arrow</i>
//                 Run
//               </a>
//             </li>
//             <li>
//               <a href="#" onclick={ event:Event => event.preventDefault(); stop() }>
//                 <i class="material-icons left">stop</i>
//                 Stop
//               </a>
//             </li>
//             <li>
//               <a href="#" onclick={ event:Event => event.preventDefault(); save() }>
//                 <i class="material-icons left">save</i>
//                 Save
//               </a>
//             </li>
//             <li>
//               <a class="waves-effect waves-light modal-trigger" href="#newModal" onclick={ event:Event => 
//                 event.preventDefault() 
//                 val input = document.getElementById("mappingName").asInstanceOf[HTMLInputElement]
//                 input.value = ""
//               }>
//                 <i class="material-icons left">add_circle_outline</i>
//                 New
//               </a>
//             </li>
//             <li>
//               <a href="#" onclick={ event:Event => event.preventDefault(); stopAll() }>
//                 <i class="material-icons left">cancel</i>
//                 Stop All
//               </a>
//             </li>
//           </ul>
//         </div>
//       </nav>

//       <!-- Modal Structure -->
//       <div id="newModal" class="modal">
//         <div class="modal-content">
//           <h4>New Mapping</h4>
//           <div class="row">
//             <div class="input-field col s6">
//               <input value="" id="mappingName" type="text" class="validate" />
//               <label class="active" for="mappingName">Mapping Name</label>
//             </div>
//           </div>
//         </div>
//         <div class="modal-footer">
//           <a href="#!" class="modal-action modal-close waves-effect waves-red btn-flat">Cancel</a>
//           <a href="#!" class="modal-action modal-close waves-effect waves-green btn-flat" onclick={ event:Event => 
//                 event.preventDefault() 
//                 val input = document.getElementById("mappingName").asInstanceOf[HTMLInputElement]
//                 if(input.value != "") newMapping(input.value)
//                 else window.alert("Could not create mapping: Invalid name.")
//           }>Create</a>
//         </div>
//       </div>

//       <div class="row">
//         <div class="col s12"> 
//           { textarea.bind }          
//         </div>
//       </div>
//     }

//   }

//   val demoSource = """

//   OSC.connect("localhost",9010)
//   def osc(adr:String) = Sink.foreach( OSC.send(adr, _:Any))

//   val joy = DeviceManager.joysticks(0)
//   //val app = AppManager("hydrogen")

//   joy.leftX.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else f ) >> osc("/mx")
//   joy.leftY.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else -f ) >> osc("/mz")
//   joy.rightX.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else -0.01*f ) >> osc("/ty")
//   joy.rightY.map(2 * _ - 1).map( (f) => if(math.abs(f) < 0.06) 0 else -0.01*f ) >> osc("/tx")

//   joy.upAnalog >> osc("/my")
//   joy.downAnalog.map(_ * -1) >> osc("/my")

//   joy.R2Analog.map(_ * -0.01) >> osc("/tz")
//   joy.L2Analog.map(_ * 0.01) >> osc("/tz")

//   joy.rightClick.filter(_ == 1) >> osc("/wSlerp")
  
//   joy.triangle >> osc("/pV")
  
//   joy.accX.zip(joy.accY) >> Sink.foreach( (xy:(Float,Float)) => OSC.send("/pUU", -xy._1*180, xy._2*180, 0))

//   joy.select >> osc("/halt")


//   """
// }

// object HtmlEscape {
//   def apply(html: String): String = {
//     val builder = new StringBuilder
  
//     for (c <- html) {
//       c match {
//         case '&'  => builder.append("&amp;")
//         case '"'  => builder.append("&quot;")
//         case '<'  => builder.append("&lt;")
//         case '>'  => builder.append("&gt;")
//         case '\'' => builder.append("&#39;")
//         case '`'  => builder.append("&#96;")
//         case '{'  => builder.append("&#123;")
//         case '}'  => builder.append("&#125;")
//         case _    => builder.append(c)
//       }
//     }

//     builder.toString
//   }
// }