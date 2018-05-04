
package flow
package ijs

import protocol.IOPort

import java.io.File
import java.io.PrintWriter
import java.io.FileOutputStream

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import concurrent.ExecutionContext.Implicits.global

import collection.mutable.ListBuffer
import collection.mutable.HashMap
import collection.mutable.Set

sealed trait Widget{ 
  def name:String
  def x:Float
  def y:Float
  def w:Float
  def h:Float
}
// config:(String,Any)*
case class Slider(name:String,x:Float,y:Float,w:Float,h:Float) extends Widget
case class Button(name:String, x:Float,y:Float,w:Float,h:Float) extends Widget
case class XY(name:String, x:Float,y:Float,w:Float,h:Float) extends Widget
case class Value(name:String, x:Float,y:Float,w:Float,h:Float) extends Widget

object Interface {

  val interfaces = HashMap[String,InterfaceBuilder]()

  def apply(name:String) = interfaces.getOrElseUpdate(name, new InterfaceBuilder(name))

  def create(name:String) = {
    val io = interfaces.getOrElseUpdate(name, new InterfaceBuilder(name))
    io.widgets.clear
    io
  }
  
  def fromApp(app:AppIO) = {
    val io = app.config.io
    val ijs = interfaces.getOrElseUpdate(io.name, new InterfaceBuilder(io.name))
    var sx = 0f
    var bx = 0f
    io.sinks.foreach {
      case IOPort(name,"f") => ijs += Slider(name,sx,0f,0.1f,0.5f); sx += 0.1f
      case IOPort(name,"bool") => ijs += Button(name,bx,0.55f,0.1f,0.1f); bx += 0.1f
      case IOPort(name,"") => ijs += Button(name,bx,0.55f,0.1f,0.1f); bx += 0.1f
      case _ => ()
    }
    ijs.save()
    ijs
  }
}



class InterfaceBuilder(val name:String) extends IO {

  val widgets = ListBuffer[Widget]()
  def +=(w:Widget) = widgets += w

  var sourceActor:Option[ActorRef] = None
  val _src = Source.actorRef[(String,Any)](bufferSize = 0, OverflowStrategy.fail)
                                    .mapMaterializedValue( (a:ActorRef) => sourceActor = Some(a) )
  val broadcastSource: Source[(String,Any),akka.NotUsed] = _src.toMat(BroadcastHub.sink)(Keep.right).run().buffer(1,OverflowStrategy.dropHead) 
    .watchTermination()((_, f) => {f.onComplete {  // for debugging
      case t => println(s"Interface source terminated: $name: $t")
    }; akka.NotUsed })
  // val sourceActors = HashMap[String,ActorRef]()
  var sinkActors = ListBuffer[ActorRef]()
  
  override def sources:Map[String,Source[Any,akka.NotUsed]] = widgets.map { case w =>
    val src = broadcastSource.collect{ case (name,value) if name == w.name => value }
      // Source.actorRef[Float](bufferSize = 0, OverflowStrategy.fail)
      // .mapMaterializedValue( (a:ActorRef) => { sourceActors(w.name) = a; akka.NotUsed } )
    w.name -> src
  }.toMap

  override def sinks:Map[String,Sink[Any,akka.NotUsed]] = widgets.map { case w =>
    val sink = Sink.foreach( (f:Any) => {
      sinkActors.foreach( _ ! (w.name,f))
      // try{ oscSend.send(s"/$name", f) }
      // catch{ case e:Exception => AppManager.close(config.io.name) }
    }).mapMaterializedValue{ case _ => akka.NotUsed}
    w.name -> sink
  }.toMap

  def save(){
    val path = "server/public/interfaces/"
    val pw = new PrintWriter(new FileOutputStream(path + name + ".html", false));
    pw.write(toHtml)
    pw.close
  }

  def toHtml() = {
    htmlHeader +
    "panel = new Interface.Panel({ useRelativeSizesAndPositions:true })\n" +
    "panel.background = 'black'\n" +
    widgets.map{ 
      case Slider(name,x,y,w,h) => s"""$name = new Interface.Slider({ name:"$name", label:"$name", bounds: [$x,$y,$w,$h] ${if(w>h) ",isVertical:false" else ""} })"""
      case Button(name,x,y,w,h) => s"""$name = new Interface.Button({ name:"$name", label:"$name", mode:"momentary", bounds: [$x,$y,$w,$h] })"""
      case XY(name,x,y,w,h) => s"""$name = new Interface.XY({ name:"$name", label:"$name", childWidth:15, numChildren:1, usePhysics:false, bounds: [$x,$y,$w,$h] })"""
      case Value(name,x,y,w,h) => s"""$name = new Interface.Label({ name:"$name", value:0, bounds: [$x,$y,$w,$h], vAlign:"middle", hAlign:"center" })"""

    }.mkString("\n") + "\n" +
    s"panel.add( ${widgets.map(_.name).mkString(",")} )" +
    htmlFooter
  }

    def htmlHeader() = """
<html>
<head>
  <script src="/assets/js/interface.js"></script>
  <script src="/assets/js/interface.client.js"></script>
</head>
<body>
  <script>
  """

  def htmlFooter() = """
  </script>
</body>
</html>
  """

}


