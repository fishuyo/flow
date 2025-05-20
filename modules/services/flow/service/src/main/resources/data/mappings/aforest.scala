
import ijs._

val io = Interface.create("aforest")

case class P(path:String, min:Float=0f, max:Float=1f, kind:String="slider", var group:String=""){
  if(group.isEmpty) group = path.split("/")(0)
}

val params = Seq(
  P("compositor/maskCenter"),
  P("compositor/maskTitan1"),
  //P("compositor/maskClean"),
  P("compositor/maskMoon"),
  P("compositor/maskNoise1"),
  P("compositor/maskNoise2"),
  P("compositor/maskPortals"),
  P("compositor/brainFade"), 
  P("compositor/rockFade"),  
  
  P("video360/path", 0, 14, group="video"),
  P("compositor/vid360/A", group="video"),
  P("compositor/vid360/B", group="video"),
  P("compositor/edg360/A", group="video"),
  P("compositor/edg360/B", group="video"),

  //P("compositor/vidtile/A", group="video"),
  //P("compositor/vidtile/B", group="video"),
  //P("compositor/edgtile/A", group="video"),
  //P("compositor/edgtile/B", group="video"),
  P("compositor/vidportals/A", group="video"),
  P("compositor/vidportals/B", group="video"),
  P("compositor/vidportals2/A", group="video"),
  P("compositor/vidportals2/B", group="video"), 
  //P("video/pathGroup1", 0, 10),
  //P("video/pathGroup2", 0, 10),
  //P("video/path1", 0, 61),
  //P("video/path2", 0, 61),
  //P("video/path3", 0, 61),
  //P("video/path4", 0, 61),

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
  P("field/rippleSrc", 0, 1, group="particles"),
  P("field/wind", 0, 2, group="particles"),
  P("particles/numAttractors", 0, 40),
  P("particles/attractorStrength", 0, 0.5),
  P("fireflies/fade", group="particles"),
  
  P("compositor/moon/A", group="moon"),
  P("compositor/moon/B", group="moon"),
  P("moon/noiseGlowFade"),
  P("moon/noiseGlowR"),
  P("moon/noiseGlowG"),
  P("moon/noiseGlowB"),
  P("moon/noiseGlowC"),
  P("moon/moonY", -2.1f, 2.1f),
  P("moon/moonFade"),

  P("noiseGen/bgNoise", 0, 3),
  P("noiseGen/bgColor", 0, 4),
  P("noiseGen/bgFade"),
  P("noiseGen/fgNoise", 0, 3),
  P("noiseGen/fgColor", 0, 4),
  P("noiseGen/fgNoise1", 0, 3),
  P("noiseGen/fgColor1", 0, 4),
  P("noiseGen/fgNoise2", 0, 3),
  P("noiseGen/fgColor2", 0, 4),
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
io += Label("timecode", 0.4, 0.835, 0.2, 0.03, "00:00:00")
io += Label("keyframes", 0.01, 0.82, 0.98, 0.05, "")


val editGrid = Layout.G(0.65,0.88,0.3,0.1,3,2)
editGrid += Button("param/syncAudio", mode="toggle")
editGrid += Button("param/rmKey")
editGrid += Button("param/snap")

editGrid += Button("param/editmode", mode="toggle")
editGrid += Button("param/addKey")
editGrid += Button("param/snapInsert")
io += editGrid

val playControls = Layout.G(0.05, 0.88, 0.3, 0.1, 2, 2)
playControls += Button("param/minus10")
playControls += Button("param/add10")
playControls += Button("param/prevKeyframe")
playControls += Button("param/nextKeyframe")
io += playControls



val presetGrid = Layout.G(0.35,0.89,0.3,0.05,3,1)
presetGrid += Button("param/rockSetup")
presetGrid += Button("param/intromode")
presetGrid += Button("param/playmode", mode="toggle")

//for(i <- 1 to 10){
//  presetGrid += Button(s"p$i")
//}
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




