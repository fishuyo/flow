
  val joy = Device.joystick(0)
  val app = AppManager("defaultApp")
  val v = 0.15f   // move speed
  val w = 0.01f  // turn speed

  def dd(f:Float) = if(math.abs(f) < 0.06) 0 else f

  joy.leftX.map(2 * _ - 1).map(dd).map(_ * v) >> app.sinks("mx")
  joy.leftY.map(2 * _ - 1).map(dd).map(_ * -v) >> app.sinks("mz")
  joy.rightX.map(2 * _ - 1).map(dd).map(_ * -w) >> app.sinks("ty")
  joy.rightY.map(2 * _ - 1).map(dd).map(_ * -w) >> app.sinks("tx")

  joy.upAnalog.map(_ * v) >> app.sinks("my")
  joy.downAnalog.map(_ * -v) >> app.sinks("my")
  joy.R2Analog.map(_ * v) >> app.sinks("my")
  joy.L2Analog.map(_ * -v) >> app.sinks("my")

  joy.R1Analog.map(_ * -w) >> app.sinks("tz")
  joy.L1Analog.map(_ * w) >> app.sinks("tz")

  joy.select >> app.sinks("halt")

