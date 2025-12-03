package com.bioradar.core.models

import java.util.UUID

/**
 * Represents a detected presence/target on the radar
 */
data class RadarTarget(
    val id: String = UUID.randomUUID().toString(),
    val angleDegrees: Float,           // 0-360 bearing from device
    val distanceMeters: Float?,        // null if unknown
    val confidence: Float,             // 0.0-1.0
    val type: TargetType,
    val velocity: Float? = null,       // m/s, null if unknown
    val isMoving: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val dataSources: Set<DataSource> = emptySet(),
    val signalStrength: Float? = null  // Combined signal strength
)

/**
 * Classification of detected targets
 */
enum class TargetType {
    HUMAN,           // High confidence human presence
    POSSIBLE_LIFE,   // Likely living but uncertain
    NOISE,           // Environmental noise/interference
    UNKNOWN,         // Unclassified detection
    VEHICLE,         // Large moving object (future)
    ANIMAL           // Non-human life form (future ML)
}

/**
 * Available sensor data sources
 */
enum class DataSource {
    WIFI,
    BLUETOOTH,
    SONAR,
    CAMERA,
    UWB,
    ACCELEROMETER,
    MAGNETOMETER,
    EXTERNAL_MODULE
}
