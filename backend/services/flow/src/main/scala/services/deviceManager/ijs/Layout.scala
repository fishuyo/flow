package flow
package ijs

import collection.mutable.ListBuffer

object Layout {
  def V(x:Float=0f, y:Float=0f, w:Float=1f, h:Float=1f, mw:Float=1f, mh:Float=1f) = new VerticalLayout{lx=x; ly=y; lw=w; lh=h; maxw=mw; maxh=mh}
  def H(x:Float=0f, y:Float=0f, w:Float=1f, h:Float=1f, mw:Float=1f, mh:Float=1f) = new HorizontalLayout{lx=x; ly=y; lw=w; lh=h; maxw=mw; maxh=mh}
  def G(x:Float=0f, y:Float=0f, w:Float=1f, h:Float=1f, nx:Int=0, ny:Int=0, mw:Float=1f, mh:Float=1f) = new GridLayout(nx,ny){lx=x; ly=y; lw=w; lh=h; maxw=mw; maxh=mh}
}
sealed trait Layout {
  var (pl,pr,pt,pb) = (0f,0f,0f,0f)
  var (lx,ly,lw,lh) = (0f,0f,1f,1f)
  var (maxw, maxh) = (1f,1f)
  val layouts = ListBuffer[Layout]()

  def +=(w:Widget) = layouts += new SingleLayout(w)
  def +=(l:Layout) = layouts += l

  def resizeChildren():Unit = {}
  def addWidgets(widgets:ListBuffer[Widget]):Unit = {
    layouts.foreach { 
      case l:SingleLayout => widgets += l.widget
      case l => l.addWidgets(widgets)
    }
  }
  // = this match {
  //   groups.foreach { 
  //     case g:SGroup => 
  //   }
  // }
}

class SingleLayout(var widget:Widget) extends Layout {
  override def resizeChildren() = widget match {
    case w:Slider => widget = w.copy(x=this.lx,y=this.ly,w=this.lw,h=this.lh)
    case w:Button => widget = w.copy(x=this.lx,y=this.ly,w=this.lw,h=this.lh)
    case w:XY => widget = w.copy(x=this.lx,y=this.ly,w=this.lw,h=this.lh)
    case w:Label => widget = w.copy(x=this.lx,y=this.ly,w=this.lw,h=this.lh)
    case w:RangeSlider => widget = w.copy(x=this.lx,y=this.ly,w=this.lw,h=this.lh)
    case w:Menu => widget = w.copy(x=this.lx,y=this.ly,w=this.lw,h=this.lh)
    case _ => ()
  }
}
class HorizontalLayout extends Layout {
  override def resizeChildren() = {
    var dw = lw / layouts.size
    if(dw > maxw) dw = maxw
    layouts.zipWithIndex.foreach { case (l,i) =>
      l.lx = lx + dw*i
      l.ly = ly
      l.lw = dw
      l.lh = lh
      l.resizeChildren()
    }
  }
}
class VerticalLayout extends Layout {
  override def resizeChildren() = {
    var dh = lh / layouts.size
    if(dh > maxh) dh = maxh
    layouts.zipWithIndex.foreach { case (l,i) =>
      l.lx = lx
      l.ly = ly + dh*i
      l.lw = lw
      l.lh = dh
      l.resizeChildren()
    }
  }
}
class GridLayout(var nx:Int=0, var ny:Int=0) extends Layout {
  override def resizeChildren() = {
    val n = layouts.size
    if(nx == 0 && ny == 0){ 
      ny = math.round(math.sqrt(n)).toInt
      nx = math.ceil(n*1f/ny).toInt
    }
    val nw = lw / nx
    val nh = lh / ny
    layouts.zipWithIndex.foreach { case (l,i) =>
      val ix = i % nx
      val iy = i / nx
      l.lx = lx + nw*ix
      l.ly = ly + nh*iy
      l.lw = nw
      l.lh = nh
      l.resizeChildren()
    }
  }
}