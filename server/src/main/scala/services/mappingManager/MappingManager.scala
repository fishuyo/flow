
package flow

import flow.protocol._
import flow.script._
import ScriptLoaderActor._

import java.io.File
import java.io.PrintWriter
import java.io.FileOutputStream
import java.net.SocketAddress
import java.net.InetSocketAddress

import collection.mutable.HashMap

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import scala.concurrent.duration._
import concurrent.ExecutionContext.Implicits.global

/**
  * The Mapping Manager handles mapping related events and state 
  */
object MappingManager {

  val mappingPath = Config("dataPath") + "/mappings/"
  val mappings = HashMap[String, Mapping]()
  val scripts = HashMap[String, ActorRef]()

  /** loads stored mappings from disk */
  def readMappingsDir() = {
    val d = new File(mappingPath)
    
    val files = {
      if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList
      else List[File]()
    }

    files.foreach { case f =>
      var name = f.getName
      name = name.substring(0, name.lastIndexOf('.')) // remove simple file extension
      val source = scala.io.Source.fromFile(f)
      val code = try source.mkString finally source.close
      val m = Mapping(name,code)
      mappings(name) = m
    }
  }

  /** get Mapping with name */
  def apply(name:String) = mappings(name)

  /** run */
  def run(name:String):Unit = run(mappings(name))
  def run(m:Mapping):Unit = m match {
    case Mapping(name, code, modified, running, errors) =>
      implicit val duration: Timeout = 10.seconds
      val script = scripts.getOrElseUpdate(name, ScriptManager())
      script ! Code(FlowScriptWrapper(code))
      script ! Reload
      val future = script ? Status
      future.onComplete {
        case scala.util.Success(status:Seq[(Int,String)]) => // XXX
          var mapping = m
          if(status.length > 0 || mapping.errors.length > 0){
            val off = FlowScriptWrapper.headerLength 
            val errs = status.map { case (i,s) => MappingError(i-off-1,s) }
            mapping = mapping.copy(errors = errs)
          } else { mapping = mapping.copy(running = true) }
          mappings(mapping.name) = mapping
          WebsocketActor.sendMapping(mapping) 
        case _ => println("MappingManager future complete error") 
      }
  }
  
  def stop(name:String):Unit = stop(mappings(name))
  def stop(m:Mapping):Unit = { 
    val scriptOption = scripts.get(m.name)
    scriptOption.foreach(_ ! Unload)
  }

  def stopAll() = {
    scripts.values.foreach(_ ! Unload)
  }
  
  def save(m:Mapping) = {
    val pw = new PrintWriter(new FileOutputStream(mappingPath + m.name + ".scala", false));
    pw.write(m.code)
    pw.close
  }

}