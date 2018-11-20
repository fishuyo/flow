


// Phasespace.disconnect
Phasespace.connect
val ps = new PhasespaceIO

import de.sciss.osc.Message
val osc = new OSCSink
osc.connect("spherez05", 9010)

// map pickable events for each glove (right/left)
for(gloveID <- Seq(1)){

	//point event
  ps.state.map{ case s =>
    val h = s.headPosition
  	val g = s.gloves(gloveID)
    val d = (g.centroid - h).normalized
    val r = Intersect.ray2Allosphere(Ray(h,d))
    println(r)
    Message("/point", gloveID, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z)
  } >> osc.sink
  
  //map each fingertip pinch events
  for(f <- Led.fingerTips){
		ps.state.map{ case s =>
      val h = s.headPosition
      val g = s.gloves(gloveID)
      val d = (g.centroid - h).normalized
      val r = Intersect.ray2Allosphere(Ray(h,d))
      if(g.isPinchOn(f)){
        Some(Message("/pick", gloveID, f.id, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z))
      }else if(g.isPinched(f)){
        val m = g.getPinchTranslation(f)
        f match {
        	// case Led.Index => Some(Message("/translate", gloveID, f.id, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z, m.x,m.y,m.z))
          case Led.Middle => Some(Message("/rotate", gloveID, f.id, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z, m.x,m.y,m.z))
          case Led.Ring => 
          	m *= 0.1f	
            Some(Message("/scale", gloveID, f.id, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z, m.x,m.y,m.z))
        	case Led.Pinky => None
        	case Led.Index => 
          	m *= (30f*(s.rightGlove.centroid - s.leftGlove.centroid).mag())
          	Some(Message("/translate", gloveID, 0, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z, m.x,m.y,m.z))
        }
        
      }else if(g.isPinchOff(f)){
        Some(Message("/unpick", gloveID, f.id, r.o.x,r.o.y,r.o.z, r.d.x,r.d.y,r.d.z))
      }else None
  	}.filter(_.isDefined).map(_.get) >> osc.sink
 	}
  
}




