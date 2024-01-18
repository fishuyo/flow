package flow

import org.apache.pekko.actor._

object System {
  var system:ActorSystem = _
  def apply() = system
  def update(s:ActorSystem) = system = s

  def address = System().asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress

  def broadcast(msg:Any) = apply().actorSelection("/user/live.*") ! msg
  def send(name:String, msg:Any) = apply().actorSelection(s"/user/live.$name.*") ! msg
  def broadcastRemote(a:Address)(msg:Any) = apply().actorSelection(s"$a/user/live.*") ! msg
  def sendRemote(a:Address)(name:String, msg:Any) = apply().actorSelection(s"$a/user/live.$name.*") ! msg
  def broadcastRemotes(as:Seq[Address])(msg:Any) = as.foreach{ case a => apply().actorSelection(s"$a/user/live.*") ! msg}
  def sendRemotes(as:Seq[Address])(name:String, msg:Any) = as.foreach{ case a => apply().actorSelection(s"$a/user/live.$name.*") ! msg}
  // def broadcastAll(msg:Any) = remotes.foreach{ case a => apply().actorSelection(s"$a/user/live.*") ! msg }
  // def sendAll(name:String, msg:Any) = remotes.foreach{ case a => apply().actorSelection(s"$a/user/live.$name.*") ! msg }

}



object Hack{
  // HACK: adds dir to load library path
  def unsafeAddDir(dir: String) = try {
    val field = classOf[ClassLoader].getDeclaredField("usr_paths")
    field.setAccessible(true)
    val paths = field.get(null).asInstanceOf[Array[String]]
    if(!(paths contains dir)) {
      field.set(null, paths :+ dir)
      java.lang.System.setProperty("java.library.path",
       java.lang.System.getProperty("java.library.path") +
       java.io.File.pathSeparator +
       dir)
    }
  } catch {
    case _: IllegalAccessException =>
      sys.error("Insufficient permissions; can't modify private variables.")
    case _: NoSuchFieldException =>
      sys.error("JVM implementation incompatible with path hack")
  }
}