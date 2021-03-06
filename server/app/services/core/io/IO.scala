
package flow

import scala.language.dynamics

import com.typesafe.config._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent._

import collection.mutable.HashMap

/**
  * The IO object holds available io nodes, connected devices.. etc.
  */
// object IO {
//   val ios = HashMap[String,IO]()
//   def apply(name:String) = ios(name)
//   def update(name:String, io:IO) = ios(name) = io
// }

/**
  * An IO represents a device or abstract node, that can generate data streams, and/or receive data streams.
  * It provides a wrapper around a set of dynamic streams utilizing BroadcastHubs and MergeHubs
  * The wrapper also provides a custom DSL for mapping IOs to other IOs
  */
trait IO extends Dynamic {

  // For default type inference on selectDynamic
  trait DefaultsTo[Type, Default]
  object DefaultsTo {
    implicit def defaultDefaultsTo[T]: DefaultsTo[T, T] = null
    implicit def fallback[T, D]: DefaultsTo[T, D] = null  
  }

  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  def sources:Map[String,Source[Any,akka.NotUsed]] = Map[String,Source[Any,akka.NotUsed]]()
  def sinks:Map[String,Sink[Any,akka.NotUsed]] = Map[String,Sink[Any,akka.NotUsed]]()

  // def typedSources = Map[String, Map[String, Source[Any,akka.NotUsed]]]()
  
  def source(name:String) = sources.get(name)
  def sink(name:String) = sinks.get(name)

  def selectDynamic[T](name:String)(implicit default:DefaultsTo[T,Float]) = source(name).get.asInstanceOf[Source[T,akka.NotUsed]]

  def destutter = Flow[Float].statefulMapConcat(() => {
    var last:Float = 0f
    elem =>
      if (elem != last) { last = elem; List(elem) }
      else Nil
  })

  def >>(io:IO)(implicit kill:SharedKillSwitch) = {
    if(sources.size > 0){
      sources.foreach { case (name,src) =>
        io.sink(name).foreach { case sink => 
          src.via(kill.flow).runWith(sink)
        }
      }
    } else { // else build from sink, allowing for dynamic source population.. hmm..
      io.sinks.foreach { case (name,sink) =>
        source(name).foreach { case src => 
          src.via(kill.flow).runWith(sink)
        }
      }
    }
  }

}

case class IOSource[T,M](src:Source[T,M]){
  implicit val system = System()
  implicit val materializer = ActorMaterializer()

  // def orLast = src.via(Flow[T].expand(Iterator.continually(_)))
  def orLast(initial:T) = src.via(Flow[T].extrapolate(Iterator.continually(_),Some(initial)))
  def >>[U >: T,N](sink:Sink[U,N])(implicit kill:SharedKillSwitch) = src.via(kill.flow).runWith(sink)
  def >>[U >: T,N](sink:Option[Sink[U,N]])(implicit kill:SharedKillSwitch) = sink match {
    case Some(s) => src.via(kill.flow).runWith(s)
    case None => println("Warning: >> encountered empty sink, check Sink exists and using correct name..")
  }
}


