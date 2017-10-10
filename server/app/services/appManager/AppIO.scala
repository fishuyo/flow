
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
    // println("AppIO: apply")
    new AppIO(AppConfig(name,Seq(),Seq(),Seq()))
	}

	def fromConfigFile(file:java.io.File):AppIO = {
    // println("AppIO: fromConfigFile")
    val stream = new java.io.FileInputStream(file)
    val config = try {  Json.parse(stream).as[AppConfig] } finally { stream.close() }
    println(config)
    new AppIO(config)
	}
}

class AppIO(config:AppConfig) extends IO {

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
    val sink = Sink.foreach(oscSend.send(s"/$name", _:Float)).mapMaterializedValue{ case _ => akka.NotUsed}
    name -> sink
  }.toMap

  val handler:OSC.OSCHandler = {
    case (Message(name:String, value:Float), addr) => 
      sourceNames += name
      sourceActors.get(name).foreach(_ ! value)
    case msg => println(s"Unhandled msg in AppIO: $msg")  
  }

  def connect() = oscSend.connect(hostname, sinkPort)

  def listen(port:Int) = {
    OSCManager() ! OSCManagerActor.Unbind(port, handler)
    OSCManager() ! OSCManagerActor.Bind(port, handler)
  }


}

// case class AppConfig(name:String, sources:IOInfo, sinks:IOInfo, osc:OSCConfig)


          // val json = Json.toJson(data).toString()
          // val writer = new PrintWriter(new File(filepath+s"scenes/${data.title}.json" ))
          // writer.write(json)
          // writer.close()


	      //   val bos = new BufferedOutputStream(new FileOutputStream(path, false))
			    // bos.write(byteArray)
			    // bos.close()
