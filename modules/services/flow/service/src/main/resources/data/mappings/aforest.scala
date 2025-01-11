
import ijs._

val io = Interface.create("aforest")

case class P(path:String, min:Float=0f, max:Float=1f, kind:String="slider", var group:String=""){
  if(group.isEmpty) group = path.split("/")(0)
}

val params = Seq(
  P("compositor/indexMask", 0, 10),
  //P("compositor/indexA", 0, 10),
  //P("compositor/indexB", 0, 10),
  P("compositor/brainFade"),
  
  P("video360/path", 0, 10, group="video"),
  P("compositor/vid360/A", group="video"),
  P("compositor/vid360/B", group="video"),
  P("compositor/edg360/A", group="video"),
  P("compositor/edg360/B", group="video"),

  P("compositor/vidtile/A", group="video"),
  P("compositor/vidtile/B", group="video"),
  P("compositor/edgtile/A", group="video"),
  P("compositor/edgtile/B", group="video"),
  P("compositor/vidportals/A", group="video"),
  P("compositor/vidportals/B", group="video"), 
  //P("video/path1"),
  //P("video/path2"),
  //P("video/path3"),
  //P("video/path4"),

  P("streamDiffusion/promptIndex", 0, 14),
  P("streamDiffusion/source", 0, 3),
  P("wave/rain", group="streamDiffusion"),
  P("compositor/streamFloor/A", group="streamDiffusion"),
  P("compositor/streamFloor/B", group="streamDiffusion"),
  P("compositor/streamFull/A", group="streamDiffusion"),
  P("compositor/streamFull/B", group="streamDiffusion"),
  P("compositor/streamportals/A", group="streamDiffusion"),
  P("compositor/streamportals/B", group="streamDiffusion"),
  //P("compositor/streamFloorFade"),

  P("compositor/particles/A", group="particles"),
  P("compositor/particles/B", group="particles"),
  P("field/edge", 0 , 2, group="particles"),
  P("field/ripple", 0, 2, group="particles"),
  P("particles/numAttractors", 0, 40),
  P("particles/attractorStrength", 0, 1),
  P("fireflies/fade", group="particles"),
  
  P("compositor/moon/A", group="moon"),
  P("compositor/moon/B", group="moon"),
  P("moon/noiseGlowFade"),
  P("moon/noiseGlowR"),
  P("moon/noiseGlowG"),
  P("moon/noiseGlowB"),
  P("moon/moonY", -2.1f, 2.1f),
  P("moon/moonFade"),

  P("noiseGen/bgNoise", 0, 3),
  P("noiseGen/bgColor", 0, 4),
  P("noiseGen/bgFade"),
  P("noiseGen/fgNoise", 0, 3),
  P("noiseGen/fgColor", 0, 4),
)

val paramCols = Layout.H(0.01f,0f,0.98f,0.755f)

// Group params by path and add to columns
params.groupBy(_.group).map { 
  case (gname, ps) =>
    val col = Layout.V(mh=0.1f)
    col += Label(gname, value = gname)
    ps.foreach { 
      case p =>
        val slider = Slider(p.path, min=p.min, max=p.max)
        col += slider
    }
    paramCols += col
}

io += paramCols
io += Slider("param/time", 0.01, 0.76, 0.98, 0.07, 0, 2400)
io += Label("timecode", 0.4, 0.835, 0.2, 0.04, "00:00:00")
io += Label("keyframes", 0.01, 0.82, 0.98, 0.05, "")


val editGrid = Layout.G(0,0.9,0.3,0.1,3,2)
editGrid += Button("param/playmode", mode="toggle")
editGrid += Button("param/editmode", mode="toggle")
editGrid += Button("param/addKey")
editGrid += Button("param/rmKey")
editGrid += Button("param/snap")
editGrid += Button("param/snapInsert")
io += editGrid

val playControls1 = Layout.H(0.35,0.89,0.3,0.05)
val playControls2 = Layout.H(0.35,0.94,0.3,0.05)
playControls2 += Button("param/prevKeyframe")
playControls1 += Button("param/minus10")
//playControls1 += Button("param/play", mode="toggle")
playControls1 += Button("param/add10")
playControls2 += Button("param/nextKeyframe")
io += playControls1
io += playControls2

val presetGrid = Layout.G(0.7,0.9,0.3,0.1,5,2)
for(i <- 1 to 10){
  presetGrid += Button(s"p$i")
}
io += presetGrid



io.addWidgetsFromLayouts()
io.save()
//io.sync()

// map time in seconds to timecode label
//io.sources("param/time").map{ case t:Float => 
//  val m = (t / 60.0f).toInt
//  val s = (t % 60.0f).toInt
//  f"00:$m%02d:$s%02d"
//} >> io.sinks("timecode")

// also make an OSCSink and stream io over osc
val osc = new OSCSink
osc.connect("localhost", 8000)
//osc.connect("192.168.1.104", 10000)
io >> osc


val recv = new OSCSource
recv.listen(9090)
recv >> io




