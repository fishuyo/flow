// package flow

// import scala.language.dynamics
// import scala.util.{Try, Success, Failure}

// import org.apache.pekko._
// import org.apache.pekko.actor._
// import org.apache.pekko.stream._
// import org.apache.pekko.stream.scaladsl._
// import scala.concurrent._

// import collection.mutable.HashMap

// // Type-safe data types for IO elements
// sealed trait IOData
// case class FloatData(value: Float) extends IOData
// case class IntData(value: Int) extends IOData
// case class BooleanData(value: Boolean) extends IOData
// case class StringData(value: String) extends IOData
// case class Vector3Data(x: Float, y: Float, z: Float) extends IOData

// // Typed source and sink definitions
// case class IOSource[T](name: String, source: Source[T, NotUsed])
// case class IOSink[T](name: String, sink: Sink[T, NotUsed])

// // Connection result for error handling
// sealed trait ConnectionResult
// case object Connected extends ConnectionResult
// case class ConnectionError(message: String) extends ConnectionResult
// case class MissingEndpoint(name: String, isSource: Boolean) extends ConnectionResult

// /**
//  * Improved IO trait with better type safety and error handling
//  */
// trait IO extends Dynamic {
  
//   implicit val system: ActorSystem = System()
//   implicit val materializer: ActorMaterializer = ActorMaterializer()

//   // Type-safe sources and sinks
//   def sources: Map[String, Source[IOData, NotUsed]] = Map.empty
//   def sinks: Map[String, Sink[IOData, NotUsed]] = Map.empty
  
//   // Typed accessors for common data types
//   def floatSource(name: String): Option[Source[Float, NotUsed]] = 
//     sources.get(name).map(_.map(_.asInstanceOf[FloatData].value))
  
//   def booleanSource(name: String): Option[Source[Boolean, NotUsed]] = 
//     sources.get(name).map(_.map(_.asInstanceOf[BooleanData].value))
  
//   def vector3Source(name: String): Option[Source[Vector3Data, NotUsed]] = 
//     sources.get(name).map(_.map(_.asInstanceOf[Vector3Data]))
  
//   // Safe dynamic access with error handling
//   def selectDynamic[T](name: String): Try[Source[T, NotUsed]] = {
//     sources.get(name) match {
//       case Some(source) => 
//         Try(source.asInstanceOf[Source[T, NotUsed]])
//       case None => 
//         Failure(new NoSuchElementException(s"Source '$name' not found"))
//     }
//   }
  
//   // Improved connection operators
//   def connectTo(io: IO)(implicit kill: SharedKillSwitch): ConnectionResult = {
//     val connections = for {
//       (name, source) <- sources
//       sink <- io.sinks.get(name)
//     } yield {
//       source.via(kill.flow).runWith(sink)
//       Connected
//     }
    
//     if (connections.isEmpty) {
//       ConnectionError("No matching source-sink pairs found")
//     } else {
//       Connected
//     }
//   }
  
//   def connectFrom(io: IO)(implicit kill: SharedKillSwitch): ConnectionResult = {
//     io.connectTo(this)
//   }
  
//   // Bidirectional connection (original >> behavior but clearer)
//   def bidirectional(io: IO)(implicit kill: SharedKillSwitch): ConnectionResult = {
//     val forward = connectTo(io)
//     val backward = connectFrom(io)
    
//     (forward, backward) match {
//       case (Connected, Connected) => Connected
//       case (Connected, _) => Connected
//       case (_, Connected) => Connected
//       case (ConnectionError(msg1), ConnectionError(msg2)) => 
//         ConnectionError(s"Forward: $msg1, Backward: $msg2")
//       case _ => ConnectionError("No connections established")
//     }
//   }
  
//   // Operator aliases for backward compatibility
//   def >>(io: IO)(implicit kill: SharedKillSwitch): ConnectionResult = bidirectional(io)
//   def >>(sink: Sink[IOData, NotUsed])(implicit kill: SharedKillSwitch): ConnectionResult = {
//     // Connect first available source to the sink
//     sources.headOption match {
//       case Some((_, source)) => 
//         source.via(kill.flow).runWith(sink)
//         Connected
//       case None => 
//         ConnectionError("No sources available")
//     }
//   }
  
//   // Utility methods
//   def destutter[T]: Flow[T, T, NotUsed] = Flow[T].statefulMapConcat(() => {
//     var last: Option[T] = None
//     elem =>
//       if (last != Some(elem)) { 
//         last = Some(elem)
//         List(elem) 
//       } else Nil
//   })
  
//   def debounce[T](timeout: FiniteDuration): Flow[T, T, NotUsed] = 
//     Flow[T].groupedWithin(1, timeout).map(_.head)
  
//   def throttle[T](elements: Int, per: FiniteDuration): Flow[T, T, NotUsed] = 
//     Flow[T].throttle(elements, per)
// }

// /**
//  * Enhanced IOSource with better functionality
//  */
// case class IOSource[T](src: Source[T, NotUsed]) {
//   implicit val system: ActorSystem = System()
//   implicit val materializer: ActorMaterializer = ActorMaterializer()

//   def orLast(initial: T): Source[T, NotUsed] = 
//     src.via(Flow[T].extrapolate(Iterator.continually(_), Some(initial)))
  
//   def >>[U >: T](sink: Sink[U, NotUsed])(implicit kill: SharedKillSwitch): ConnectionResult = {
//     Try(src.via(kill.flow).runWith(sink)) match {
//       case Success(_) => Connected
//       case Failure(ex) => ConnectionError(ex.getMessage)
//     }
//   }
  
//   def >>[U >: T](sink: Option[Sink[U, NotUsed]])(implicit kill: SharedKillSwitch): ConnectionResult = {
//     sink match {
//       case Some(s) => this >> s
//       case None => MissingEndpoint("sink", false)
//     }
//   }
  
//   // Utility transformations
//   def map[U](f: T => U): IOSource[U] = IOSource(src.map(f))
//   def filter(p: T => Boolean): IOSource[T] = IOSource(src.filter(p))
//   def collect[U](pf: PartialFunction[T, U]): IOSource[U] = IOSource(src.collect(pf))
// }

// /**
//  * IO Registry for managing multiple IOs
//  */
// object IORegistry {
//   private val ios = HashMap[String, IO]()
  
//   def register(name: String, io: IO): Unit = ios(name) = io
//   def get(name: String): Option[IO] = ios.get(name)
//   def apply(name: String): IO = ios(name)
//   def remove(name: String): Option[IO] = ios.remove(name)
//   def list: Set[String] = ios.keySet.toSet
  
//   // Bulk operations
//   def connectAll(implicit kill: SharedKillSwitch): Map[String, ConnectionResult] = {
//     val pairs = ios.values.toList.combinations(2).map { case List(io1, io2) =>
//       (io1, io2)
//     }
    
//     pairs.map { case (io1, io2) =>
//       s"${io1.getClass.getSimpleName} -> ${io2.getClass.getSimpleName}" -> io1.connectTo(io2)
//     }.toMap
//   }
// }

// /**
//  * IO Builder for fluent API
//  */
// case class IOBuilder(io: IO) {
//   def withSource[T](name: String, source: Source[T, NotUsed]): IOBuilder = {
//     // This would require a mutable IO or a new IO instance
//     // For now, return the same builder
//     this
//   }
  
//   def withSink[T](name: String, sink: Sink[T, NotUsed]): IOBuilder = {
//     this
//   }
  
//   def build: IO = io
// }

// object IOBuilder {
//   def apply(io: IO): IOBuilder = IOBuilder(io)
// } 