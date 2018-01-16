
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

object MappingManager {

  val script = ScriptManager()  // TODO: need multiple script actors for each mapping script running


  val mappingPath = "data/mappings/"
  val mappings = HashMap[String, Mapping]()
  // readMappingsDir()

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

  def apply(name:String) = mappings(name)

  def run(name:String):Unit = run(mappings(name))
  def run(m:Mapping):Unit = m match {
    case Mapping(name, code, modified, running, errors) =>
      script ! Code(FlowScriptWrapper(code)); script ! Reload
  }
  
  def stop(name:String):Unit = stop(mappings(name))
  def stop(m:Mapping):Unit = { script ! Unload }
  
  def save(m:Mapping) = {
    val pw = new PrintWriter(new FileOutputStream(mappingPath + m.name + ".scala", false));
    pw.write(m.code)
    pw.close
  }

}