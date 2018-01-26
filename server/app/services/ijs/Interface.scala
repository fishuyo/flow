
package flow
package ijs

import protocol.IOPort

import java.io.File
import java.io.PrintWriter
import java.io.FileOutputStream

import collection.mutable.ListBuffer

sealed trait Widget{ 
  def name:String
  def x:Float
  def y:Float
  def w:Float
  def h:Float
}
case class Slider(name:String,x:Float,y:Float,w:Float,h:Float) extends Widget
case class Button(name:String, x:Float,y:Float,w:Float,h:Float) extends Widget

object Interface {
  def create(name:String) = new Interface(name)
  
  def fromApp(app:AppIO) = {
    val io = app.config.io
    val ijs = new Interface(io.name)
    var sx = 0f
    var bx = 0f
    io.sinks.foreach {
      case IOPort(name,"float") => ijs += Slider(name,sx,0f,0.1f,0.5f); sx += 0.1f
      case IOPort(name,"bool") => ijs += Button(name,bx,0.55f,0.1f,0.1f); bx += 0.1f
      case IOPort(name,"") => ijs += Button(name,bx,0.55f,0.1f,0.1f); bx += 0.1f
      case _ => ()
    }
    ijs.save()
  }
}


class Interface(val name:String) extends IO {

  val widgets = ListBuffer[Widget]()

  def +=(w:Widget) = widgets += w

  def save() = {
    val path = "server/public/interfaces/"
    val pw = new PrintWriter(new FileOutputStream(path + name + ".html", false));
    pw.write(toHtml)
    pw.close
  }

  def toHtml() = {
    Html.header +
    "panel = new Interface.Panel({ useRelativeSizesAndPositions:true })\n" +
    "panel.background = 'black'\n" +
    widgets.map{ 
      case Slider(name,x,y,w,h) => s"""$name = new Interface.Slider({ name:"$name", label:"$name", bounds: [$x,$y,$w,$h] ${if(w>h) ",isVertical:false" else ""} })"""
      case Button(name,x,y,w,h) => s"""$name = new Interface.Button({ name:"$name", label:"$name", mode:"momentary", bounds: [$x,$y,$w,$h] })"""

    }.mkString("\n") + "\n" +
    s"panel.add( ${widgets.map(_.name).mkString(",")} )" +
    Html.footer
  }

}


object Html {
  def header() = """
<html>
<head>
  <script src="/assets/js/interface.js"></script>
  <script src="/assets/js/interface.client.js"></script>
</head>
<body>
  <script>
  """

  def footer() = """
  </script>
</body>
</html>
  """
}

