
import ijs._

// Create an interface.js io named test
val io = Interface.create("test")

// Add 20 sliders
val ns = 20
for(i <- 0 until ns)
  io += Slider(s"s$i", i*(1f/ns), 0, 1f/ns, 0.5)

// Add 2 more sliders
io += Slider("h1", x=0, y=0.5, w=0.5, h=0.25, min=0.0, max=10.0)
io += RangeSlider("h2",0,0.75,0.5,0.25)

// Add a 3 by 3 grid of buttons
val (nx,ny) = (3,3)
for(x <- 0 until nx; y <- 0 until ny)
  io += Button(s"b$x$y", 0.5+x*(0.5/nx), 0.5+y*(0.5/ny), 0.5/nx, 0.5/ny)

// 2D slider
// io += XY("xy",0.5,0.5,0.5,0.5)

// Generate interface.js html file
// now available at --> localhost:9000/ijs/test.html
io.save()

// stream interface.js io to itself
// effectively synchronizing multiple instances of the interface
// io >> io

// also make an OSCSink and stream io over osc
val osc = new OSCSink
osc.connect("localhost", 8000)
io >> osc


// Create an interface.js from an AppIO
// val app = AppManager("defaultApp")
// val io2 = Interface.fromApp(app)
// io2 >> app
// io2 >> io2

