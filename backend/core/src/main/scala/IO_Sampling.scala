// package flow

// import scala.language.dynamics
// import scala.util.{Try, Success, Failure}
// import scala.concurrent.duration._

// import org.apache.pekko._
// import org.apache.pekko.actor._
// import org.apache.pekko.stream._
// import org.apache.pekko.stream.scaladsl._
// import org.apache.pekko.stream.stage._
// import scala.concurrent._

// import collection.mutable.{HashMap, ArrayBuffer, Queue}
// import java.util.concurrent.atomic.{AtomicLong, AtomicReference, AtomicBoolean}
// import java.time.Instant

// // Sampling strategies for different IO types
// sealed trait SamplingStrategy
// case object SampleLatest extends SamplingStrategy // Only most recent data (e.g., joystick position)
// case object SampleAll extends SamplingStrategy // All data (e.g., logging, recording)
// case object SampleBatched extends SamplingStrategy // Batch multiple samples (e.g., network optimization)
// case object SampleThrottled extends SamplingStrategy // Fixed rate sampling (e.g., 60fps)

// // Buffering strategies for different use cases
// sealed trait BufferingStrategy
// case object DropOldest extends BufferingStrategy // Drop oldest when buffer full (real-time)
// case object DropNewest extends BufferingStrategy // Drop newest when buffer full (preserve history)
// case object Backpressure extends BufferingStrategy // Block until space available
// case object Unbounded extends BufferingStrategy // No limit (memory permitting)

// // Sampling configuration
// case class SamplingConfig(
//   strategy: SamplingStrategy = SampleLatest,
//   buffering: BufferingStrategy = DropOldest,
//   sampleRate: Option[FiniteDuration] = None, // e.g., 60fps = 16.67ms
//   bufferSize: Int = 1, // For SampleLatest, 1 is sufficient
//   batchSize: Int = 10, // For SampleBatched
//   batchTimeout: FiniteDuration = 50.millis // For SampleBatched
// )

// // Enhanced IOData with sampling metadata
// sealed trait IOData {
//   def timestamp: Long
//   def sequenceNumber: Long
//   def sourceId: String
// }
// case class FloatData(
//   value: Float, 
//   override val timestamp: Long = System.nanoTime(),
//   override val sequenceNumber: Long = 0L,
//   override val sourceId: String = ""
// ) extends IOData

// case class IntData(
//   value: Int, 
//   override val timestamp: Long = System.nanoTime(),
//   override val sequenceNumber: Long = 0L,
//   override val sourceId: String = ""
// ) extends IOData

// case class BooleanData(
//   value: Boolean, 
//   override val timestamp: Long = System.nanoTime(),
//   override val sequenceNumber: Long = 0L,
//   override val sourceId: String = ""
// ) extends IOData

// case class Vector3Data(
//   x: Float, y: Float, z: Float, 
//   override val timestamp: Long = System.nanoTime(),
//   override val sequenceNumber: Long = 0L,
//   override val sourceId: String = ""
// ) extends IOData

// // High-performance sampling stage
// class SamplingStage[T](
//   config: SamplingConfig,
//   sourceId: String
// ) extends GraphStage[FlowShape[T, T]] {
  
//   val in = Inlet[T]("SamplingStage.in")
//   val out = Outlet[T]("SamplingStage.out")
  
//   override val shape = FlowShape(in, out)
  
//   override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    
//     private var sequenceNumber = 0L
//     private var lastSampleTime = 0L
//     private val buffer = Queue[T]()
//     private var batchBuffer = ArrayBuffer[T]()
//     private var lastPushed: Option[T] = None
    
//     // Timer for throttled sampling
//     private var timer: Option[Cancellable] = None
    
//     override def preStart(): Unit = {
//       config.strategy match {
//         case SampleThrottled =>
//           config.sampleRate.foreach { rate =>
//             timer = Some(system.scheduler.scheduleWithFixedDelay(
//               initialDelay = rate,
//               delay = rate
//             ) {
//               pushNextSample()
//             }(system.dispatcher))
//           }
//         case _ => // Other strategies don't need timers
//       }
//     }
    
//     override def postStop(): Unit = {
//       timer.foreach(_.cancel())
//     }
    
//     setHandler(in, new InHandler {
//       override def onPush(): Unit = {
//         val elem = grab(in)
//         sequenceNumber += 1
        
//         config.strategy match {
//           case SampleLatest =>
//             // Replace the single buffer element
//             buffer.clear()
//             buffer.enqueue(elem)
//             if (isAvailable(out)) {
//               pushNextSample()
//             }
            
//           case SampleAll =>
//             // Add to buffer, respect buffer size
//             addToBuffer(elem)
//             if (isAvailable(out)) {
//               pushNextSample()
//             }
            
//           case SampleBatched =>
//             // Add to batch buffer
//             batchBuffer += elem
//             if (batchBuffer.size >= config.batchSize || 
//                 (System.nanoTime() - lastSampleTime) > config.batchTimeout.toNanos) {
//               pushBatch()
//             }
            
//           case SampleThrottled =>
//             // Store latest, timer will push it
//             buffer.clear()
//             buffer.enqueue(elem)
//         }
        
//         pull(in)
//       }
//     })
    
//     setHandler(out, new OutHandler {
//       override def onPull(): Unit = {
//         config.strategy match {
//           case SampleThrottled =>
//             // Timer-driven, don't pull here
//           case _ =>
//             if (buffer.nonEmpty || batchBuffer.nonEmpty) {
//               pushNextSample()
//             } else {
//               pull(in)
//             }
//         }
//       }
//     })
    
//     private def addToBuffer(elem: T): Unit = {
//       config.buffering match {
//         case DropOldest =>
//           if (buffer.size >= config.bufferSize) {
//             buffer.dequeue() // Drop oldest
//           }
//           buffer.enqueue(elem)
          
//         case DropNewest =>
//           if (buffer.size < config.bufferSize) {
//             buffer.enqueue(elem)
//           }
//           // Otherwise drop newest (don't add)
          
//         case Backpressure =>
//           buffer.enqueue(elem)
//           // Will naturally backpressure when buffer is full
          
//         case Unbounded =>
//           buffer.enqueue(elem)
//       }
//     }
    
//     private def pushNextSample(): Unit = {
//       if (isAvailable(out) && buffer.nonEmpty) {
//         val elem = buffer.dequeue()
//         lastSampleTime = System.nanoTime()
//         push(out, elem)
//       }
//     }
    
//     private def pushBatch(): Unit = {
//       if (isAvailable(out) && batchBuffer.nonEmpty) {
//         // For now, push the first element of the batch
//         // Could be enhanced to push the entire batch as a sequence
//         val elem = batchBuffer.head
//         batchBuffer.remove(0)
//         lastSampleTime = System.nanoTime()
//         push(out, elem)
//       }
//     }
//   }
// }

// // Network optimization stage for OSC bundling
// class OSCBundlingStage[T](
//   bundleSize: Int = 10,
//   bundleTimeout: FiniteDuration = 50.millis
// ) extends GraphStage[FlowShape[T, Seq[T]]] {
  
//   val in = Inlet[T]("OSCBundlingStage.in")
//   val out = Outlet[Seq[T]]("OSCBundlingStage.out")
  
//   override val shape = FlowShape(in, out)
  
//   override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    
//     private val bundleBuffer = ArrayBuffer[T]()
//     private var lastBundleTime = System.nanoTime()
    
//     setHandler(in, new InHandler {
//       override def onPush(): Unit = {
//         val elem = grab(in)
//         bundleBuffer += elem
        
//         val now = System.nanoTime()
//         val shouldBundle = bundleBuffer.size >= bundleSize || 
//                           (now - lastBundleTime) > bundleTimeout.toNanos
        
//         if (shouldBundle && isAvailable(out)) {
//           pushBundle()
//         }
        
//         pull(in)
//       }
//     })
    
//     setHandler(out, new OutHandler {
//       override def onPull(): Unit = {
//         if (bundleBuffer.nonEmpty) {
//           pushBundle()
//         } else {
//           pull(in)
//         }
//       }
//     })
    
//     private def pushBundle(): Unit = {
//       if (isAvailable(out) && bundleBuffer.nonEmpty) {
//         val bundle = bundleBuffer.toSeq
//         bundleBuffer.clear()
//         lastBundleTime = System.nanoTime()
//         push(out, bundle)
//       }
//     }
//   }
// }

// // Connection result for error handling
// sealed trait ConnectionResult
// case object Connected extends ConnectionResult
// case class ConnectionError(message: String) extends ConnectionResult
// case class MissingEndpoint(name: String, isSource: Boolean) extends ConnectionResult

// /**
//  * Enhanced IO trait with sampling and buffering strategies
//  */
// trait IO extends Dynamic {
  
//   implicit val system: ActorSystem = System()
//   implicit val materializer: ActorMaterializer = ActorMaterializer()

//   // Sampling configuration per source
//   def samplingConfigs: Map[String, SamplingConfig] = Map.empty
  
//   // Default sampling config
//   def defaultSamplingConfig: SamplingConfig = SamplingConfig(
//     strategy = SampleLatest,
//     buffering = DropOldest,
//     bufferSize = 1
//   )
  
//   // Type-safe sources and sinks
//   def sources: Map[String, Source[IOData, NotUsed]] = Map.empty
//   def sinks: Map[String, Sink[IOData, NotUsed]] = Map.empty
  
//   // Enhanced typed accessors with sampling
//   def floatSource(name: String): Option[Source[Float, NotUsed]] = 
//     sources.get(name).map { source =>
//       val config = samplingConfigs.getOrElse(name, defaultSamplingConfig)
//       source
//         .via(new SamplingStage[IOData](config, name))
//         .map(_.asInstanceOf[FloatData].value)
//     }
  
//   def booleanSource(name: String): Option[Source[Boolean, NotUsed]] = 
//     sources.get(name).map { source =>
//       val config = samplingConfigs.getOrElse(name, defaultSamplingConfig)
//       source
//         .via(new SamplingStage[IOData](config, name))
//         .map(_.asInstanceOf[BooleanData].value)
//     }
  
//   def vector3Source(name: String): Option[Source[Vector3Data, NotUsed]] = 
//     sources.get(name).map { source =>
//       val config = samplingConfigs.getOrElse(name, defaultSamplingConfig)
//       source
//         .via(new SamplingStage[IOData](config, name))
//         .map(_.asInstanceOf[Vector3Data])
//     }
  
//   // Connection operators with sampling awareness
//   def connectTo(io: IO)(implicit kill: SharedKillSwitch): ConnectionResult = {
//     val connections = for {
//       (name, source) <- sources
//       sink <- io.sinks.get(name)
//     } yield {
//       val config = samplingConfigs.getOrElse(name, defaultSamplingConfig)
//       val sampledSource = source.via(new SamplingStage[IOData](config, name))
//       sampledSource.via(kill.flow).runWith(sink)
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
//  * HID Device IO with joystick-optimized sampling
//  */
// abstract class HidDeviceIO(val index: Int) extends IO {
  
//   // Joystick-specific sampling: 60fps, latest data only
//   override def defaultSamplingConfig: SamplingConfig = SamplingConfig(
//     strategy = SampleThrottled,
//     buffering = DropOldest,
//     sampleRate = Some(16.67.millis), // 60fps
//     bufferSize = 1
//   )
  
//   // Override for specific controls that need different sampling
//   override def samplingConfigs: Map[String, SamplingConfig] = Map(
//     // Analog sticks: 60fps sampling
//     "leftX" -> SamplingConfig(SampleThrottled, DropOldest, Some(16.67.millis), 1),
//     "leftY" -> SamplingConfig(SampleThrottled, DropOldest, Some(16.67.millis), 1),
//     "rightX" -> SamplingConfig(SampleThrottled, DropOldest, Some(16.67.millis), 1),
//     "rightY" -> SamplingConfig(SampleThrottled, DropOldest, Some(16.67.millis), 1),
    
//     // Buttons: immediate response, no sampling
//     "triangle" -> SamplingConfig(SampleAll, DropOldest, None, 10),
//     "circle" -> SamplingConfig(SampleAll, DropOldest, None, 10),
//     "X" -> SamplingConfig(SampleAll, DropOldest, None, 10),
//     "square" -> SamplingConfig(SampleAll, DropOldest, None, 10)
//   )
  
//   // ... rest of HID implementation
// }

// /**
//  * OSC IO with network optimization
//  */
// class OSCIO extends IO {
  
//   // Network-optimized sampling: bundle messages, drop stale data
//   override def defaultSamplingConfig: SamplingConfig = SamplingConfig(
//     strategy = SampleBatched,
//     buffering = DropOldest,
//     batchSize = 10,
//     batchTimeout = 50.millis
//   )
  
//   // Add OSC-specific bundling stage
//   def bundledSource[T](name: String): Option[Source[Seq[T], NotUsed]] = 
//     sources.get(name).map { source =>
//       val config = samplingConfigs.getOrElse(name, defaultSamplingConfig)
//       source
//         .via(new SamplingStage[IOData](config, name))
//         .via(new OSCBundlingStage[T](10, 50.millis))
//         .map(_.map(_.asInstanceOf[T]))
//     }
  
//   // ... rest of OSC implementation
// }

// /**
//  * Example usage and configuration
//  */
// object IOSamplingExamples {
  
//   // Configure different sampling strategies for different use cases
//   def configureJoystickSampling(): Map[String, SamplingConfig] = Map(
//     // Position data: 60fps sampling
//     "leftX" -> SamplingConfig(SampleThrottled, DropOldest, Some(16.67.millis), 1),
//     "leftY" -> SamplingConfig(SampleThrottled, DropOldest, Some(16.67.millis), 1),
    
//     // Button presses: all events
//     "triangle" -> SamplingConfig(SampleAll, DropOldest, None, 50),
//     "circle" -> SamplingConfig(SampleAll, DropOldest, None, 50),
    
//     // Trigger data: 120fps for precision
//     "L2Analog" -> SamplingConfig(SampleThrottled, DropOldest, Some(8.33.millis), 1),
//     "R2Analog" -> SamplingConfig(SampleThrottled, DropOldest, Some(8.33.millis), 1)
//   )
  
//   def configureOSCOptimization(): Map[String, SamplingConfig] = Map(
//     // Bundle multiple joystick updates
//     "joystick_updates" -> SamplingConfig(SampleBatched, DropOldest, batchSize = 20, batchTimeout = 100.millis),
    
//     // Immediate button events
//     "button_events" -> SamplingConfig(SampleAll, DropOldest, None, 100),
    
//     // Throttled position updates
//     "position_updates" -> SamplingConfig(SampleThrottled, DropOldest, Some(33.millis), 1) // 30fps
//   )
  
//   def configureRecordingIO(): Map[String, SamplingConfig] = Map(
//     // Record everything for analysis
//     "all_data" -> SamplingConfig(SampleAll, Unbounded, None, Int.MaxValue)
//   )
// } 