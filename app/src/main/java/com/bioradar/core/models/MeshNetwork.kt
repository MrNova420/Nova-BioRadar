package com.bioradar.core.models

/**
 * Mesh node representation
 */
data class MeshNode(
    val nodeId: String,
    val nodeName: String,              // User-friendly name
    val zoneName: String,              // Assigned location label
    val lastStatus: ZoneStatus,
    val lastAngle: Float?,
    val lastConfidence: Float,
    val lastUpdate: Long,
    val connectionType: ConnectionType,
    val batteryLevel: Int?,
    val isOnline: Boolean = true,
    val signalStrength: Int? = null    // Connection quality
)

/**
 * Connection types for mesh networking
 */
enum class ConnectionType {
    WIFI_DIRECT,
    BLUETOOTH,
    LOCAL_HOTSPOT,
    WIRED_USB          // Future: USB OTG connection
}

/**
 * Compact mesh alert message (for transmission)
 */
data class MeshAlert(
    val nodeId: String,
    val time: Long,
    val zone: String,
    val level: ZoneStatus,
    val angle: Float?,
    val confidence: Float,
    val batteryLevel: Int? = null
)

/**
 * Network status
 */
data class NetworkStatus(
    val role: MeshRole,
    val connectedNodes: List<MeshNode>,
    val isHealthy: Boolean
)

/**
 * Mesh network roles
 */
enum class MeshRole {
    STANDALONE,  // Not connected to mesh
    HUB,         // Central coordinator
    NODE         // Remote sensor node
}
