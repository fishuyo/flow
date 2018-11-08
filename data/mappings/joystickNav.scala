val app = new OSCSink
app.connect("spherez05", 9010)

val joy = Device.joystick(0)
// val app = AppManager("defaultApp")
val v = 0.15f   // move speed
val w = 0.01f  // turn speed
def dd(f:Float) = if(Math.abs(f) < 0.06) 0 else f
joy.leftX.map(2 * _ - 1).map(dd).map(_ * v) >> app.sink("mx")
joy.leftY.map(2 * _ - 1).map(dd).map(_ * -v) >> app.sink("mz")
joy.rightX.map(2 * _ - 1).map(dd).map(_ * -w) >> app.sink("ty")
joy.rightY.map(2 * _ - 1).map(dd).map(_ * -w) >> app.sink("tx")
joy.upAnalog.map(_ * v) >> app.sink("my")
joy.downAnalog.map(_ * -v) >> app.sink("my")
joy.R2Analog.map(_ * v) >> app.sink("my")
joy.L2Analog.map(_ * -v) >> app.sink("my")
joy.R1Analog.map(_ * -w) >> app.sink("tz")
joy.L1Analog.map(_ * w) >> app.sink("tz")
