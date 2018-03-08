

# Flow (the new Device Server) Workshop

Flow lets one create mappings between data streams of various [devices / interfaces] and [applications] over OpenSoundControl. Flow provides a web browser based interface for seeing available devices and implementing mapping scripts via a simplified [scala](https://www.scala-lang.org/) based [reactive stream](http://www.reactive-streams.org/) DSL built using [akka-stream](https://doc.akka.io/docs/akka/2.5.5/scala/stream/index.html).

## Getting started / Installation
### Requirements
- A jdk (java development kit). Recommended [Oracle Jdk 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- sbt (simple build tool) - [Install instruction](https://www.scala-sbt.org/download.html)
- get Flow - [https://github.com/fishuyo/flow.git](https://github.com/fishuyo/flow.git)

### Build / Run Flow server
```
git clone https://github.com/fishuyo/flow.git
cd flow
sbt run
```
- sbt will fetch needed dependencies into the build and start the flow server. (first run will take a couple minutes..)
- open a web browser and go to [http://localhost:9000](http://localhost:9000)
- by default, it will be running in development mode, where project source code changes are detected and recompiled when you refresh the browser, so this will trigger a compilation step the first time you load the page.

## My First Mapping
- in the left side bar, expand the "Mappings" dropdown and click on "\_MyFirstMapping"
- this will load the code for "\_MyFirstMapping" into the code editor
- press Cmd-Enter to compile and run the mapping script.
- this example demonstrates generated data streams sent to the Print object which prints whatever it gets to the console.
- try breaking the example by mashing the keyboard, and re-run with Cmd-Enter
- you should see a red dot by the line of code signaling an error in the script
- mouse over the red dot to see the associated error message

## About Mappings and IOs
- every mapping maps a stream from a Source (generates data) to a Sink (consumes data)
- the ">>" operator in the Flow DSL "materializes" the data flow from source to sink
- IOs are a generalization of anything with a list of named Sources and Sinks
- HID Devices are IOS and provide Sources for each button or joystick axis, and Sinks for parameters sent to the device such as LEDs or force feedback.
- the ">>" operator can be used to map an IO to another IO, in this case materializing multiple streams from Sources in the first IO to Sinks in the second IO with corresponding names and types.

## Device IOs
- if an implemented device is connected it will show up in the "Connected Devices" dropdown click on it to reveal its available Sources and Sinks.
- See the "\_DeviceMapping" example.

## OSC Sink
- the OSC Sink is an easy way to send stream data over OSC
- See the "\_OSCSink" example

## Interface.js IOs
- See "\_ijsTest" example

## App IOs and allolib
- App IOs require a config file to specify available Sources and Sinks for an "app"
- This config file can be sent through an OSC handshake from the app
- allolib's App class utilizes a default configuration to handshake and enable default joystick navigation mappings see "joystickNav" mapping.
- To customize your App's Sources and Sinks you can override the default config parameter and implement your own OSC handler.
- Check out allolib's example in "allolib/examples/util/customDeviceServerApp.cpp"


## other resources
- [Scala school](https://twitter.github.io/scala_school/)

## todos
- implement IOs for: apple trackpad, leap, kinect, phasespace, vrpn, MIDI
- IO types Float, Int, String, Vec3f, etc.
- Guiding implementation of additional hid devices
- better Interface.js support
- ui changes:
	- Connected / Non connected devices / Unimplemented devices
	- Show device sinks
	- delete, rename mappings
	- mapping folders?
	- Stream Watch in browser
	- Console view in browser