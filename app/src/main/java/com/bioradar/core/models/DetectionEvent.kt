package com.bioradar.core.models

import java.util.UUID

/**
 * Detection event for logging and history
 */
data class DetectionEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val targets: List<RadarTarget>,
    val mode: OperatingMode,
    val deviceStable: Boolean,
    val batteryLevel: Int,
    val activeSensors: Set<DataSource>,
    val locationLabel: String? = null,  // e.g., "NORTH GATE"
    val encrypted: Boolean = false
)

/**
 * App operating modes
 */
enum class OperatingMode {
    NORMAL,      // Full features, standard battery usage
    EMERGENCY,   // Low power, minimal UI, extended runtime
    GUARD,       // Fixed position monitoring
    STEALTH,     // Silent operation, no emissions
    SEARCH,      // Aggressive scanning, high accuracy
    LAB,         // Debug mode with raw sensor data
    SENTRY,      // Automated perimeter protection
    ULTIMATE,    // Auto-maximize all capabilities (NEW)
    BLACKOUT     // Complete off-grid operation (NEW)
}

/**
 * Emergency mode sub-profiles
 */
enum class EmergencyProfile {
    SILENT_SENTRY,  // No sounds, haptic only
    GUARDIAN,       // Full sensors (when charging)
    RECON,          // Walking mode, self-motion compensation
    BLACKOUT        // Minimum power, maximum stealth
}
