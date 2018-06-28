
package flow

import protocol.AppConfig
import protocol.IOConfig
import protocol.IOPort
import protocol.Message.appConfigFormat

import julienrf.json.derived._
import play.api.libs.json._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import de.sciss.osc.Message

import collection.mutable.Set
import collection.mutable.HashMap


object AppIO{
	def apply(name:String) = {
    new AppIO(AppConfig(IOConfig(name,Seq(),Seq()),Seq()))
	}

  def fromConfig(conf:String):AppIO = {
    val config = Json.parse(conf).as[AppConfig]
    new AppIO(config)
  }
  
	def fromConfigFile(file:java.io.File):AppIO = {
    val stream = new java.io.FileInputStream(file)
    val config = try {  Json.parse(stream).as[AppConfig] } finally { stream.close() }
    println(config)
    new AppIO(config)
	}
}

class AppIO(val config:AppConfig) extends IO {

  // var oscConfig = OSCConfig()

	// sources as actor refs sent messages from OSCRecv handler
	// sinks as OSCSend to app port and address

  // val sourceNames = Set[String]()
  // val sinkNames = Set[String]()
  val sourceActors = HashMap[String,ActorRef]()

  val oscSend = new OSCSend() 
  var hostname = "localhost"
  var sinkPort = 9010

  // sourcePorts ++= config.io.sources
  // sinkPorts ++= config.io.sinks

	
  override def sources:Map[String,Source[Any,akka.NotUsed]] = config.io.sources.map { case IOPort(name,types) =>
    val src = Source.actorRef[Any](bufferSize = 0, OverflowStrategy.fail)
      .mapMaterializedValue( (a:ActorRef) => { sourceActors(name) = a; akka.NotUsed } )
    name -> src
  }.toMap

  override def sinks:Map[String,Sink[Any,akka.NotUsed]] = config.io.sinks.map { case IOPort(name,types) =>
    val sink = Sink.foreach( (f:Any) => {
      try{ oscSend.send(s"/$name", f) }
      catch{ case e:Exception => AppManager.close(config.io.name) }
    }).mapMaterializedValue{ case _ => akka.NotUsed}
    name -> sink
  }.toMap

  val handler:OSC.OSCHandler = {
    case (Message(name:String, value:Any), addr) => 
      // sourceNames += name
      // XXX need to strip /s?
      sourceActors.get(name).foreach(_ ! value)
    case msg => println(s"Unhandled msg in AppIO: $msg")  
  }

  def connect() = oscSend.connect(hostname, sinkPort)

  def listen() = {
    // XXX may get message intended to be from another app, if same osc namespace! No good
    // either use different ports, or different osc namespace..
    OSCManager() ! OSCManagerActor.Unbind(12000, handler) //XXX find better way to make this work
    OSCManager() ! OSCManagerActor.Bind(12000, handler)
  }

  def close() = {
    stopDefaultMappings()
    OSCManager() ! OSCManagerActor.Unbind(12000, handler)
    oscSend.disconnect
  }

  def runDefaultMappings() = config.defaultMappings.foreach(MappingManager.run(_))
  def stopDefaultMappings() = config.defaultMappings.foreach(MappingManager.stop(_))

  def toJson() = Json.toJson(config).toString
}

// case class AppConfig(name:String, sources:IOInfo, sinks:IOInfo, osc:OSCConfig)


          // val json = Json.toJson(data).toString()
          // val writer = new PrintWriter(new File(filepath+s"scenes/${data.title}.json" ))
          // writer.write(json)
          // writer.close()


	      //   val bos = new BufferedOutputStream(new FileOutputStream(path, false))
			    // bos.write(byteArray)
			    // bos.close()
