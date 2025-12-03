package com.bioradar.core.models

/**
 * Complete mode profile configuration
 */
data class ModeProfile(
    val mode: OperatingMode,
    val scanIntervalMs: Long,
    val enabledSensors: Set<DataSource>,
    val uiBrightness: Float,           // 0.0-1.0
    val animationsEnabled: Boolean,
    val sonarEnabled: Boolean,
    val cameraEnabled: Boolean,
    val alertTypes: Set<AlertType>,
    val batteryThreshold: Int,         // Auto-downgrade below this %
    val description: String
)

/**
 * Predefined mode profiles
 */
object ModeProfiles {
    val NORMAL = ModeProfile(
        mode = OperatingMode.NORMAL,
        scanIntervalMs = 100,
        enabledSensors = DataSource.entries.toSet(),
        uiBrightness = 1.0f,
        animationsEnabled = true,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION, AlertType.VISUAL_ONLY),
        batteryThreshold = 20,
        description = "Full features, standard battery usage"
    )
    
    val EMERGENCY = ModeProfile(
        mode = OperatingMode.EMERGENCY,
        scanIntervalMs = 2000,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.ACCELEROMETER),
        uiBrightness = 0.1f,
        animationsEnabled = false,
        sonarEnabled = false,
        cameraEnabled = false,
        alertTypes = setOf(AlertType.VIBRATION_ONLY),
        batteryThreshold = 5,
        description = "Maximum battery life, minimal visibility"
    )
    
    val GUARD = ModeProfile(
        mode = OperatingMode.GUARD,
        scanIntervalMs = 500,
        enabledSensors = DataSource.entries.toSet(),
        uiBrightness = 0.3f,
        animationsEnabled = false,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION),
        batteryThreshold = 10,
        description = "Fixed position monitoring - plug in recommended"
    )
    
    val STEALTH = ModeProfile(
        mode = OperatingMode.STEALTH,
        scanIntervalMs = 1000,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.CAMERA),
        uiBrightness = 0.0f,
        animationsEnabled = false,
        sonarEnabled = false,  // No sound emission
        cameraEnabled = true,
        alertTypes = setOf(AlertType.VIBRATION_ONLY),
        batteryThreshold = 15,
        description = "Silent operation, no sound emissions"
    )
    
    val SEARCH = ModeProfile(
        mode = OperatingMode.SEARCH,
        scanIntervalMs = 50,
        enabledSensors = DataSource.entries.toSet(),
        uiBrightness = 0.8f,
        animationsEnabled = true,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION, AlertType.VISUAL_ONLY),
        batteryThreshold = 30,
        description = "Aggressive scanning, highest accuracy, high battery"
    )
    
    val LAB = ModeProfile(
        mode = OperatingMode.LAB,
        scanIntervalMs = 100,
        enabledSensors = DataSource.entries.toSet(),
        uiBrightness = 1.0f,
        animationsEnabled = true,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.VISUAL_ONLY),
        batteryThreshold = 20,
        description = "Debug mode - shows raw sensor data and graphs"
    )
    
    val SENTRY = ModeProfile(
        mode = OperatingMode.SENTRY,
        scanIntervalMs = 300,
        enabledSensors = DataSource.entries.toSet(),
        uiBrightness = 0.2f,
        animationsEnabled = false,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION, AlertType.FLASH_AND_VIBRATION),
        batteryThreshold = 15,
        description = "Automated perimeter protection"
    )
    
    /**
     * Get profile for a given mode
     */
    fun getProfile(mode: OperatingMode): ModeProfile = when (mode) {
        OperatingMode.NORMAL -> NORMAL
        OperatingMode.EMERGENCY -> EMERGENCY
        OperatingMode.GUARD -> GUARD
        OperatingMode.STEALTH -> STEALTH
        OperatingMode.SEARCH -> SEARCH
        OperatingMode.LAB -> LAB
        OperatingMode.SENTRY -> SENTRY
    }
    
    /**
     * All available profiles
     */
    val entries: List<ModeProfile> = listOf(NORMAL, EMERGENCY, GUARD, STEALTH, SEARCH, LAB, SENTRY)
}
