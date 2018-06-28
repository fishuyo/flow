
package flow

import java.io.File

import collection.mutable.HashMap

object AppManager {

	val appConfigPath = "data/apps/"
	val apps = HashMap[String, AppIO]()

	def getAppList() = apps.values.map(_.config).toSeq

	def apply(name:String) = getOrElseOpen(name)
	
	def getOrElseOpen(name:String) = {
		apps.get(name) match {
			case Some(a) => a 
			case None => val a = open(name); apps(name) = a; a
		}
	}
	

	def open(name:String) = {
		val file = new File(appConfigPath + name + ".json")
		var app = file.exists match {
			case true => AppIO.fromConfigFile(file)
			case false => AppIO(name)
		}
		app
	}

	def close(name:String) = {
		apps.remove(name).foreach(_.close)
		controllers.WebsocketActor.sendAppList()
	}

	def handshakeConfig(config:String, addr:String, port:Int) = {
		val app = AppIO.fromConfig(config)
		val name = app.config.io.name
		apps.remove(name).foreach(_.close)
		apps(name) = app

		app.hostname = addr 
		app.sinkPort = port
		app.connect()
		app.listen()

		app.runDefaultMappings()
		controllers.WebsocketActor.sendAppList()
	}

	def handshake(name:String, addr:String, port:Int) = {
		val app = getOrElseOpen(name)

		app.hostname = addr
		app.sinkPort = port
		app.connect()
		app.listen() 
		
		app.runDefaultMappings()
		
		controllers.WebsocketActor.sendAppList()
	}




}