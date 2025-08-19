
// openvr test


val io = new OpenVRIO()

io.controllers.map(_.foreach{
  case c =>
    val pos = c.pose.pos;
    //println(s"${pos.x} ${pos.y} ${pos.z}")
    //println(s"${c.buttonsTouched}")
    //println(s"${c.buttonsPressed}")
    for(i <- 0 until 5){
      val v = c.analogData(i)
      if(v.x != 0f || v.y != 0f)
        println(s"axis $i: ${v.x} ${v.y}") 
    }
}) >> Null
