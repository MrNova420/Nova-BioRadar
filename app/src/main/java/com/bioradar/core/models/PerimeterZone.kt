package com.bioradar.core.models

import java.util.UUID

/**
 * Perimeter zone configuration
 */
data class PerimeterZone(
    val id: String = UUID.randomUUID().toString(),
    val name: String,                    // "NORTH GATE", "STAIRS 2F"
    val monitoringSector: Sector,
    val sensitivity: Sensitivity,
    val baselineCalibrated: Boolean = false,
    val baselineData: SensorBaseline? = null,
    val alertType: AlertType,
    val createdAt: Long = System.currentTimeMillis(),
    val activeSensors: Set<DataSource> = DataSource.entries.toSet()
) {
    fun getScanInterval(): Long = when (sensitivity) {
        Sensitivity.LOW -> 1000L
        Sensitivity.MEDIUM -> 500L
        Sensitivity.HIGH -> 200L
        Sensitivity.CUSTOM -> 500L
    }

    fun getThresholds(): AlertThresholds = sensitivity.getThresholds()
}

/**
 * Monitoring sector options
 */
enum class Sector {
    FORWARD_CONE,    // ~90° front
    LEFT_SECTOR,     // ~90° left
    RIGHT_SECTOR,    // ~90° right
    REAR_SECTOR,     // ~90° back
    FRONT_WIDE,      // ~180° front
    FULL_360         // All directions
}

/**
 * Detection sensitivity levels
 */
enum class Sensitivity {
    LOW,      // Fewer false positives, may miss subtle movement
    MEDIUM,   // Balanced
    HIGH,     // More sensitive, more false positives
    CUSTOM    // User-defined thresholds
}

/**
 * Alert type options
 */
enum class AlertType {
    SOUND_AND_VIBRATION,
    VIBRATION_ONLY,
    SILENT_LOG_ONLY,
    VISUAL_ONLY,
    FLASH_AND_VIBRATION
}

/**
 * Sensor baseline for calibration
 */
data class SensorBaseline(
    val avgRssiVariance: Float,
    val avgSonarEnergy: Float,
    val avgCameraMotion: Float,
    val ambientNoiseLevel: Float,
    val calibrationTime: Long,
    val sampleCount: Int,
    val environmentType: String? = null  // "indoor", "outdoor", etc.
)

/**
 * Zone alert status
 */
enum class ZoneStatus {
    GREEN_CLEAR,       // No activity detected
    YELLOW_POSSIBLE,   // Low-level movement/noise
    RED_PRESENCE,      // Strong presence detected
    UNKNOWN            // Calibrating or error
}

/**
 * Alert thresholds configuration
 */
data class AlertThresholds(
    val yellow: Float,  // Possible movement threshold
    val red: Float      // Confirmed presence threshold
)

/**
 * Extension to get thresholds from sensitivity
 */
fun Sensitivity.getThresholds(): AlertThresholds {
    return when (this) {
        Sensitivity.LOW -> AlertThresholds(yellow = 40f, red = 70f)
        Sensitivity.MEDIUM -> AlertThresholds(yellow = 25f, red = 50f)
        Sensitivity.HIGH -> AlertThresholds(yellow = 15f, red = 35f)
        Sensitivity.CUSTOM -> AlertThresholds(yellow = 20f, red = 45f)
    }
}
