
// Mapping a single value
Source.single("Catfood!") >> Print

// Mapping a sequence of values
Source(1 to 10) >> Print

// Map and filter data!
Source(1 to 10).map( _ * 2).filter(_ > 15).map("Map and filter stream: " + _) >> Print

// Combine data streams!
val s1 = Source(0 to 8 by 2)
val s2 = Source(1 to 9 by 2)
s1.zip(s2).map{ case (a,b) => (-a,b,b-a) } >> Print


// you can write any valid scala code
def doFun(i:Int) = {
  (0 until i).foreach{ case i => println(math.cos(i)) }
  "The Salmon is the strongest and most handsome fish." // last line returned from function
}

Source(1 to 3).map(doFun) >> Print