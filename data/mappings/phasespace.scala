


// phasespace.Phasespace.disconnect
// phasespace.Phasespace.connect
val ps = new PhasespaceIO

import de.sciss.osc.Message
val osc = new OSCSink
osc.connect("localhost", 9010)

// map pickable events for each glove (right/left)
for(gloveID <- Seq(0,1)){

	//point event
  ps.state.map{ case s =>
    val h = s.headPosition
  	val g = s.gloves(gloveID)
    val d = (g.centroid - h).normalized
    Message("/point", gloveID, h.x,h.y,h.z, d.x,d.y,d.z)
  } >> osc.sink
  
  //map each fingertip pinch events
  for(f <- Led.fingerTips){
		ps.state.map{ case s =>
      val h = s.headPosition
      val g = s.gloves(gloveID)
      val d = (g.centroid - h).normalized
      if(g.isPinchOn(f)){
        Some(Message("/pick", gloveID, f.id, h.x,h.y,h.z, d.x,d.y,d.z))
      }else if(g.isPinched(f)){
        Some(Message("/drag", gloveID, f.id, h.x,h.y,h.z, d.x,d.y,d.z))
      }else if(g.isPinchOff(f)){
        Some(Message("/unpick", gloveID, f.id, h.x,h.y,h.z, d.x,d.y,d.z))
      }
      None
  	}.filter(_.isDefined).map(_.get) >> osc.sink
 	}
  
}




