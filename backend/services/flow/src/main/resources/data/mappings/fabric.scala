
import de.sciss.osc.Message

val osc = new OSCSink
osc.connect("ar01.1g", 9010)

val abutton = 4L
val grip = 4L
val bbutton = 2L
val trigger = 8589934592L
val touch = 4294967296L
val stick = 4294967296L

val yOffset = -1.5f

val io = new OpenVRIO()


io.right.map{ case c =>
  val p = c.pose.pos;
  val q = c.pose.quat;
  Message("/controller/rightPose", p.x,p.y+yOffset,p.z, q.w,q.x,q.y,q.z)
} >> osc.sink

io.right.map{ case c =>
  val v = c.vel;
  Message("/controller/rightVel", v.x,v.y,v.z)
} >> osc.sink

io.right.map{ case c =>
  val b1 = (c.buttonsTouched & touch) != 0;
  val b2 = (c.buttonsTouched & grip) != 0;
  val b3 = (c.buttonsTouched & trigger) != 0;
  val b4 = (c.buttonsTouched & bbutton) != 0;
  if(b1 && b3) Message("/controller/rightGrab", 1.0f)
  else Message("/controller/rightGrab", 0.0f)
} >> osc.sink

io.right.map{ case c =>
  val b = (c.buttonsPressed & trigger) != 0;
  if(b) Message("/controller/rightTear", 1.0f)
  else  Message("/controller/rightTear", 0.0f)
} >> osc.sink

var tgdown = false
io.right.map{ case c =>
  val b = (c.buttonsPressed & abutton) != 0;
  if(b && !tgdown){
    tgdown = true
    Some(Message("/fabric/toggleGravity"))
  } else if(!b) {
    tgdown = false
    None
  } else {
    None
  }
}.filter(_.isDefined).map(_.get) >> osc.sink

io.right.map{ case c =>
  val b = (c.buttonsPressed & bbutton) != 0;
  if(b) Some(Message("/fabric/reset"))
  else None
}.filter(_.isDefined).map(_.get) >> osc.sink


io.right.map{
  case c =>
    //val pos = c.pose.pos;
    //println(s"${pos.x} ${pos.y} ${pos.z}")
    //if(c.buttonsTouched > 0) println(c.buttonsTouched)
    //for(i <- 0 until 5){
    //  val v = c.analogData(i)
    //  if(v.x != 0f || v.y != 0f)
    //    println(s"axis $i: ${v.x} ${v.y}")
} >> Null


