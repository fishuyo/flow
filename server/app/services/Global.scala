package flow

import akka.actor._

object System {
  var system:ActorSystem = _
  def apply() = system
  def update(s:ActorSystem) = system = s
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