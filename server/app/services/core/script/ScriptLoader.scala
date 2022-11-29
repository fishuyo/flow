package flow
package script

import java.io.File
import scala.io.Source

import scala.language.dynamics

import reflect.runtime.universe._
import reflect.runtime.currentMirror
import tools.reflect.ToolBox

// import com.twitter.util.Eval

import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import concurrent.Await
import concurrent.duration._

import collection.mutable.HashMap
import collection.mutable.ListBuffer

/**
  * ScriptLoaderActor companion object
  */
object ScriptLoaderActor {
  case class Path(path:String, reloadOnChange:Boolean)
  case class Code(code:String)
  case object Load
  case object Reload
  case object Unload
  case object Status

  // def props = propsEval
  def props = propsToolbox
  // def propsEval = Props(new ScriptLoaderActor(new EvalScriptLoader()))
  def propsToolbox = Props(new ScriptLoaderActor(new ToolboxScriptLoader()))
}
/**
 * ScriptLoaderActor, each responsible for compiling and running
 * a script file or chunk of code
 */
class ScriptLoaderActor(val loader:ScriptLoader) extends Actor with ActorLogging {
  import ScriptLoaderActor._
  def receive = {
    case Path(path, reloadOnChange) =>
      log.info(s"path $path")
      // if(reloadOnChange) Monitor(path){ (p) => self ! Reload }
      loader.setPath(path)
    case Code(code) => 
      loader.setCode(code)
    case Load | "load" =>
      log.info("loading..");
      loader.reload()
    case Reload | "reload" => 
      log.info("reloading..");
      loader.reload()
    case Unload | "unload" => loader.unload()
    case Status => 
      if(loader.errors.isEmpty) loader.checkErrors()
      sender ! loader.errors

    case x => log.warning("Received unknown message: {}", x)
  }
}

/**
  * ScriptLoader trait 
  */
trait ScriptLoader {

  var loaded = false
  var code=""
  var path:Option[String] = None
  var obj:AnyRef = null
  var errors:Seq[(Int,String)] = Seq()

  def setPath(s:String) = path = Some(s)
  def setCode(s:String) = code = s
  def getCode() = {
    if(path.isDefined) code = Source.fromFile(new File(path.get)).mkString
    code
  }

  def reload(){
    try{
      errors = Seq()
      val ret = compile()
      ret match{
        case s:Script =>
          unload
          obj = ret
          s.load()
          loaded = true
        case a:ActorRef =>
          unload
          obj = ret
          a ! "load"
          loaded = true
        case obj => println(s"Unrecognized return value from script: $obj")
      }
    } catch { case e:Exception => 
      e.printStackTrace 
      val frame = e.getStackTrace.find{ e => e.getMethodName.contains("load") }.get
      errors = Seq((frame.getLineNumber, "RuntimeError: " + e.toString))

      // val cause = e.getCause
      // if(cause != null){
        // e.printStackTrace
        // val frame = cause.getStackTrace.find{ e => e.getMethodName.contains(".load") }.get
        // println(s"$cause at line ${frame.getLineNumber}")
        // errors = Seq((frame.getLineNumber, "RuntimeError: " + cause.toString))
      // } else println("Exception in script: " + e); 
      unload
    }
  }

  def unload(){
    obj match {
      case s:Script => 
        s.unload()
        obj = null
        loaded = false
      case a:ActorRef =>
        a ! "unload"
        a ! akka.actor.PoisonPill
        obj = null
        loaded = false
      case _ => ()
    }
  }

  def checkErrors(){}

  def compile():AnyRef
}

/**
  * Toolbox implementation of a ScriptLoader
  */
object ToolboxScriptLoader {
  val toolbox = currentMirror.mkToolBox() 

  def logToolboxErrorLocation(){
  }
}
class ToolboxScriptLoader extends ScriptLoader {
  val toolbox = ToolboxScriptLoader.toolbox //currentMirror.mkToolBox() 

  def compile() = {
    val source = getCode()
    val tree = toolbox.parse(source)
    // checkErrors()
    val script = toolbox.eval(tree).asInstanceOf[AnyRef]
    script
  } 

  override def checkErrors() = {
    if(toolbox.frontEnd.hasErrors){
      val errs = toolbox.frontEnd.infos.map { case info =>
        val line = info.pos.line
        val msg = s"""
          ${info.msg}
          ${info.pos.lineContent}
          ${info.pos.lineCaret} 
        """
        (line,msg)
      }.toSeq
      errors = errs
    } else errors = Seq()
  }
}

/**
  * Twitter Eval implementation of a ScriptLoader
  */
// object EvalScriptLoader {
//   val eval = new Eval()
//   def apply() = eval
// }
// class EvalScriptLoader extends ScriptLoader {
//   def compile() = {
//     val source = getCode()
//     val script = EvalScriptLoader.eval[AnyRef](source) 
//     script
//   }
// }




