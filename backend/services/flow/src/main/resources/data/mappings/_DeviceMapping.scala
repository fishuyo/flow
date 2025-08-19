
// DeviceIO for first ps3 controller
val joy = Device("PLAYSTATION(R)3 Controller", 0)

// Print a couple Sources
joy.leftX >> Print
joy.L2 >> Print

// Map buttons to own sinks
joy.triangleAnalog >> joy.sinks("leftRumble")
joy.circle >> joy.sinks("rightRumble")
joy.X >> joy.sinks("led1")