
import ijs._

val io = Interface.create("aforest")

case class P(path:String, min:Float=0f, max:Float=1f, kind:String="slider"){
  val group = path.split("/")(0)
}

val params = Seq(
  P("compositor/indexMask"),
  P("compositor/indexA"),
  P("compositor/indexB"),
  P("compositor/streamFloorFade"),

  P("moon/noiseGlowFade "),
  P("moon/noiseGlowR"),
  P("moon/noiseGlowG"),
  P("moon/noiseGlowB"),
  P("moon/moonY"),
  P("moon/moonFade"),

  P("field/edge"),
  P("field/ripple"),
  P("particles/numAttractors"),
  P("particles/attractorStrength"),
  P("fireflies/fade"),

  P("video/path1"),
  P("video/path2"),
  P("video/path3"),
  P("video/path4"),

  P("video360/path"),

  P("streamDiffusion/promptIndex"),
  P("streamDiffusion/source"),

  P("wave/rain"),

  P("noiseGen/bgNoise"),
  P("noiseGen/bgColor"),
  P("noiseGen/bgFade"),
  P("noiseGen/fgNoise"),
  P("noiseGen/fgColor"),
)

val paramCols = Layout.H(0f,0f,1f,0.8f)

// Group params by path and add to columns
params.groupBy(_.group).map { 
  case (gname, ps) =>
    val col = Layout.V(mh=0.1f)
    col += Label(gname, value = gname)
    ps.foreach { 
      case p =>
        val slider = Slider(p.path)
        col += slider
    }
    paramCols += col
}

io += paramCols

io.addWidgetsFromLayouts()
io.save()
//io.sync()

io.sources("wave/rain") >> io.sinks("wave")

// also make an OSCSink and stream io over osc
val osc = new OSCSink
osc.connect("localhost", 10000)
io >> osc







