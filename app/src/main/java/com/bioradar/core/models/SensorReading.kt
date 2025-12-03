package com.bioradar.core.models

/**
 * Generic sensor reading wrapper
 */
data class SensorReading(
    val source: DataSource,
    val timestamp: Long = System.currentTimeMillis(),
    val values: FloatArray,
    val metadata: Map<String, Any> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SensorReading
        return source == other.source && 
               timestamp == other.timestamp && 
               values.contentEquals(other.values)
    }
    
    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }
}

/**
 * Bluetooth device reading with RSSI
 */
data class BleDeviceReading(
    val address: String,
    val name: String?,
    val rssi: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * WiFi access point reading
 */
data class WifiApReading(
    val bssid: String,
    val ssid: String?,
    val rssi: Int,
    val frequency: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Sonar echo reading
 */
data class SonarReading(
    val echoDistance: Float?,      // Estimated distance in meters
    val echoStrength: Float,       // Echo amplitude 0.0-1.0
    val dopplerShift: Float?,      // Frequency shift for motion
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Camera motion reading
 */
data class CameraMotionReading(
    val sector: Int,               // 0-7 for 8 sectors
    val motionMagnitude: Float,    // 0.0-1.0
    val angleDegrees: Float,       // Direction of motion
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * UWB ranging reading
 */
data class UwbReading(
    val distanceMeters: Float,
    val azimuthDegrees: Float,
    val elevationDegrees: Float?,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)
