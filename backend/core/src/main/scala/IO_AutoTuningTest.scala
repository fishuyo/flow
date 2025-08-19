// package flow

// import scala.concurrent.duration._
// import scala.util.{Try, Success, Failure}
// import org.apache.pekko.stream.scaladsl._
// import org.apache.pekko.stream._
// import java.time.Instant

// /**
//  * Simple test demonstrating auto-tuning in action
//  */
// object IOAutoTuningTest {
  
//   def main(args: Array[String]): Unit = {
//     println("=== IO Auto-tuning Test ===")
    
//     // Test 1: Basic auto-tuning demonstration
//     testBasicAutoTuning()
    
//     // Test 2: Latency vs throughput trade-off visualization
//     testLatencyThroughputTradeoff()
    
//     // Test 3: Network condition adaptation
//     testNetworkAdaptation()
    
//     println("\n=== All tests completed ===")
//   }
  
//   def testBasicAutoTuning(): Unit = {
//     println("\n--- Test 1: Basic Auto-tuning ---")
    
//     val oscIO = new TestOSCIO()
    
//     // Configure auto-tuning
//     val config = AutoTuningConfig(
//       enabled = true,
//       adaptationInterval = 2.seconds,
//       minBundleSize = 1,
//       maxBundleSize = 20,
//       minBundleTimeout = 5.millis,
//       maxBundleTimeout = 100.millis,
//       targetLatencyMs = 30.0,
//       targetThroughputPerSec = 500.0,
//       explorationRate = 0.2,
//       learningRate = 0.3,
//       historySize = 50,
//       stabilityThreshold = 3
//     )
    
//     oscIO.configureAutoTuning("test_stream", config)
    
//     // Start with conservative parameters
//     oscIO.setBundlingParams("test_stream", BundlingParams(5, 25.millis))
    
//     println("Initial configuration:")
//     println(s"  Bundle Size: 5")
//     println(s"  Bundle Timeout: 25ms")
//     println(s"  Target Latency: 30ms")
//     println(s"  Target Throughput: 500 msg/sec")
    
//     // Simulate message flow for 10 seconds
//     println("\nRunning auto-tuning for 10 seconds...")
//     val startTime = System.currentTimeMillis()
    
//     while (System.currentTimeMillis() - startTime < 10000) {
//       Thread.sleep(500) // Check every 500ms
      
//       oscIO.getBundlingParams("test_stream").foreach { params =>
//         val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
//         println(f"[${elapsed}%.1fs] Bundle: ${params.bundleSize}, Timeout: ${params.bundleTimeout.toMillis}ms")
//       }
//     }
    
//     println("\nFinal configuration:")
//     oscIO.getBundlingParams("test_stream").foreach { params =>
//       println(s"  Bundle Size: ${params.bundleSize}")
//       println(s"  Bundle Timeout: ${params.bundleTimeout.toMillis}ms")
//     }
//   }
  
//   def testLatencyThroughputTradeoff(): Unit = {
//     println("\n--- Test 2: Latency vs Throughput Trade-off ---")
    
//     val testIO = new TestOSCIO()
    
//     // Test different bundle configurations
//     val configurations = List(
//       ("Small Bundle (Low Latency)", BundlingParams(1, 5.millis)),
//       ("Medium Bundle (Balanced)", BundlingParams(10, 25.millis)),
//       ("Large Bundle (High Throughput)", BundlingParams(50, 100.millis))
//     )
    
//     configurations.foreach { case (name, params) =>
//       println(s"\n--- Testing $name ---")
//       testIO.setBundlingParams("test_stream", params)
      
//       // Simulate performance for this configuration
//       val (latency, throughput, efficiency) = simulatePerformance(params)
      
//       println(f"  Bundle Size: ${params.bundleSize}")
//       println(f"  Bundle Timeout: ${params.bundleTimeout.toMillis}ms")
//       println(f"  Simulated Latency: ${latency}%.1fms")
//       println(f"  Simulated Throughput: ${throughput}%.0f msg/sec")
//       println(f"  Efficiency Score: ${efficiency}%.3f")
      
//       // Analyze trade-off
//       analyzeTradeoff(params, latency, throughput)
//     }
//   }
  
//   def testNetworkAdaptation(): Unit = {
//     println("\n--- Test 3: Network Condition Adaptation ---")
    
//     val adaptiveIO = new TestOSCIO()
    
//     // Configure for adaptive behavior
//     val adaptiveConfig = AutoTuningConfig(
//       enabled = true,
//       adaptationInterval = 1.second,
//       minBundleSize = 1,
//       maxBundleSize = 100,
//       minBundleTimeout = 1.millis,
//       maxBundleTimeout = 500.millis,
//       targetLatencyMs = 50.0,
//       targetThroughputPerSec = 1000.0,
//       explorationRate = 0.3,
//       learningRate = 0.4,
//       historySize = 100,
//       stabilityThreshold = 2
//     )
    
//     adaptiveIO.configureAutoTuning("adaptive_stream", adaptiveConfig)
//     adaptiveIO.setBundlingParams("adaptive_stream", BundlingParams(10, 50.millis))
    
//     // Simulate changing network conditions
//     val networkConditions = List(
//       ("Good Network", 5.0, 100.0, 0.001),
//       ("Poor Network", 50.0, 10.0, 0.1),
//       ("Variable Network", 25.0, 50.0, 0.05)
//     )
    
//     networkConditions.foreach { case (condition, latencyMs, bandwidthMBps, lossRate) =>
//       println(s"\n--- Simulating $condition ---")
//       println(s"  Network Latency: ${latencyMs}ms")
//       println(s"  Network Bandwidth: ${bandwidthMBps}MB/s")
//       println(s"  Packet Loss: ${lossRate * 100}%")
      
//       // Simulate adaptation for 5 seconds
//       val startTime = System.currentTimeMillis()
//       while (System.currentTimeMillis() - startTime < 5000) {
//         Thread.sleep(1000)
        
//         adaptiveIO.getBundlingParams("adaptive_stream").foreach { params =>
//           val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
//           println(f"  [${elapsed}%.0fs] Bundle: ${params.bundleSize}, Timeout: ${params.bundleTimeout.toMillis}ms")
//         }
//       }
//     }
//   }
  
//   // Helper methods
  
//   private def simulatePerformance(params: BundlingParams): (Double, Double, Double) = {
//     // Simple performance simulation based on bundle parameters
//     val bundleSize = params.bundleSize
//     val timeoutMs = params.bundleTimeout.toMillis
    
//     // Latency increases with bundle size and timeout
//     val latency = 5.0 + (bundleSize * 0.5) + (timeoutMs * 0.3)
    
//     // Throughput increases with bundle size but decreases with timeout
//     val throughput = (bundleSize * 1000.0) / (timeoutMs + 10.0)
    
//     // Efficiency score (higher is better)
//     val latencyScore = 1.0 / (1.0 + latency / 100.0)
//     val throughputScore = math.min(throughput / 1000.0, 1.0)
//     val efficiency = (latencyScore * 0.4 + throughputScore * 0.6)
    
//     (latency, throughput, efficiency)
//   }
  
//   private def analyzeTradeoff(params: BundlingParams, latency: Double, throughput: Double): Unit = {
//     println("  Trade-off Analysis:")
    
//     if (params.bundleSize <= 5) {
//       println("    → Optimized for low latency")
//       println("    → Good for real-time applications")
//       println("    → May sacrifice some throughput")
//     } else if (params.bundleSize <= 20) {
//       println("    → Balanced latency and throughput")
//       println("    → Good for general-purpose use")
//       println("    → Moderate network efficiency")
//     } else {
//       println("    → Optimized for high throughput")
//       println("    → Good for data recording/batching")
//       println("    → May have higher latency")
//     }
    
//     if (latency < 20) {
//       println("    → Excellent latency performance")
//     } else if (latency < 50) {
//       println("    → Good latency performance")
//     } else {
//       println("    → Latency may be too high for real-time use")
//     }
    
//     if (throughput > 800) {
//       println("    → Excellent throughput performance")
//     } else if (throughput > 400) {
//       println("    → Good throughput performance")
//     } else {
//       println("    → Throughput may be insufficient for high-volume data")
//     }
//   }
// }

// // Test implementation
// class TestOSCIO extends AutoTuningIO {
//   override def sources: Map[String, Source[IOData, NotUsed]] = Map(
//     "test_stream" -> Source.repeat(FloatData(0.5f)).throttle(100, 1.second),
//     "adaptive_stream" -> Source.repeat(FloatData(0.5f)).throttle(200, 1.second)
//   )
  
//   override def sinks: Map[String, Sink[IOData, NotUsed]] = Map(
//     "test_stream" -> Sink.ignore,
//     "adaptive_stream" -> Sink.ignore
//   )
// }

// /**
//  * Quick demonstration of the key concepts
//  */
// object QuickDemo {
//   def main(args: Array[String]): Unit = {
//     println("=== Quick Auto-tuning Demo ===")
    
//     // Show the trade-off in action
//     println("\nLatency vs Throughput Trade-off Examples:")
    
//     val examples = List(
//       ("Real-time Gaming", 1, 5, "Ultra-low latency, moderate throughput"),
//       ("Live Streaming", 5, 20, "Low latency, good throughput"),
//       ("Data Logging", 50, 200, "High throughput, acceptable latency"),
//       ("Batch Processing", 200, 1000, "Maximum throughput, high latency")
//     )
    
//     examples.foreach { case (useCase, bundleSize, timeoutMs, description) =>
//       val (latency, throughput, _) = simulatePerformance(BundlingParams(bundleSize, timeoutMs.millis))
//       println(f"  $useCase: Bundle=$bundleSize, Timeout=${timeoutMs}ms")
//       println(f"    → Latency: ${latency}%.1fms, Throughput: ${throughput}%.0f msg/sec")
//       println(f"    → $description")
//     }
    
//     println("\nAuto-tuning automatically finds the optimal balance!")
//     println("Run IOAutoTuningTest.main() for a full demonstration.")
//   }
  
//   private def simulatePerformance(params: BundlingParams): (Double, Double, Double) = {
//     val bundleSize = params.bundleSize
//     val timeoutMs = params.bundleTimeout.toMillis
//     val latency = 5.0 + (bundleSize * 0.5) + (timeoutMs * 0.3)
//     val throughput = (bundleSize * 1000.0) / (timeoutMs + 10.0)
//     val latencyScore = 1.0 / (1.0 + latency / 100.0)
//     val throughputScore = math.min(throughput / 1000.0, 1.0)
//     val efficiency = (latencyScore * 0.4 + throughputScore * 0.6)
//     (latency, throughput, efficiency)
//   }
// } 