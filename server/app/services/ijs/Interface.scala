
package flow
package ijs

import java.io.File
import java.io.PrintWriter
import java.io.FileOutputStream

import collection.mutable.ListBuffer

sealed trait Component{ 
  def name:String
  def x:Float
  def y:Float
  def w:Float
  def h:Float
}
case class Slider(name:String,x:Float,y:Float,w:Float,h:Float) extends Component
// case class Button(val name:String, x:Float,y:Float,w:Float,h:Float) extends Component

object Interface {
  def create(name:String) = new Interface(name)
}


class Interface(val name:String) extends IO {

  val components = ListBuffer[Component]()

  def +=(component:Component) = components += component

  def save() = {
    val path = "server/public/interfaces/"
    val pw = new PrintWriter(new FileOutputStream(path + name + ".html", false));
    pw.write(toHtml)
    pw.close
  }

  def toHtml() = {
    Html.header +
    "panel = new Interface.Panel({ useRelativeSizesAndPositions:true })\n" +
    components.map{ 
      case Slider(name,x,y,w,h) => s"""$name = new Interface.Slider({ name:"$name", bounds: [$x,$y,$w,$h] })"""

    }.mkString("\n") + "\n" +
    s"panel.add( ${components.map(_.name).mkString(",")} )" +
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

