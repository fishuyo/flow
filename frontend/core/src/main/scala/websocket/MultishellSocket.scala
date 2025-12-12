package client
package websocket

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom
import org.scalajs.dom.WebSocket
import upickle.default._
import multishell.protocol._

object MultishellSocket {
  private var ws: Option[WebSocket] = None
  private val outputBus = new EventBus[Message]
  private val connectionStatus = Var(false)
  
  // Default instance ID for single-shell mode (until we add instance management UI)
  private val defaultInstanceId = "default"
  
  // Track shell instances that need to be recreated on reconnection
  private val shellInstances = scala.collection.mutable.Set[String]()
  private var keepaliveInterval: Option[Int] = None
  
  // Track if we're currently connecting to prevent multiple simultaneous connection attempts
  private var isConnecting = false

  val outputStream: EventStream[Message] = outputBus.events
  val isConnected: Signal[Boolean] = connectionStatus.signal
  
  // Track connection state to prevent duplicate updates
  private var lastConnectionState = false
  
  private def startKeepalive(): Unit = {
    // Clear existing keepalive if any
    keepaliveInterval.foreach(dom.window.clearInterval)
    
    // Send keepalive every 15 seconds to prevent timeout (more frequent)
    val intervalId = dom.window.setInterval(() => {
      ws.foreach { socket =>
        if (socket.readyState == WebSocket.OPEN) {
          try {
            socket.send("keepalive")
            dom.console.log("[MultishellSocket] Keepalive sent")
          } catch {
            case e: Exception =>
              dom.console.error(s"[MultishellSocket] Error sending keepalive: $e")
              // If keepalive fails, connection might be dead
              stopKeepalive()
          }
        } else {
          // Socket not open, stop keepalive
          stopKeepalive()
        }
      }
    }, 15000) // 15 seconds - more frequent to prevent timeouts
    
    keepaliveInterval = Some(intervalId)
    dom.console.log("[MultishellSocket] Keepalive started (15s interval)")
  }
  
  private def stopKeepalive(): Unit = {
    keepaliveInterval.foreach(dom.window.clearInterval)
    keepaliveInterval = None
  }
  
  private def recreateShellInstances(): Unit = {
    if (shellInstances.isEmpty) {
      dom.console.log("[MultishellSocket] No shell instances to recreate")
      return
    }
    
    val instancesList = shellInstances.toSeq
    dom.console.log(s"[MultishellSocket] Recreating ${instancesList.size} shell instances")
    
    instancesList.zipWithIndex.foreach { case (instanceId, index) =>
      // Add a small delay between recreations to avoid overwhelming the server
      dom.window.setTimeout(() => {
        if (ws.exists(_.readyState == WebSocket.OPEN)) {
          createShell(instanceId, None)
        }
      }, 100 * index)
    }
  }
  
  // Track which instances we've already attempted to create to prevent duplicates
  private val creationAttempts = scala.collection.mutable.Set[String]()
  
  def createShell(instanceId: String, hostConfig: Option[HostConfig]): Unit = {
    // Track this instance for reconnection
    shellInstances.add(instanceId)
    
    // Prevent duplicate creation attempts
    if (creationAttempts.contains(instanceId)) {
      dom.console.log(s"[MultishellSocket] Shell instance '$instanceId' creation already in progress, skipping")
      return
    }
    
    creationAttempts.add(instanceId)
    dom.console.log(s"[MultishellSocket] Creating shell instance: $instanceId")
    
    ws match {
      case Some(socket) =>
        val state = socket.readyState
        if (state == WebSocket.OPEN) {
          try {
            val message = write(CreateShell(instanceId, hostConfig))
            socket.send(message)
            dom.console.log(s"[MultishellSocket] CreateShell message sent for instance: $instanceId")
          } catch {
            case e: Exception =>
              creationAttempts.remove(instanceId) // Allow retry on error
              dom.console.error(s"[MultishellSocket] Error creating shell: $e")
              e.printStackTrace()
          }
        } else {
          creationAttempts.remove(instanceId) // Allow retry
          dom.console.log(s"[MultishellSocket] WebSocket not connected (state=$state) for instance $instanceId")
          MultishellSocket.connect()
          dom.window.setTimeout(() => createShell(instanceId, hostConfig), 1000)
        }
      case None =>
        creationAttempts.remove(instanceId) // Allow retry
        dom.console.log(s"[MultishellSocket] No WebSocket for instance $instanceId")
        MultishellSocket.connect()
        // Wait longer for connection to establish before retrying
        dom.window.setTimeout(() => createShell(instanceId, hostConfig), 1000)
    }
  }

  def connect(): Unit = {
    // If already connected, do nothing
    if (ws.exists(_.readyState == WebSocket.OPEN)) {
      dom.console.log("[MultishellSocket] WebSocket already connected, skipping")
      return
    }
    
    // If already connecting, do nothing (prevent multiple simultaneous connection attempts)
    if (isConnecting) {
      dom.console.log("[MultishellSocket] Connection already in progress, skipping duplicate call")
      return
    }
    
    // If connection exists but is closing, wait for it to close
    if (ws.exists(s => s.readyState == WebSocket.CLOSING)) {
      dom.console.log("[MultishellSocket] WebSocket is closing, will reconnect when closed")
      return
    }
    
    // If connection exists but is connecting, wait
    if (ws.exists(s => s.readyState == WebSocket.CONNECTING)) {
      dom.console.log("[MultishellSocket] WebSocket is connecting, waiting...")
      return
    }
    
    isConnecting = true
    dom.console.log("[MultishellSocket] Starting new WebSocket connection...")
    
    val protocol = if (dom.window.location.protocol == "https:") "wss" else "ws"
    val host = dom.window.location.host
    val url = s"$protocol://$host/api/multishell/ws"
    
    dom.console.log(s"[MultishellSocket] Connecting to WebSocket: $url")
    
    try {
      val socket = new WebSocket(url)
      
      socket.onopen = { _ =>
        dom.console.log("[MultishellSocket] WebSocket connected successfully")
        isConnecting = false
        
        // Only update connection status if it actually changed (prevents duplicate signals)
        if (!lastConnectionState) {
          lastConnectionState = true
          connectionStatus.set(true)
        }
        
        ws = Some(socket)
        
        // Clear creation attempts on new connection to allow fresh creation
        creationAttempts.clear()
        
        // Start keepalive to prevent timeout
        startKeepalive()
        
        // Only recreate instances if we had instances before (reconnection scenario)
        // On fresh page load, shellInstances will be empty, so XTerm components will create them
        if (shellInstances.nonEmpty) {
          dom.window.setTimeout(() => {
            recreateShellInstances()
          }, 100) // Small delay to ensure connection is fully established
        }
      }
      
      socket.onmessage = { event =>
          try {
            val rawMessage = event.data.toString
            
            // Skip keepalive responses and empty messages
            if (rawMessage != "keepalive" && rawMessage.trim.nonEmpty) {
              dom.console.log(s"[MultishellSocket] Received message: $rawMessage")
              val message = read[Message](rawMessage)
              dom.console.log(s"[MultishellSocket] Parsed message: $message")
              
              // Handle ShellError messages - if instance not found, try to recreate it
              message match {
                case m: ShellError if m.text.contains("not found") =>
                  dom.console.log(s"[MultishellSocket] Shell instance '${m.instanceId}' not found, attempting to recreate...")
                  // Ensure instance is tracked and try to recreate
                  shellInstances.add(m.instanceId)
                  // Only recreate if we're connected
                  if (ws.exists(_.readyState == WebSocket.OPEN)) {
                    createShell(m.instanceId, None)
                  }
                
                case _ =>
                  // Emit all messages normally
                  outputBus.emit(message)
              }
            }
          } catch {
            case e: Exception =>
              dom.console.error(s"[MultishellSocket] Error parsing WebSocket message: $e")
              e.printStackTrace()
          }
        }
        
        socket.onerror = { event =>
          dom.console.error(s"[MultishellSocket] WebSocket error: $event")
          isConnecting = false
          // Only update if state actually changed
          if (lastConnectionState) {
            lastConnectionState = false
            connectionStatus.set(false)
          }
        }
        
        socket.onclose = { event =>
          dom.console.log(s"[MultishellSocket] WebSocket closed: code=${event.code}, reason=${event.reason}, wasClean=${event.wasClean}")
          isConnecting = false
          // Only update if state actually changed
          if (lastConnectionState) {
            lastConnectionState = false
            connectionStatus.set(false)
          }
          ws = None
          stopKeepalive()
          
          // Attempt to reconnect after a delay if not a clean close
          // Only reconnect if we have instances to reconnect (not a fresh page load)
          if (!event.wasClean && shellInstances.nonEmpty) {
            dom.console.log("[MultishellSocket] Connection lost, attempting to reconnect in 2 seconds...")
            dom.window.setTimeout(() => {
              connect()
            }, 2000)
          }
        }
      } catch {
        case e: Exception =>
          dom.console.error(s"[MultishellSocket] Failed to create WebSocket: $e")
          e.printStackTrace()
          isConnecting = false
          connectionStatus.set(false)
      }
  }

  def disconnect(): Unit = {
    stopKeepalive()
    ws.foreach(_.close())
    ws = None
    if (lastConnectionState) {
      lastConnectionState = false
      connectionStatus.set(false)
    }
  }

  def sendInput(instanceId: String, data: String): Unit = {
    // Ensure instance is tracked
    shellInstances.add(instanceId)
    
    dom.console.log(s"[MultishellSocket] sendInput called for instance $instanceId with: '${data.replaceAll("\r", "\\r").replaceAll("\n", "\\n")}'")
    ws match {
      case Some(socket) =>
        val state = socket.readyState
        if (state == WebSocket.OPEN) {
          try {
            val message = write(ShellInput(instanceId, data))
            socket.send(message)
          } catch {
            case e: Exception =>
              dom.console.error(s"[MultishellSocket] Error sending input: $e")
              e.printStackTrace()
          }
        } else {
          dom.console.log(s"[MultishellSocket] WebSocket not connected (state=$state), attempting to reconnect...")
          connect()
          dom.window.setTimeout(() => sendInput(instanceId, data), 500)
        }
      case None =>
        dom.console.log("[MultishellSocket] No WebSocket, connecting first...")
        connect()
        dom.window.setTimeout(() => sendInput(instanceId, data), 500)
    }
  }

  def sendResize(instanceId: String, rows: Int, cols: Int): Unit = {
    dom.console.log(s"[MultishellSocket] sendResize called for instance $instanceId with: ${rows}x${cols}")
    ws match {
      case Some(socket) =>
        val state = socket.readyState
        if (state == WebSocket.OPEN) {
          try {
            val message = write(ShellResize(instanceId, rows, cols))
            socket.send(message)
            dom.console.log(s"[MultishellSocket] Resize message sent successfully for instance: $instanceId")
          } catch {
            case e: Exception =>
              dom.console.error(s"[MultishellSocket] Error sending resize: $e")
              e.printStackTrace()
          }
        } else {
          dom.console.log(s"[MultishellSocket] WebSocket not connected (state=$state), attempting to reconnect...")
          connect()
          dom.window.setTimeout(() => sendResize(instanceId, rows, cols), 500)
        }
      case None =>
        dom.console.log("[MultishellSocket] No WebSocket, connecting first...")
        connect()
        dom.window.setTimeout(() => sendResize(instanceId, rows, cols), 500)
    }
  }

  // Backward compatibility methods using default instance
  def sendInput(data: String): Unit = sendInput(defaultInstanceId, data)
  def sendResize(rows: Int, cols: Int): Unit = sendResize(defaultInstanceId, rows, cols)
  def sendCommand(data: String): Unit = sendInput(defaultInstanceId, data)
}

