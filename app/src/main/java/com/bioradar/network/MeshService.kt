package com.bioradar.network

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.bioradar.BioRadarApplication
import com.bioradar.MainActivity
import com.bioradar.R
import com.bioradar.core.models.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import javax.inject.Inject

/**
 * Mesh Network Service for multi-device coordination
 */
@AndroidEntryPoint
class MeshService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    
    private val _role = MutableStateFlow(MeshRole.STANDALONE)
    val role: StateFlow<MeshRole> = _role.asStateFlow()
    
    private val _connectedNodes = MutableStateFlow<Map<String, MeshNode>>(emptyMap())
    val connectedNodes: StateFlow<Map<String, MeshNode>> = _connectedNodes.asStateFlow()
    
    private val alertHandler: (MeshAlert) -> Unit = { alert ->
        // Broadcast alert to the UI
        val intent = Intent(ACTION_MESH_ALERT).apply {
            putExtra("node_id", alert.nodeId)
            putExtra("zone", alert.zone)
            putExtra("level", alert.level.name)
            putExtra("confidence", alert.confidence)
        }
        sendBroadcast(intent)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        channel = wifiP2pManager?.initialize(this, Looper.getMainLooper(), null)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CREATE_HUB -> {
                val networkName = intent.getStringExtra(EXTRA_NETWORK_NAME) ?: "BioRadar-Hub"
                initializeAsHub(networkName)
            }
            ACTION_JOIN_NETWORK -> {
                val hubAddress = intent.getStringExtra(EXTRA_HUB_ADDRESS) ?: return START_NOT_STICKY
                val nodeName = intent.getStringExtra(EXTRA_NODE_NAME) ?: "Node-${UUID.randomUUID().toString().take(8)}"
                initializeAsNode(hubAddress, nodeName)
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        serviceScope.cancel()
    }
    
    /**
     * Initialize as mesh hub
     */
    private fun initializeAsHub(networkName: String) {
        startForeground(NOTIFICATION_ID, createNotification("Hub: $networkName"))
        _role.value = MeshRole.HUB
        isRunning = true
        
        // Create WiFi P2P group
        try {
            wifiP2pManager?.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    startListeningForNodes()
                }
                
                override fun onFailure(reason: Int) {
                    // Fallback to simple socket server
                    startListeningForNodes()
                }
            })
        } catch (e: SecurityException) {
            // Handle permission error
            startListeningForNodes()
        }
    }
    
    /**
     * Initialize as mesh node
     */
    private fun initializeAsNode(hubAddress: String, nodeName: String) {
        startForeground(NOTIFICATION_ID, createNotification("Node: $nodeName"))
        _role.value = MeshRole.NODE
        isRunning = true
        
        serviceScope.launch {
            try {
                connectToHub(hubAddress, nodeName)
            } catch (e: Exception) {
                // Handle connection error
                disconnect()
            }
        }
    }
    
    /**
     * Start listening for incoming node connections
     */
    private fun startListeningForNodes() {
        serviceScope.launch {
            try {
                serverSocket = ServerSocket(MESH_PORT)
                
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: continue
                        handleNodeConnection(clientSocket)
                    } catch (e: Exception) {
                        if (isRunning) {
                            delay(1000) // Retry after error
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle server error
            }
        }
    }
    
    /**
     * Handle incoming node connection
     */
    private fun handleNodeConnection(socket: Socket) {
        serviceScope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                val writer = PrintWriter(socket.outputStream, true)
                
                // Initial handshake
                val nodeInfo = reader.readLine()
                val nodeId = UUID.randomUUID().toString()
                val zoneName = nodeInfo ?: "Unknown"
                
                // Add to connected nodes
                val node = MeshNode(
                    nodeId = nodeId,
                    nodeName = "Node-${nodeId.take(8)}",
                    zoneName = zoneName,
                    lastStatus = ZoneStatus.GREEN_CLEAR,
                    lastAngle = null,
                    lastConfidence = 0f,
                    lastUpdate = System.currentTimeMillis(),
                    connectionType = ConnectionType.WIFI_DIRECT,
                    batteryLevel = null,
                    isOnline = true
                )
                
                _connectedNodes.value = _connectedNodes.value + (nodeId to node)
                
                // Send acknowledgment
                writer.println("CONNECTED:$nodeId")
                
                // Listen for updates
                while (isRunning && !socket.isClosed) {
                    try {
                        val message = reader.readLine() ?: break
                        processNodeMessage(nodeId, message)
                    } catch (e: Exception) {
                        break
                    }
                }
                
                // Remove node on disconnect
                _connectedNodes.value = _connectedNodes.value - nodeId
                
            } catch (e: Exception) {
                // Handle connection error
            }
        }
    }
    
    /**
     * Process incoming message from a node
     */
    private fun processNodeMessage(nodeId: String, message: String) {
        // Parse message format: STATUS:LEVEL:ANGLE:CONFIDENCE:BATTERY
        val parts = message.split(":")
        if (parts.size >= 4) {
            val status = try { ZoneStatus.valueOf(parts[0]) } catch (e: Exception) { ZoneStatus.UNKNOWN }
            val angle = parts[1].toFloatOrNull()
            val confidence = parts[2].toFloatOrNull() ?: 0f
            val battery = parts.getOrNull(3)?.toIntOrNull()
            
            // Update node
            val currentNode = _connectedNodes.value[nodeId] ?: return
            val updatedNode = currentNode.copy(
                lastStatus = status,
                lastAngle = angle,
                lastConfidence = confidence,
                lastUpdate = System.currentTimeMillis(),
                batteryLevel = battery
            )
            
            _connectedNodes.value = _connectedNodes.value + (nodeId to updatedNode)
            
            // Create and handle alert
            val alert = MeshAlert(
                nodeId = nodeId,
                time = System.currentTimeMillis(),
                zone = currentNode.zoneName,
                level = status,
                angle = angle,
                confidence = confidence,
                batteryLevel = battery
            )
            
            alertHandler(alert)
        }
    }
    
    /**
     * Connect to hub as a node
     */
    private suspend fun connectToHub(hubAddress: String, nodeName: String) {
        try {
            val socket = Socket(hubAddress, MESH_PORT)
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            val writer = PrintWriter(socket.outputStream, true)
            
            // Send node info
            writer.println(nodeName)
            
            // Wait for acknowledgment
            val response = reader.readLine()
            if (response?.startsWith("CONNECTED:") == true) {
                val nodeId = response.substringAfter("CONNECTED:")
                
                // Start sending updates
                while (isRunning && !socket.isClosed) {
                    // Send periodic status updates
                    val status = "GREEN_CLEAR:0:0.5:85"
                    writer.println(status)
                    delay(5000)
                }
            }
            
            socket.close()
        } catch (e: Exception) {
            throw e
        }
    }
    
    /**
     * Send alert to hub (from node)
     */
    fun sendAlert(alert: MeshAlert) {
        if (_role.value != MeshRole.NODE) return
        // Send via existing connection
    }
    
    /**
     * Broadcast alert to all nodes (from hub)
     */
    fun broadcastAlert(alert: MeshAlert) {
        if (_role.value != MeshRole.HUB) return
        // Broadcast to all connected nodes
    }
    
    /**
     * Disconnect from mesh
     */
    private fun disconnect() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
        
        try {
            wifiP2pManager?.removeGroup(channel, null)
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        
        _role.value = MeshRole.STANDALONE
        _connectedNodes.value = emptyMap()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, BioRadarApplication.NOTIFICATION_CHANNEL_RADAR)
            .setContentTitle("BioRadar Mesh")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    companion object {
        const val NOTIFICATION_ID = 1002
        const val MESH_PORT = 19420
        
        const val ACTION_CREATE_HUB = "com.bioradar.mesh.CREATE_HUB"
        const val ACTION_JOIN_NETWORK = "com.bioradar.mesh.JOIN_NETWORK"
        const val ACTION_DISCONNECT = "com.bioradar.mesh.DISCONNECT"
        const val ACTION_MESH_ALERT = "com.bioradar.mesh.ALERT"
        
        const val EXTRA_NETWORK_NAME = "network_name"
        const val EXTRA_HUB_ADDRESS = "hub_address"
        const val EXTRA_NODE_NAME = "node_name"
    }
}
