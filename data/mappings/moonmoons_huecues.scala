
import HueProtocol._
// import scala.concurrent.ExecutionContext.Implicits.global

val recv = new OSCSource
recv.listen(8080)

// val time = System().scheduler
Schedule.clear

// 1  4  2
// 10 9  3
// 8  6  11
val (l00, l10, l20, l01, l11, l21, l02, l12, l22) = (1,10,8,4,9,6,2,3,11)
val (b0, b1, l0) = (5,7,7)

val on = SetLightState(on=Some(true), transitiontime=Some(0))
val off = SetLightState(on=Some(false), transitiontime=Some(0))
val fadein = SetLightState(on=Some(true), bri=Some(200), transitiontime=Some(50))
val fadeout = SetLightState(on=Some(true), bri=Some(0), transitiontime=Some(50))

val test = SetLightState(on=Some(true), bri=Some(200), hue=Some(12000), transitiontime=Some(50))

recv.off >> Sink.foreach((e:Any) => { Hue.setGroupState(0,off) })
recv.fadein >> Sink.foreach((e:Any) => { Hue.setGroupState(0,fadein) })
recv.fadeout >> Sink.foreach((e:Any) => { Hue.setGroupState(0,fadeout) })

recv.test >> Sink.foreach((e:Any) => { 
  Hue.setLightState(l0, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some(0)))
})


recv.q0 >> Sink.foreach((e:Any) => { 
  val d = 5
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
      Hue.setLightState(b0, SetLightState(on=Some(true), bri=Some(200), hue=Some(0), transitiontime=Some((d*10).toInt)))
      Hue.setLightState(l11, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
      Hue.setLightState(l21, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
  }
  Schedule.after(4*d seconds){
      Hue.setLightState(l22, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
      // Hue.setLightState(b0, SetLightState(on=Some(false), bri=Some(0), hue=Some(7000), transitiontime=Some((d*10).toInt)))
  }
})
  
  



