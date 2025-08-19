// package flow

// import scala.concurrent.duration._
// import scala.util.{Try, Success, Failure}
// import org.apache.pekko.stream.scaladsl._
// import org.apache.pekko.stream._
// import java.time.Instant

// /**
//  * Comprehensive examples demonstrating auto-tuning for latency vs throughput optimization
//  */
// object IOAutoTuningExamples {
  
//   // Example 1: OSC Network Optimization with Real-time Profiling
//   def demonstrateOSCAutoTuning(): Unit = {
//     println("=== OSC Auto-tuning Demonstration ===")
    
//     // Create an auto-tuning OSC IO
//     val oscIO = new AutoTuningOSCIO()
    
//     // Configure auto-tuning for different message types
//     oscIO.configureAutoTuning("joystick_updates", AutoTuningExamples.configureOSCAutoTuning())
//     oscIO.configureAutoTuning("button_events", AutoTuningExamples.configureJoystickAutoTuning())
//     oscIO.configureAutoTuning("position_updates", AutoTuningExamples.configureOSCAutoTuning())
    
//     // Start with conservative parameters
//     oscIO.setBundlingParams("joystick_updates", BundlingParams(5, 25.millis))
//     oscIO.setBundlingParams("button_events", BundlingParams(1, 5.millis))
//     oscIO.setBundlingParams("position_updates", BundlingParams(10, 50.millis))
    
//     // Simulate network conditions
//     simulateNetworkConditions(oscIO)
    
//     // Monitor performance over time
//     monitorPerformance(oscIO, "joystick_updates")
    
//     println("OSC Auto-tuning demonstration completed")
//   }
  
//   // Example 2: Joystick Input Optimization
//   def demonstrateJoystickAutoTuning(): Unit = {
//     println("=== Joystick Auto-tuning Demonstration ===")
    
//     val joystickIO = new AutoTuningJoystickIO()
    
//     // Configure for different joystick axes
//     joystickIO.configureAutoTuning("leftStick", AutoTuningExamples.configureJoystickAutoTuning())
//     joystickIO.configureAutoTuning("rightStick", AutoTuningExamples.configureJoystickAutoTuning())
//     joystickIO.configureAutoTuning("triggers", AutoTuningExamples.configureJoystickAutoTuning())
    
//     // Start with 60fps target
//     joystickIO.setBundlingParams("leftStick", BundlingParams(1, 16.67.millis))
//     joystickIO.setBundlingParams("rightStick", BundlingParams(1, 16.67.millis))
//     joystickIO.setBundlingParams("triggers", BundlingParams(1, 8.33.millis)) // 120fps for precision
    
//     // Simulate joystick input patterns
//     simulateJoystickInput(joystickIO)
    
//     println("Joystick Auto-tuning demonstration completed")
//   }
  
//   // Example 3: Recording vs Real-time Trade-off Analysis
//   def demonstrateRecordingVsRealtime(): Unit = {
//     println("=== Recording vs Real-time Trade-off Analysis ===")
    
//     val recordingIO = new AutoTuningRecordingIO()
    
//     // Configure for recording (high throughput, high latency acceptable)
//     recordingIO.configureAutoTuning("all_data", AutoTuningExamples.configureRecordingAutoTuning())
    
//     // Configure for real-time monitoring (low latency, moderate throughput)
//     val realtimeConfig = AutoTuningConfig(
//       enabled = true,
//       adaptationInterval = 2.seconds,
//       minBundleSize = 1,
//       maxBundleSize = 10,
//       minBundleTimeout = 1.millis,
//       maxBundleTimeout = 100.millis,
//       targetLatencyMs = 20.0,
//       targetThroughputPerSec = 500.0,
//       explorationRate = 0.05,
//       learningRate = 0.15,
//       historySize = 50,
//       stabilityThreshold = 5
//     )
//     recordingIO.configureAutoTuning("realtime_monitor", realtimeConfig)
    
//     // Compare performance profiles
//     comparePerformanceProfiles(recordingIO)
    
//     println("Recording vs Real-time analysis completed")
//   }
  
//   // Example 4: Network Condition Adaptation
//   def demonstrateNetworkAdaptation(): Unit = {
//     println("=== Network Condition Adaptation ===")
    
//     val adaptiveIO = new AutoTuningNetworkIO()
    
//     // Configure for adaptive network conditions
//     val adaptiveConfig = AutoTuningConfig(
//       enabled = true,
//       adaptationInterval = 1.second, // Very frequent adaptation
//       minBundleSize = 1,
//       maxBundleSize = 200,
//       minBundleTimeout = 1.millis,
//       maxBundleTimeout = 1000.millis,
//       targetLatencyMs = 50.0,
//       targetThroughputPerSec = 1000.0,
//       explorationRate = 0.2, // High exploration for changing conditions
//       learningRate = 0.3,
//       historySize = 300,
//       stabilityThreshold = 5
//     )
//     adaptiveIO.configureAutoTuning("adaptive_stream", adaptiveConfig)
    
//     // Simulate changing network conditions
//     simulateChangingNetworkConditions(adaptiveIO)
    
//     println("Network adaptation demonstration completed")
//   }
  
//   // Helper methods for demonstrations
  
//   private def simulateNetworkConditions(io: AutoTuningIO): Unit = {
//     // Simulate different network scenarios
//     val scenarios = List(
//       ("Good Network", 5.0, 100.0, 0.001), // Low latency, high bandwidth, low loss
//       ("Poor Network", 50.0, 10.0, 0.1),   // High latency, low bandwidth, high loss
//       ("Variable Network", 25.0, 50.0, 0.05) // Medium everything
//     )
    
//     scenarios.foreach { case (name, latencyMs, bandwidthMBps, lossRate) =>
//       println(s"\n--- Simulating $name ---")
//       println(s"Latency: ${latencyMs}ms, Bandwidth: ${bandwidthMBps}MB/s, Loss: ${lossRate * 100}%")
      
//       // Update network profiler with simulated conditions
//       // (In real implementation, this would come from actual network monitoring)
      
//       // Let auto-tuning adapt
//       Thread.sleep(5000) // 5 seconds per scenario
      
//       // Show current performance
//       println(io.analyzeLatencyThroughputTradeoff("joystick_updates"))
//     }
//   }
  
//   private def simulateJoystickInput(io: AutoTuningIO): Unit = {
//     // Simulate different joystick usage patterns
//     val patterns = List(
//       ("Slow Movement", 1, 100), // 1 update per 100ms
//       ("Fast Movement", 10, 100), // 10 updates per 100ms
//       ("Rapid Flicking", 50, 100) // 50 updates per 100ms
//     )
    
//     patterns.foreach { case (name, updatesPer100ms, durationMs) =>
//       println(s"\n--- Simulating $name ---")
//       println(s"Input rate: ${updatesPer100ms} updates per 100ms")
      
//       // Simulate input pattern
//       val startTime = System.currentTimeMillis()
//       while (System.currentTimeMillis() - startTime < durationMs) {
//         // Simulate joystick input
//         Thread.sleep(100 / updatesPer100ms)
//       }
      
//       // Show adaptation results
//       println(io.analyzeLatencyThroughputTradeoff("leftStick"))
//     }
//   }
  
//   private def simulateChangingNetworkConditions(io: AutoTuningIO): Unit = {
//     // Simulate network conditions that change over time
//     val timeWindows = List(
//       (0, 10, "Stable Good Network"),
//       (10, 20, "Degrading Network"),
//       (20, 30, "Poor Network"),
//       (30, 40, "Recovering Network"),
//       (40, 50, "Stable Good Network")
//     )
    
//     timeWindows.foreach { case (start, end, description) =>
//       println(s"\n--- $description (${start}-${end}s) ---")
      
//       val startTime = System.currentTimeMillis()
//       while (System.currentTimeMillis() - startTime < (end - start) * 1000) {
//         // Simulate network traffic
//         Thread.sleep(100)
        
//         // Show current parameters every 2 seconds
//         if ((System.currentTimeMillis() - startTime) % 2000 < 100) {
//           io.getBundlingParams("adaptive_stream").foreach { params =>
//             println(f"Current: bundleSize=${params.bundleSize}, timeout=${params.bundleTimeout.toMillis}ms")
//           }
//         }
//       }
//     }
//   }
  
//   private def monitorPerformance(io: AutoTuningIO, sourceName: String): Unit = {
//     // Monitor performance metrics over time
//     val monitoringDuration = 30.seconds
//     val startTime = System.currentTimeMillis()
    
//     println(s"\n--- Monitoring $sourceName for ${monitoringDuration.toSeconds}s ---")
    
//     while (System.currentTimeMillis() - startTime < monitoringDuration.toMillis) {
//       Thread.sleep(2000) // Check every 2 seconds
      
//       val (networkLatency, bandwidth, packetLoss) = io.getNetworkStats
//       val (cpuUtil, memoryUsage) = io.getSystemStats
      
//       println(f"[${Instant.now()}] Network: ${networkLatency/1_000_000.0}%.1fms, " +
//               f"${bandwidth/1024/1024}%.1fMB/s, ${packetLoss*100}%.1f%% loss | " +
//               f"System: ${cpuUtil*100}%.1f%% CPU, ${memoryUsage/1024/1024}%.1fMB")
//     }
//   }
  
//   private def comparePerformanceProfiles(io: AutoTuningIO): Unit = {
//     println("\n--- Performance Profile Comparison ---")
    
//     // Simulate recording workload
//     println("Recording Configuration (High Throughput, High Latency):")
//     io.setBundlingParams("all_data", BundlingParams(100, 500.millis))
//     println(io.analyzeLatencyThroughputTradeoff("all_data"))
    
//     // Simulate real-time workload
//     println("\nReal-time Configuration (Low Latency, Moderate Throughput):")
//     io.setBundlingParams("realtime_monitor", BundlingParams(5, 20.millis))
//     println(io.analyzeLatencyThroughputTradeoff("realtime_monitor"))
    
//     // Show the trade-off analysis
//     println("\n--- Trade-off Analysis ---")
//     println("""
//     |Latency vs Throughput Trade-offs:
//     |
//     |1. Recording Mode:
//     |   - Bundle Size: Large (50-200 messages)
//     |   - Timeout: Long (200-1000ms)
//     |   - Priority: Throughput over Latency
//     |   - Use Case: Data logging, analysis, batch processing
//     |
//     |2. Real-time Mode:
//     |   - Bundle Size: Small (1-10 messages)
//     |   - Timeout: Short (1-50ms)
//     |   - Priority: Latency over Throughput
//     |   - Use Case: Interactive applications, live monitoring
//     |
//     |3. Adaptive Mode:
//     |   - Bundle Size: Dynamic (1-100 messages)
//     |   - Timeout: Adaptive (1-500ms)
//     |   - Priority: Balanced based on conditions
//     |   - Use Case: Variable network conditions, mixed workloads
//     |
//     |Auto-tuning Benefits:
//     |   - Automatically finds optimal parameters for current conditions
//     |   - Adapts to changing network/system performance
//     |   - Balances latency and throughput based on targets
//     |   - Reduces manual configuration and tuning
//     """.stripMargin)
//   }
// }

// // Example implementations of auto-tuning IOs

// class AutoTuningOSCIO extends AutoTuningIO {
//   // OSC-specific implementation with network optimization
//   override def sources: Map[String, Source[IOData, NotUsed]] = Map(
//     "joystick_updates" -> Source.repeat(FloatData(0.5f)).throttle(60, 1.second),
//     "button_events" -> Source.repeat(BooleanData(true)).throttle(10, 1.second),
//     "position_updates" -> Source.repeat(Vector3Data(1.0f, 2.0f, 3.0f)).throttle(30, 1.second)
//   )
  
//   override def sinks: Map[String, Sink[IOData, NotUsed]] = Map(
//     "joystick_updates" -> Sink.ignore,
//     "button_events" -> Sink.ignore,
//     "position_updates" -> Sink.ignore
//   )
// }

// class AutoTuningJoystickIO extends AutoTuningIO {
//   // Joystick-specific implementation with input optimization
//   override def sources: Map[String, Source[IOData, NotUsed]] = Map(
//     "leftStick" -> Source.repeat(Vector3Data(0.0f, 0.0f, 0.0f)).throttle(60, 1.second),
//     "rightStick" -> Source.repeat(Vector3Data(0.0f, 0.0f, 0.0f)).throttle(60, 1.second),
//     "triggers" -> Source.repeat(FloatData(0.0f)).throttle(120, 1.second)
//   )
  
//   override def sinks: Map[String, Sink[IOData, NotUsed]] = Map(
//     "leftStick" -> Sink.ignore,
//     "rightStick" -> Sink.ignore,
//     "triggers" -> Sink.ignore
//   )
// }

// class AutoTuningRecordingIO extends AutoTuningIO {
//   // Recording-specific implementation
//   override def sources: Map[String, Source[IOData, NotUsed]] = Map(
//     "all_data" -> Source.repeat(FloatData(0.0f)).throttle(1000, 1.second),
//     "realtime_monitor" -> Source.repeat(FloatData(0.0f)).throttle(100, 1.second)
//   )
  
//   override def sinks: Map[String, Sink[IOData, NotUsed]] = Map(
//     "all_data" -> Sink.ignore,
//     "realtime_monitor" -> Sink.ignore
//   )
// }

// class AutoTuningNetworkIO extends AutoTuningIO {
//   // Network-adaptive implementation
//   override def sources: Map[String, Source[IOData, NotUsed]] = Map(
//     "adaptive_stream" -> Source.repeat(FloatData(0.0f)).throttle(500, 1.second)
//   )
  
//   override def sinks: Map[String, Sink[IOData, NotUsed]] = Map(
//     "adaptive_stream" -> Sink.ignore
//   )
// }

// /**
//  * Main demonstration runner
//  */
// object AutoTuningDemo {
//   def main(args: Array[String]): Unit = {
//     println("Starting IO Auto-tuning Demonstrations")
//     println("=" * 50)
    
//     // Run all demonstrations
//     IOAutoTuningExamples.demonstrateOSCAutoTuning()
//     IOAutoTuningExamples.demonstrateJoystickAutoTuning()
//     IOAutoTuningExamples.demonstrateRecordingVsRealtime()
//     IOAutoTuningExamples.demonstrateNetworkAdaptation()
    
//     println("\n" + "=" * 50)
//     println("All demonstrations completed!")
//     println("\nKey Insights:")
//     println("- Auto-tuning automatically finds optimal bundle sizes and timeouts")
//     println("- System adapts to changing network and system conditions")
//     println("- Different use cases (real-time vs recording) have different optimal parameters")
//     println("- Performance profiling provides insights into latency vs throughput trade-offs")
//   }
// } 