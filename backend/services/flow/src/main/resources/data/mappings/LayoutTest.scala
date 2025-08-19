
import ijs._

// Create an interface.js io named test
val io = Interface.create("layout")

val h = Layout.H()
val v1 = Layout.V()
val v2 = Layout.V()
val v3 = Layout.V()

io += h
h += v1
h += v2
h += v3

// Add 10 sliders
val ns = 10
for(i <- 0 until ns){
  v1 += Slider(s"s1_$i")
  v2 += Slider(s"s2_$i")
  v3 += Slider(s"s3_$i")
}

// Generate interface.js html file
// now available at --> localhost:9000/ijs/test.html
io.save()
io.sync()

// also make an OSCSink and stream io over osc
val osc = new OSCSink
osc.connect("localhost", 8000)
io >> osc


