

val osc = new OSCSink
osc.connect("localhost", 8000)
osc.prefix = "/ps3"             // prepended to OSC address

// DeviceIO for first ps3 controller
val joy = Device("PLAYSTATION(R)3 Controller", 0)

// Stream each joystick Source over OSC with addresses --> osc.prefix + "/" + [source name]  
joy >> osc
