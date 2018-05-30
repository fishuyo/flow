package flow

import de.sciss.osc.Message

object OSCApi {

  val handler:OSC.OSCHandler = {
    
    case ( Message("/handshake", name:String), addr) => 
      println(s"OSCApi handshake: $name $addr")
      AppManager.handshake(name, addr, 12001)
    case ( Message("/handshake", name:String, port:Int), addr) => 
      println(s"OSCApi handshake: $name $addr $port")
      AppManager.handshake(name, addr, port)
    case ( Message("/handshake", name:String, address:String, port:Int), addr) => 
      println(s"OSCApi handshake: $name $addr $address $port")
      AppManager.handshake(name, address, port)
    
    case ( Message("/disconnectApplication", name:String), addr) => 
      println(s"OSCApi disconnectApplication: $name")
      AppManager.close(name)
      
    case msg => println(msg)
  
  }

  def listen(port:Int) = OSCManager() ! OSCManagerActor.Bind(port, handler)
}
