
package flow

import java.io.File
import java.net.SocketAddress
import java.net.InetSocketAddress

import collection.mutable.HashMap

object AppManager {

	val appConfigPath = "data/apps/"
	val apps = HashMap[String, AppIO]()

	def apply(name:String) = apps(name)

	def handshake(name:String, addr:SocketAddress, port:Int=9010) = {  // TODO split into two functions, one for backwards compat with old DeviceServer
		val file = new File(appConfigPath + name + ".json")
		// println(file.getAbsolutePath)
		var app = file.exists match {
			case true => AppIO.fromConfigFile(file)
			case false => AppIO(name)
		}

		app.hostname = addr.asInstanceOf[InetSocketAddress].getHostName
		app.sinkPort = port
		app.connect()

		app = apps.getOrElseUpdate(name, app)
		app.listen(12000) // XXX 
		
		app.runDefaultMappings()
		
		System().actorSelection("akka://application/user/*/flowActor") ! "sendAppList"
	}


}