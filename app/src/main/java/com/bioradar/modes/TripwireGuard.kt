package com.bioradar.modes

import com.bioradar.core.models.*
import com.bioradar.modes.PerimeterGuard
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tripwire Mode - Simplified perimeter guard for specific entry points
 * Provides quick setup templates for common scenarios
 */
@Singleton
class TripwireGuard @Inject constructor(
    private val perimeterGuard: PerimeterGuard
) {
    /**
     * Quick setup for doorway monitoring
     * High sensitivity, narrow cone, vibration only
     */
    fun setupDoorway(name: String = "Doorway") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FORWARD_CONE,
            sensitivity = Sensitivity.HIGH,
            alertType = AlertType.VIBRATION_ONLY,
            activeSensors = setOf(DataSource.SONAR, DataSource.CAMERA)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for hallway monitoring
     * Medium sensitivity, wide front coverage
     */
    fun setupHallway(name: String = "Hallway") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FRONT_WIDE,
            sensitivity = Sensitivity.MEDIUM,
            alertType = AlertType.SOUND_AND_VIBRATION,
            activeSensors = setOf(DataSource.SONAR, DataSource.CAMERA, DataSource.WIFI)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for room monitoring
     * Low sensitivity, 360° coverage to reduce false positives
     */
    fun setupRoom(name: String = "Room") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FULL_360,
            sensitivity = Sensitivity.LOW,
            alertType = AlertType.SOUND_AND_VIBRATION,
            activeSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.SONAR)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for stairway monitoring
     * Medium sensitivity, forward cone
     */
    fun setupStairway(name: String = "Stairway") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FORWARD_CONE,
            sensitivity = Sensitivity.MEDIUM,
            alertType = AlertType.VIBRATION_ONLY,
            activeSensors = setOf(DataSource.SONAR, DataSource.CAMERA)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for outdoor perimeter
     * High sensitivity with radio-based detection
     */
    fun setupOutdoorPerimeter(name: String = "Perimeter") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FULL_360,
            sensitivity = Sensitivity.HIGH,
            alertType = AlertType.FLASH_AND_VIBRATION,
            activeSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.CAMERA)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for stealth monitoring
     * Silent operation, no sound emissions
     */
    fun setupStealthWatch(name: String = "Stealth") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FRONT_WIDE,
            sensitivity = Sensitivity.HIGH,
            alertType = AlertType.VIBRATION_ONLY,
            // No sonar to avoid sound emissions
            activeSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.CAMERA)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for vehicle monitoring
     * Lower sensitivity for larger movements
     */
    fun setupVehicleWatch(name: String = "Vehicle Lane") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FRONT_WIDE,
            sensitivity = Sensitivity.LOW,
            alertType = AlertType.SOUND_AND_VIBRATION,
            activeSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH)
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Get all available preset templates
     */
    fun getPresetTemplates(): List<TripwirePreset> = listOf(
        TripwirePreset(
            id = "doorway",
            name = "Doorway",
            description = "High sensitivity, narrow cone for entry monitoring",
            icon = "door"
        ),
        TripwirePreset(
            id = "hallway",
            name = "Hallway",
            description = "Medium sensitivity, wide front coverage",
            icon = "hallway"
        ),
        TripwirePreset(
            id = "room",
            name = "Room",
            description = "360° coverage with low sensitivity",
            icon = "room"
        ),
        TripwirePreset(
            id = "stairway",
            name = "Stairway",
            description = "Forward cone monitoring for stairs",
            icon = "stairs"
        ),
        TripwirePreset(
            id = "outdoor",
            name = "Outdoor Perimeter",
            description = "Full 360° with high sensitivity",
            icon = "fence"
        ),
        TripwirePreset(
            id = "stealth",
            name = "Stealth Watch",
            description = "Silent operation, no sound emissions",
            icon = "visibility_off"
        ),
        TripwirePreset(
            id = "vehicle",
            name = "Vehicle Lane",
            description = "Low sensitivity for larger movements",
            icon = "car"
        )
    )
    
    /**
     * Apply preset by ID
     */
    fun applyPreset(presetId: String, customName: String? = null) {
        val name = customName ?: getPresetTemplates().find { it.id == presetId }?.name ?: presetId
        
        when (presetId) {
            "doorway" -> setupDoorway(name)
            "hallway" -> setupHallway(name)
            "room" -> setupRoom(name)
            "stairway" -> setupStairway(name)
            "outdoor" -> setupOutdoorPerimeter(name)
            "stealth" -> setupStealthWatch(name)
            "vehicle" -> setupVehicleWatch(name)
        }
    }
}

/**
 * Tripwire preset template
 */
data class TripwirePreset(
    val id: String,
    val name: String,
    val description: String,
    val icon: String
)
