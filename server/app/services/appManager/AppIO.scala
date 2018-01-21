
package flow

import protocol.AppConfig
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
    new AppIO(AppConfig(name,Seq(),Seq(),Seq()))
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

  val sourceNames = Set[String]()
  val sinkNames = Set[String]()
  val sourceActors = HashMap[String,ActorRef]()

  val oscSend = new OSCSend() 
  var hostname = "localhost"
  var sinkPort = 9010

  sourceNames ++= config.sources
  sinkNames ++= config.sinks

	
  override def sources:Map[String,Source[Float,akka.NotUsed]] = sourceNames.map { case name =>
    val src = Source.actorRef[Float](bufferSize = 0, OverflowStrategy.fail)
      .mapMaterializedValue( (a:ActorRef) => { sourceActors(name) = a; akka.NotUsed } )
    name -> src
  }.toMap

  override def sinks:Map[String,Sink[Float,akka.NotUsed]] = sinkNames.map { case name =>
    val sink = Sink.foreach( (f:Float) => {
      try{ oscSend.send(s"/$name", f) }
      catch{ case e:Exception => AppManager.close(config.name) }
    }).mapMaterializedValue{ case _ => akka.NotUsed}
    name -> sink
  }.toMap

  val handler:OSC.OSCHandler = {
    case (Message(name:String, value:Float), addr) => 
      sourceNames += name
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


}

// case class AppConfig(name:String, sources:IOInfo, sinks:IOInfo, osc:OSCConfig)


          // val json = Json.toJson(data).toString()
          // val writer = new PrintWriter(new File(filepath+s"scenes/${data.title}.json" ))
          // writer.write(json)
          // writer.close()


	      //   val bos = new BufferedOutputStream(new FileOutputStream(path, false))
			    // bos.write(byteArray)
			    // bos.close()
