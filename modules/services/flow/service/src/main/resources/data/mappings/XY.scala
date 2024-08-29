
import ijs._

// Create an interface.js io named test
val io = Interface.create("xy")

//io += Slider(s"s$i", i*(0.5/ns), 0, 0.5/ns, 0.5)
//io += Slider("h1", x=0, y=0.5, w=0.5, h=0.25, min=0.0, max=10.0)
// io += Button(s"b$x$y", 0.5+x*(0.5/nx), 0.5+y*(0.5/ny), 0.5/nx, 0.5/ny)

// 2D slider
io += XY("xy",0.05,0.05,0.4,0.95)
io += XY("xz",0.55,0.05,0.4,0.95)

// Generate interface.js html file
// now available at --> localhost:9000/interfaces/test.html
io.save()
io.sync()

// stream interface.js io to itself
// effectively synchronizing multiple instances of the interface
// io >> io

// also make an OSCSink and stream io over osc
val osc = new OSCSink
osc.connect("localhost", 8000)
io >> osc



