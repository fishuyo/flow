
import HueProtocol._

val recv = new OSCSource
recv.listen(8071)

Schedule.clear

// translab Light ids
// 1  4  2
// 10 9  3
// 8  6  11 (piano) 5
// (door)

val (l00, l10, l20, l01, l11, l21, l02, l12, l22) = (1,10,8,4,9,6,2,3,11)
val (b0, b1, l0) = (5,7,12)
val overheadlights = List(1,10,8,4,9,6,2,3,11)

val rlight = Random.oneOf(overheadlights : _*)
val rhue = Random.int(0,65535)

var rls = scala.collection.mutable.LinkedHashSet[Int]()
def rulight() = {
  if(rls.isEmpty){
    while(rls.size < overheadlights.size)
    	rls = rls + rlight()
  }
  val r = rls.head
  rls = rls.tail
  r
}

recv.lightsoff >> Sink.foreach((e:Any) => { Hue.setGroupState(0, SetLightState(on=Some(false), transitiontime=Some(0))) })
recv.lightson >> Sink.foreach((e:Any) => { Hue.setGroupState(0, SetLightState(on=Some(true), transitiontime=Some(0))) })
recv.lightsup >> Sink.foreach((e:Any) => { Hue.setGroupState(0, SetLightState(on=Some(true), bri=Some(200), transitiontime=Some(50))) })
recv.lightsdown >> Sink.foreach((e:Any) => { Hue.setGroupState(0, SetLightState(on=Some(false), bri=Some(0), transitiontime=Some(50))) })

recv.reset >> Sink.foreach((e:Any) => { Hue.setGroupState(0, SetLightState(on=Some(true), bri=Some(255), ct=Some(366), transitiontime=Some(50))) })

recv.aaron1 >> Sink.foreach((e:Any) => { 
  val d = 15
  Hue.setLightState(l00, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
  Schedule.after(d seconds){
  	Hue.setLightState(l10, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
  	Hue.setLightState(l01, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
    Hue.setLightState(l00, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
  }
  Schedule.after(2*d seconds){
    Hue.setLightState(l11, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
    Hue.setLightState(l21, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
    Hue.setLightState(l10, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
    Hue.setLightState(l01, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
  }
  Schedule.after(3*d seconds){
      Hue.setLightState(l22, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
      Hue.setLightState(l11, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
      Hue.setLightState(l21, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
  }
  Schedule.after(4*d seconds){
      Hue.setLightState(l22, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
      Hue.setLightState(b0, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
      Hue.setLightState(b1, SetLightState(on=Some(true), bri=Some(200), hue=Some(32000), transitiontime=Some((d*10).toInt)))
      // Hue.setLightState(b0, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
  }
})

recv.rocklight >> Sink.foreach((e:Any) => { Hue.setLightState(l01, SetLightState(on=Some(true), bri=Some(150), hue=Some(55000), transitiontime=Some(50))) })
recv.floormap >> Sink.foreach((e:Any) => { Hue.setLightState(l01, SetLightState(on=Some(true), bri=Some(0), ct=Some(366), transitiontime=Some(50))) })
recv.test >> Sink.foreach((e:Any) => { Hue.setLightState(b0, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some(10))) })

recv.randomlightfade >> Sink.foreach((e:Any) => { 
  val d = e.asInstanceOf[Float] / 1000.0
  val l = rulight()
  val h = rhue()
  Hue.setLightState(l, SetLightState(on=Some(true), bri=Some(180), hue=Some(h), transitiontime=Some((d*10).toInt)))
  Schedule.after(d seconds){
  	Hue.setLightState(l, SetLightState(on=Some(true), bri=Some(0), hue=Some(h), transitiontime=Some((d*10).toInt)))
  }
})

var ready = 2
recv.randomlightfade2 >> Sink.foreach((e:Any) => { 
  val d = e.asInstanceOf[Float] / 1000.0
  val l = rulight()
  val h = rhue()
  if(ready > 0){
    ready -= 1
    Hue.setLightState(l, SetLightState(on=Some(true), bri=Some(128), hue=Some(h), transitiontime=Some((0).toInt)))
    Schedule.after(5 millis){
      Hue.setLightState(l, SetLightState(on=Some(true), bri=Some(0), hue=Some(h), transitiontime=Some((d*10).toInt)))
    }
    Schedule.after(1 second){ ready += 1 }
  }
})

recv.transition >> Sink.foreach((e:Any) => { 
  Schedule.after(1 second){ 
    Hue.setGroupState(0, SetLightState(on=Some(false), bri=Some(0), transitiontime=Some(90))) 
  } 
  Schedule.after(15 second){ 
    Hue.setLightState(l10, SetLightState(on=Some(true), bri=Some(100), hue=Some(32000), transitiontime=Some(50))) 
    Hue.setLightState(l11, SetLightState(on=Some(true), bri=Some(100), hue=Some(55000), transitiontime=Some(50))) 
  } 
})
  


