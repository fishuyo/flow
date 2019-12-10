
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

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.ConfigFactory

/**
  * The Mapping Manager handles mapping related events and state 
  */
object MappingManager {

  val config = ConfigFactory.load()
  val mappingPath = config.getString("path.mappings")
  val mappings = HashMap[String, Mapping]()
  val scripts = HashMap[String, ActorRef]()

  readMappingsDir()

  /** loads stored mappings from disk */
  def readMappingsDir(){
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
    case Mapping(name, code, loading, modified, running, errors) =>
      implicit val duration: Timeout = 10 seconds
      val script = scripts.getOrElseUpdate(name, ScriptManager())
      script ! Code(FlowScriptWrapper(code))
      script ! Reload
      val future = script ? Status
      future.onSuccess {
        case status:Seq[(Int,String)] =>
          var mapping = m
          if(status.length > 0 || mapping.errors.length > 0){
            val off = FlowScriptWrapper.headerLength 
            val errs = status.map { case (i,s) => MappingError(i-off-1,s) }
            mapping = mapping.copy(errors = errs)
          } else { mapping = mapping.copy(running = true) }
          mapping = mapping.copy(loading = false)
          mappings(mapping.name) = mapping
          WebsocketActor.sendMapping(mapping)  
      }
  }
  
  def stop(name:String):Unit = stop(mappings(name))
  def stop(m:Mapping):Unit = { 
    val scriptOption = scripts.get(m.name)
    scriptOption.foreach(_ ! Unload)
    var mapping = m.copy(running = false, loading = false)
    mappings(m.name) = mapping
    WebsocketActor.sendMapping(mapping)  
  }

  def stopAll() = {
    scripts.values.foreach(_ ! Unload)
    mappings.values.foreach { case m =>
      mappings(m.name) = m.copy(running = false, loading = false)
    }
    WebsocketActor.sendMappingList()
  }
  
  def save(m:Mapping) = {
    val pw = new PrintWriter(new FileOutputStream(mappingPath + m.name + ".scala", false));
    pw.write(m.code)
    pw.close
  }

}