package com.bioradar.modes

import com.bioradar.core.models.*

/**
 * Screen wake mode for emergency profiles
 */
enum class ScreenWakeMode {
    ALWAYS_ON,      // Screen always visible
    PULSE,          // Periodic wake (every N seconds)
    ON_ALERT_ONLY,  // Wake only when alert triggered
    NEVER           // Screen always off
}

/**
 * Emergency mode configuration
 */
data class EmergencyConfig(
    val name: String,
    val description: String,
    val sonarEnabled: Boolean = true,
    val cameraEnabled: Boolean = true,
    val screenWake: ScreenWakeMode = ScreenWakeMode.ALWAYS_ON,
    val alertMode: AlertType = AlertType.SOUND_AND_VIBRATION,
    val scanIntervalMs: Long = 1000,
    val uiBrightness: Float = 0.5f,
    val keepScreenOff: Boolean = false,
    val selfMotionCompensation: Boolean = false,
    val requiresCharging: Boolean = false,
    val enabledSensors: Set<DataSource> = DataSource.entries.toSet(),
    val pulseIntervalSeconds: Int = 10
)

/**
 * Predefined emergency profiles
 */
object EmergencyProfiles {
    
    /**
     * Silent Sentry - Maximum stealth
     * No sounds, no visible animations, haptic only
     * Best for: Covert monitoring, sleeping situations
     */
    val SILENT_SENTRY = EmergencyConfig(
        name = "Silent Sentry",
        description = "Maximum stealth - no sounds, haptic only",
        sonarEnabled = false,  // No sound emission
        cameraEnabled = true,
        screenWake = ScreenWakeMode.ON_ALERT_ONLY,
        alertMode = AlertType.VIBRATION_ONLY,
        scanIntervalMs = 2000,
        uiBrightness = 0.0f,
        keepScreenOff = true,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.CAMERA)
    )
    
    /**
     * Guardian - Full protection when charging
     * All sensors at high frequency, plug-in recommended
     * Best for: Fixed position monitoring with power available
     */
    val GUARDIAN = EmergencyConfig(
        name = "Guardian",
        description = "Full protection - all sensors, requires charging",
        sonarEnabled = true,
        cameraEnabled = true,
        screenWake = ScreenWakeMode.ALWAYS_ON,
        alertMode = AlertType.SOUND_AND_VIBRATION,
        scanIntervalMs = 200,
        uiBrightness = 0.3f,
        keepScreenOff = false,
        requiresCharging = true,
        enabledSensors = DataSource.entries.toSet()
    )
    
    /**
     * Recon - Walking mode
     * Compensates for user movement, reduced false positives
     * Best for: Patrol and search operations
     */
    val RECON = EmergencyConfig(
        name = "Recon",
        description = "Walking mode - compensates for user movement",
        sonarEnabled = true,
        cameraEnabled = true,
        screenWake = ScreenWakeMode.PULSE,
        alertMode = AlertType.VIBRATION_ONLY,
        scanIntervalMs = 500,
        uiBrightness = 0.2f,
        keepScreenOff = false,
        selfMotionCompensation = true,
        pulseIntervalSeconds = 5,
        enabledSensors = DataSource.entries.toSet()
    )
    
    /**
     * Blackout - Minimum power consumption
     * Only passive radio scanning, maximum battery life
     * Best for: Extended operation without charging, 24+ hours
     */
    val BLACKOUT = EmergencyConfig(
        name = "Blackout",
        description = "Minimum power - 24+ hour operation",
        sonarEnabled = false,
        cameraEnabled = false,
        screenWake = ScreenWakeMode.ON_ALERT_ONLY,
        alertMode = AlertType.VIBRATION_ONLY,
        scanIntervalMs = 5000,
        uiBrightness = 0.0f,
        keepScreenOff = true,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH)
    )
    
    /**
     * Camp Watch - Overnight monitoring
     * Balanced settings for overnight camp security
     * Best for: Sleeping area protection
     */
    val CAMP_WATCH = EmergencyConfig(
        name = "Camp Watch",
        description = "Overnight monitoring - balanced battery and coverage",
        sonarEnabled = true,
        cameraEnabled = false,  // Save battery, camera less useful at night
        screenWake = ScreenWakeMode.ON_ALERT_ONLY,
        alertMode = AlertType.FLASH_AND_VIBRATION,
        scanIntervalMs = 1000,
        uiBrightness = 0.0f,
        keepScreenOff = true,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.SONAR)
    )
    
    /**
     * Search Party - Aggressive scanning for missing persons
     * Maximum range and sensitivity
     * Best for: Active search operations
     */
    val SEARCH_PARTY = EmergencyConfig(
        name = "Search Party",
        description = "Maximum sensitivity for active search",
        sonarEnabled = true,
        cameraEnabled = true,
        screenWake = ScreenWakeMode.ALWAYS_ON,
        alertMode = AlertType.SOUND_AND_VIBRATION,
        scanIntervalMs = 100,
        uiBrightness = 0.8f,
        keepScreenOff = false,
        selfMotionCompensation = true,
        enabledSensors = DataSource.entries.toSet()
    )
    
    /**
     * Get all available emergency profiles
     */
    fun getAllProfiles(): List<EmergencyConfig> = listOf(
        SILENT_SENTRY,
        GUARDIAN,
        RECON,
        BLACKOUT,
        CAMP_WATCH,
        SEARCH_PARTY
    )
    
    /**
     * Get profile by name
     */
    fun getProfile(name: String): EmergencyConfig? {
        return getAllProfiles().find { it.name == name }
    }
    
    /**
     * Get recommended profile based on conditions
     */
    fun getRecommendedProfile(
        isCharging: Boolean,
        batteryLevel: Int,
        isMoving: Boolean,
        isNighttime: Boolean
    ): EmergencyConfig {
        return when {
            isCharging -> GUARDIAN
            isMoving -> RECON
            isNighttime && batteryLevel > 30 -> CAMP_WATCH
            batteryLevel < 20 -> BLACKOUT
            batteryLevel < 40 -> SILENT_SENTRY
            else -> SILENT_SENTRY
        }
    }
    
    /**
     * Estimate battery life for each profile
     */
    fun estimateBatteryLife(profile: EmergencyConfig, batteryLevel: Int): Int {
        val drainRatePerHour = when (profile.name) {
            BLACKOUT.name -> 2
            SILENT_SENTRY.name -> 5
            CAMP_WATCH.name -> 7
            RECON.name -> 12
            GUARDIAN.name -> 20
            SEARCH_PARTY.name -> 25
            else -> 10  // Default for custom profiles
        }
        
        return (batteryLevel / drainRatePerHour.toFloat()).toInt()
    }
}

/**
 * Mode manager for switching between profiles
 */
class ModeManager {
    private var currentMode: OperatingMode = OperatingMode.NORMAL
    private var currentEmergencyProfile: EmergencyConfig? = null
    
    /**
     * Set operating mode
     */
    fun setMode(mode: OperatingMode, emergencyProfile: EmergencyConfig? = null) {
        currentMode = mode
        currentEmergencyProfile = if (mode == OperatingMode.EMERGENCY) {
            emergencyProfile ?: EmergencyProfiles.SILENT_SENTRY
        } else null
    }
    
    /**
     * Get current mode
     */
    fun getCurrentMode(): OperatingMode = currentMode
    
    /**
     * Get current emergency profile (if in emergency mode)
     */
    fun getCurrentEmergencyProfile(): EmergencyConfig? = currentEmergencyProfile
    
    /**
     * Get effective scan interval
     */
    fun getScanInterval(): Long {
        return currentEmergencyProfile?.scanIntervalMs 
            ?: ModeProfiles.getProfile(currentMode).scanIntervalMs
    }
    
    /**
     * Get enabled sensors for current mode
     */
    fun getEnabledSensors(): Set<DataSource> {
        return currentEmergencyProfile?.enabledSensors
            ?: ModeProfiles.getProfile(currentMode).enabledSensors
    }
    
    /**
     * Check if sonar is enabled
     */
    fun isSonarEnabled(): Boolean {
        return currentEmergencyProfile?.sonarEnabled
            ?: ModeProfiles.getProfile(currentMode).sonarEnabled
    }
    
    /**
     * Get UI brightness
     */
    fun getUiBrightness(): Float {
        return currentEmergencyProfile?.uiBrightness
            ?: ModeProfiles.getProfile(currentMode).uiBrightness
    }
}
