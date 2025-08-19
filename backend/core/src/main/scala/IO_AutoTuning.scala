// package flow

// import scala.language.dynamics
// import scala.util.{Try, Success, Failure}
// import scala.concurrent.duration._
// import scala.math._

// import org.apache.pekko._
// import org.apache.pekko.actor._
// import org.apache.pekko.stream._
// import org.apache.pekko.stream.scaladsl._
// import org.apache.pekko.stream.stage._
// import scala.concurrent._

// import collection.mutable.{HashMap, ArrayBuffer, Queue, PriorityQueue}
// import java.util.concurrent.atomic.{AtomicLong, AtomicReference, AtomicBoolean, AtomicDouble}
// import java.time.Instant
// import java.util.concurrent.ConcurrentHashMap

// // Performance profiling data structures
// case class PerformanceProfile(
//   name: String,
//   timestamp: Long = System.nanoTime(),
//   latencyNs: Long,
//   throughputPerSec: Double,
//   bundleSize: Int,
//   bundleTimeoutMs: Long,
//   networkLatencyNs: Long,
//   cpuUtilization: Double,
//   memoryUsage: Long,
//   droppedMessages: Long,
//   totalMessages: Long
// ) {
//   def latencyMs: Double = latencyNs / 1_000_000.0
//   def throughputMsgsPerSec: Double = throughputPerSec
//   def efficiencyScore: Double = {
//     // Higher score = better performance
//     // Balance between latency and throughput
//     val latencyScore = 1.0 / (1.0 + latencyMs / 100.0) // Prefer < 100ms
//     val throughputScore = min(throughputMsgsPerSec / 1000.0, 1.0) // Prefer > 1000 msg/sec
//     val dropRate = if (totalMessages > 0) droppedMessages.toDouble / totalMessages else 0.0
//     val dropScore = 1.0 - dropRate
    
//     (latencyScore * 0.4 + throughputScore * 0.4 + dropScore * 0.2)
//   }
// }

// // Auto-tuning configuration
// case class AutoTuningConfig(
//   enabled: Boolean = true,
//   adaptationInterval: FiniteDuration = 5.seconds,
//   minBundleSize: Int = 1,
//   maxBundleSize: Int = 100,
//   minBundleTimeout: FiniteDuration = 1.millis,
//   maxBundleTimeout: FiniteDuration = 500.millis,
//   targetLatencyMs: Double = 50.0,
//   targetThroughputPerSec: Double = 1000.0,
//   explorationRate: Double = 0.1, // 10% random exploration
//   learningRate: Double = 0.1,
//   historySize: Int = 100,
//   stabilityThreshold: Int = 10 // Consecutive similar configs before considering stable
// )

// // Bundling parameters that can be tuned
// case class BundlingParams(
//   bundleSize: Int,
//   bundleTimeout: FiniteDuration
// ) {
//   def toSamplingConfig: SamplingConfig = SamplingConfig(
//     strategy = SampleBatched,
//     buffering = DropOldest,
//     batchSize = bundleSize,
//     batchTimeout = bundleTimeout
//   )
// }

// // Enhanced performance monitoring stage with auto-tuning
// class AutoTuningStage[T](
//   initialParams: BundlingParams,
//   config: AutoTuningConfig,
//   sourceId: String
// ) extends GraphStage[FlowShape[T, Seq[T]]] {
  
//   val in = Inlet[T]("AutoTuningStage.in")
//   val out = Outlet[Seq[T]]("AutoTuningStage.out")
  
//   override val shape = FlowShape(in, out)
  
//   override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    
//     private val bundleBuffer = ArrayBuffer[T]()
//     private var lastBundleTime = System.nanoTime()
//     private var currentParams = initialParams
    
//     // Performance tracking
//     private val messageCount = new AtomicLong(0)
//     private val droppedCount = new AtomicLong(0)
//     private val latencyHistory = ArrayBuffer[Long]()
//     private val throughputHistory = ArrayBuffer[Double]()
//     private val profileHistory = ArrayBuffer[PerformanceProfile]()
    
//     // Auto-tuning state
//     private var lastAdaptationTime = System.nanoTime()
//     private var consecutiveStableConfigs = 0
//     private var bestParams = initialParams
//     private var bestScore = 0.0
    
//     // Timer for adaptation
//     private var adaptationTimer: Option[Cancellable] = None
    
//     override def preStart(): Unit = {
//       if (config.enabled) {
//         adaptationTimer = Some(system.scheduler.scheduleWithFixedDelay(
//           initialDelay = config.adaptationInterval,
//           delay = config.adaptationInterval
//         ) {
//           adaptParameters()
//         }(system.dispatcher))
//       }
//     }
    
//     override def postStop(): Unit = {
//       adaptationTimer.foreach(_.cancel())
//     }
    
//     setHandler(in, new InHandler {
//       override def onPush(): Unit = {
//         val startTime = System.nanoTime()
//         val elem = grab(in)
//         messageCount.incrementAndGet()
        
//         bundleBuffer += elem
        
//         val now = System.nanoTime()
//         val shouldBundle = bundleBuffer.size >= currentParams.bundleSize || 
//                           (now - lastBundleTime) > currentParams.bundleTimeout.toNanos
        
//         if (shouldBundle && isAvailable(out)) {
//           val bundle = pushBundle()
//           val latency = System.nanoTime() - startTime
//           recordPerformance(latency, bundle.size)
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
    
//     private def pushBundle(): Seq[T] = {
//       if (isAvailable(out) && bundleBuffer.nonEmpty) {
//         val bundle = bundleBuffer.toSeq
//         bundleBuffer.clear()
//         lastBundleTime = System.nanoTime()
//         push(out, bundle)
//         bundle
//       } else {
//         Seq.empty
//       }
//     }
    
//     private def recordPerformance(latency: Long, bundleSize: Int): Unit = {
//       latencyHistory.synchronized {
//         latencyHistory += latency
//         if (latencyHistory.size > config.historySize) {
//           latencyHistory.remove(0)
//         }
//       }
      
//       // Calculate current throughput
//       val now = System.nanoTime()
//       val recentMessages = messageCount.get()
//       val timeWindow = 1.0 // 1 second window
//       val throughput = recentMessages / timeWindow
      
//       throughputHistory.synchronized {
//         throughputHistory += throughput
//         if (throughputHistory.size > config.historySize) {
//           throughputHistory.remove(0)
//         }
//       }
      
//       // Create performance profile
//       val profile = PerformanceProfile(
//         name = sourceId,
//         latencyNs = latency,
//         throughputPerSec = throughput,
//         bundleSize = currentParams.bundleSize,
//         bundleTimeoutMs = currentParams.bundleTimeout.toMillis,
//         networkLatencyNs = 0, // Would need network monitoring
//         cpuUtilization = 0.0, // Would need system monitoring
//         memoryUsage = 0L, // Would need memory monitoring
//         droppedMessages = droppedCount.get(),
//         totalMessages = messageCount.get()
//       )
      
//       profileHistory.synchronized {
//         profileHistory += profile
//         if (profileHistory.size > config.historySize) {
//           profileHistory.remove(0)
//         }
//       }
//     }
    
//     private def adaptParameters(): Unit = {
//       val now = System.nanoTime()
//       if (now - lastAdaptationTime < config.adaptationInterval.toNanos) return
      
//       val currentProfile = getCurrentProfile()
//       val currentScore = currentProfile.efficiencyScore
      
//       // Update best if improved
//       if (currentScore > bestScore) {
//         bestScore = currentScore
//         bestParams = currentParams
//         consecutiveStableConfigs = 0
//       } else {
//         consecutiveStableConfigs += 1
//       }
      
//       // Decide on next parameters
//       val newParams = if (consecutiveStableConfigs >= config.stabilityThreshold) {
//         // Stable configuration, try small improvements
//         exploreNearbyParams(currentParams)
//       } else if (math.random < config.explorationRate) {
//         // Random exploration
//         generateRandomParams()
//       } else {
//         // Gradient-based improvement
//         improveParams(currentParams, currentProfile)
//       }
      
//       // Apply new parameters
//       currentParams = newParams
//       lastAdaptationTime = now
      
//       println(s"[${Instant.now()}] Auto-tuning $sourceId: " +
//               s"bundleSize=${currentParams.bundleSize}, " +
//               s"timeout=${currentParams.bundleTimeout.toMillis}ms, " +
//               f"score=${currentScore}%.3f")
//     }
    
//     private def getCurrentProfile(): PerformanceProfile = {
//       profileHistory.synchronized {
//         if (profileHistory.isEmpty) {
//           PerformanceProfile(sourceId, bundleSize = currentParams.bundleSize, 
//                            bundleTimeoutMs = currentParams.bundleTimeout.toMillis,
//                            latencyNs = 0, throughputPerSec = 0, networkLatencyNs = 0,
//                            cpuUtilization = 0, memoryUsage = 0, droppedMessages = 0, totalMessages = 0)
//         } else {
//           profileHistory.last
//         }
//       }
//     }
    
//     private def exploreNearbyParams(params: BundlingParams): BundlingParams = {
//       val sizeVariation = max(1, params.bundleSize / 10)
//       val timeoutVariation = params.bundleTimeout.toMillis / 10
      
//       val newSize = params.bundleSize + (math.random * sizeVariation * 2 - sizeVariation).toInt
//       val newTimeout = params.bundleTimeout.toMillis + (math.random * timeoutVariation * 2 - timeoutVariation)
      
//       BundlingParams(
//         bundleSize = newSize.max(config.minBundleSize).min(config.maxBundleSize),
//         bundleTimeout = newTimeout.millis.max(config.minBundleTimeout).min(config.maxBundleTimeout)
//       )
//     }
    
//     private def generateRandomParams(): BundlingParams = {
//       val size = config.minBundleSize + (math.random * (config.maxBundleSize - config.minBundleSize)).toInt
//       val timeout = config.minBundleTimeout.toMillis + 
//                    math.random * (config.maxBundleTimeout.toMillis - config.minBundleTimeout.toMillis)
      
//       BundlingParams(
//         bundleSize = size,
//         bundleTimeout = timeout.millis
//       )
//     }
    
//     private def improveParams(params: BundlingParams, profile: PerformanceProfile): BundlingParams = {
//       val latencyMs = profile.latencyMs
//       val throughput = profile.throughputMsgsPerSec
      
//       // Simple gradient-based improvement
//       var newSize = params.bundleSize
//       var newTimeout = params.bundleTimeout.toMillis
      
//       if (latencyMs > config.targetLatencyMs) {
//         // Reduce latency by decreasing bundle size and timeout
//         newSize = max(config.minBundleSize, (newSize * 0.9).toInt)
//         newTimeout = max(config.minBundleTimeout.toMillis, newTimeout * 0.9)
//       } else if (throughput < config.targetThroughputPerSec) {
//         // Increase throughput by increasing bundle size
//         newSize = min(config.maxBundleSize, (newSize * 1.1).toInt)
//       }
      
//       BundlingParams(
//         bundleSize = newSize,
//         bundleTimeout = newTimeout.millis
//       )
//     }
    
//     // Public methods for monitoring
//     def getCurrentParams: BundlingParams = currentParams
//     def getBestParams: BundlingParams = bestParams
//     def getBestScore: Double = bestScore
//     def getPerformanceHistory: Seq[PerformanceProfile] = profileHistory.synchronized(profileHistory.toSeq)
//   }
// }

// // Network performance profiler
// class NetworkProfiler {
//   private val latencyHistory = ArrayBuffer[Long]()
//   private val bandwidthHistory = ArrayBuffer[Double]()
//   private val packetLossHistory = ArrayBuffer[Double]()
  
//   def recordLatency(latencyNs: Long): Unit = {
//     latencyHistory.synchronized {
//       latencyHistory += latencyNs
//       if (latencyHistory.size > 100) latencyHistory.remove(0)
//     }
//   }
  
//   def recordBandwidth(bytesPerSec: Double): Unit = {
//     bandwidthHistory.synchronized {
//       bandwidthHistory += bytesPerSec
//       if (bandwidthHistory.size > 100) bandwidthHistory.remove(0)
//     }
//   }
  
//   def recordPacketLoss(lossRate: Double): Unit = {
//     packetLossHistory.synchronized {
//       packetLossHistory += lossRate
//       if (packetLossHistory.size > 100) packetLossHistory.remove(0)
//     }
//   }
  
//   def getAverageLatency: Long = {
//     latencyHistory.synchronized {
//       if (latencyHistory.isEmpty) 0L else latencyHistory.sum / latencyHistory.size
//     }
//   }
  
//   def getAverageBandwidth: Double = {
//     bandwidthHistory.synchronized {
//       if (bandwidthHistory.isEmpty) 0.0 else bandwidthHistory.sum / bandwidthHistory.size
//     }
//   }
  
//   def getAveragePacketLoss: Double = {
//     packetLossHistory.synchronized {
//       if (packetLossHistory.isEmpty) 0.0 else packetLossHistory.sum / packetLossHistory.size
//     }
//   }
// }

// // System resource monitor
// class SystemMonitor {
//   private val cpuHistory = ArrayBuffer[Double]()
//   private val memoryHistory = ArrayBuffer[Long]()
  
//   def recordCPU(utilization: Double): Unit = {
//     cpuHistory.synchronized {
//       cpuHistory += utilization
//       if (cpuHistory.size > 100) cpuHistory.remove(0)
//     }
//   }
  
//   def recordMemory(usageBytes: Long): Unit = {
//     memoryHistory.synchronized {
//       memoryHistory += usageBytes
//       if (memoryHistory.size > 100) memoryHistory.remove(0)
//     }
//   }
  
//   def getAverageCPU: Double = {
//     cpuHistory.synchronized {
//       if (cpuHistory.isEmpty) 0.0 else cpuHistory.sum / cpuHistory.size
//     }
//   }
  
//   def getAverageMemory: Long = {
//     memoryHistory.synchronized {
//       if (memoryHistory.isEmpty) 0L else memoryHistory.sum / memoryHistory.size
//     }
//   }
// }

// // Auto-tuning IO trait
// trait AutoTuningIO extends IO {
  
//   private val networkProfiler = new NetworkProfiler()
//   private val systemMonitor = new SystemMonitor()
//   private val autoTuningConfigs = HashMap[String, AutoTuningConfig]()
//   private val currentParams = HashMap[String, BundlingParams]()
  
//   // Default auto-tuning configuration
//   def defaultAutoTuningConfig: AutoTuningConfig = AutoTuningConfig(
//     enabled = true,
//     adaptationInterval = 5.seconds,
//     minBundleSize = 1,
//     maxBundleSize = 50,
//     minBundleTimeout = 1.millis,
//     maxBundleTimeout = 200.millis,
//     targetLatencyMs = 50.0,
//     targetThroughputPerSec = 1000.0,
//     explorationRate = 0.1,
//     learningRate = 0.1,
//     historySize = 100,
//     stabilityThreshold = 10
//   )
  
//   // Configure auto-tuning for specific sources
//   def configureAutoTuning(sourceName: String, config: AutoTuningConfig): Unit = {
//     autoTuningConfigs(sourceName) = config
//   }
  
//   // Enhanced source with auto-tuning
//   def autoTuningSource[T](name: String): Option[Source[Seq[T], NotUsed]] = 
//     sources.get(name).map { source =>
//       val config = autoTuningConfigs.getOrElse(name, defaultAutoTuningConfig)
//       val initialParams = currentParams.getOrElse(name, BundlingParams(10, 50.millis))
      
//       source
//         .via(new AutoTuningStage[T](initialParams, config, name))
//         .map(_.map(_.asInstanceOf[T]))
//     }
  
//   // Performance monitoring methods
//   def getNetworkStats: (Long, Double, Double) = (
//     networkProfiler.getAverageLatency,
//     networkProfiler.getAverageBandwidth,
//     networkProfiler.getAveragePacketLoss
//   )
  
//   def getSystemStats: (Double, Long) = (
//     systemMonitor.getAverageCPU,
//     systemMonitor.getAverageMemory
//   )
  
//   // Manual parameter adjustment
//   def setBundlingParams(sourceName: String, params: BundlingParams): Unit = {
//     currentParams(sourceName) = params
//   }
  
//   def getBundlingParams(sourceName: String): Option[BundlingParams] = 
//     currentParams.get(sourceName)
  
//   // Performance analysis
//   def analyzeLatencyThroughputTradeoff(sourceName: String): String = {
//     val config = autoTuningConfigs.getOrElse(sourceName, defaultAutoTuningConfig)
//     val params = currentParams.getOrElse(sourceName, BundlingParams(10, 50.millis))
    
//     val (avgLatency, avgBandwidth, packetLoss) = getNetworkStats
//     val (cpuUtil, memoryUsage) = getSystemStats
    
//     s"""
//     |Latency vs Throughput Analysis for $sourceName:
//     |  Current Configuration:
//     |    Bundle Size: ${params.bundleSize}
//     |    Bundle Timeout: ${params.bundleTimeout.toMillis}ms
//     |  
//     |  Network Performance:
//     |    Average Latency: ${avgLatency / 1_000_000.0}ms
//     |    Average Bandwidth: ${avgBandwidth / 1024 / 1024}MB/s
//     |    Packet Loss Rate: ${packetLoss * 100}%
//     |  
//     |  System Performance:
//     |    CPU Utilization: ${cpuUtil * 100}%
//     |    Memory Usage: ${memoryUsage / 1024 / 1024}MB
//     |  
//     |  Trade-off Analysis:
//     |    - Larger bundles → Higher throughput, higher latency
//     |    - Smaller bundles → Lower latency, lower throughput
//     |    - Network conditions affect optimal bundle size
//     |    - CPU utilization affects processing overhead
//     """.stripMargin
//   }
  
//   // Auto-tuning control
//   def enableAutoTuning(sourceName: String): Unit = {
//     val config = autoTuningConfigs.getOrElse(sourceName, defaultAutoTuningConfig)
//     autoTuningConfigs(sourceName) = config.copy(enabled = true)
//   }
  
//   def disableAutoTuning(sourceName: String): Unit = {
//     val config = autoTuningConfigs.getOrElse(sourceName, defaultAutoTuningConfig)
//     autoTuningConfigs(sourceName) = config.copy(enabled = false)
//   }
  
//   def isAutoTuningEnabled(sourceName: String): Boolean = {
//     autoTuningConfigs.get(sourceName).exists(_.enabled)
//   }
// }

// // Example usage and configuration
// object AutoTuningExamples {
  
//   def configureOSCAutoTuning(): AutoTuningConfig = AutoTuningConfig(
//     enabled = true,
//     adaptationInterval = 3.seconds, // More frequent adaptation for network
//     minBundleSize = 1,
//     maxBundleSize = 100,
//     minBundleTimeout = 1.millis,
//     maxBundleTimeout = 500.millis,
//     targetLatencyMs = 30.0, // Lower latency target for real-time
//     targetThroughputPerSec = 2000.0, // Higher throughput target
//     explorationRate = 0.15, // More exploration for network conditions
//     learningRate = 0.2,
//     historySize = 200,
//     stabilityThreshold = 15
//   )
  
//   def configureJoystickAutoTuning(): AutoTuningConfig = AutoTuningConfig(
//     enabled = true,
//     adaptationInterval = 10.seconds, // Less frequent for stable input
//     minBundleSize = 1,
//     maxBundleSize = 20,
//     minBundleTimeout = 5.millis,
//     maxBundleTimeout = 100.millis,
//     targetLatencyMs = 16.67, // 60fps target
//     targetThroughputPerSec = 60.0,
//     explorationRate = 0.05, // Less exploration for predictable input
//     learningRate = 0.1,
//     historySize = 100,
//     stabilityThreshold = 20
//   )
  
//   def configureRecordingAutoTuning(): AutoTuningConfig = AutoTuningConfig(
//     enabled = false, // Disable for recording to preserve all data
//     adaptationInterval = 30.seconds,
//     minBundleSize = 1,
//     maxBundleSize = 1000,
//     minBundleTimeout = 1.millis,
//     maxBundleTimeout = 1000.millis,
//     targetLatencyMs = 1000.0, // High latency acceptable for recording
//     targetThroughputPerSec = 10000.0, // High throughput for data capture
//     explorationRate = 0.0, // No exploration for recording
//     learningRate = 0.0,
//     historySize = 1000,
//     stabilityThreshold = 1
//   )
// } 