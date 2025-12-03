package com.bioradar.core.managers

import com.bioradar.core.models.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Location naming system for mesh networking
 * Provides GPS-independent location labeling for nodes
 */
@Singleton
class LocationManager @Inject constructor() {
    
    private val namedLocations = mutableMapOf<String, NamedLocation>()
    
    /**
     * Named location for mesh node assignment
     */
    data class NamedLocation(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val description: String? = null,
        val assignedNodeId: String? = null,
        val createdAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Location with current status
     */
    data class LocationStatus(
        val location: NamedLocation,
        val status: ZoneStatus,
        val lastUpdate: Long?,
        val isOnline: Boolean,
        val confidence: Float = 0f
    )
    
    /**
     * Add a new named location
     */
    fun addLocation(name: String, description: String? = null): NamedLocation {
        val location = NamedLocation(name = name, description = description)
        namedLocations[location.id] = location
        return location
    }
    
    /**
     * Remove a location
     */
    fun removeLocation(locationId: String) {
        namedLocations.remove(locationId)
    }
    
    /**
     * Update location name or description
     */
    fun updateLocation(locationId: String, name: String? = null, description: String? = null) {
        namedLocations[locationId]?.let { current ->
            namedLocations[locationId] = current.copy(
                name = name ?: current.name,
                description = description ?: current.description
            )
        }
    }
    
    /**
     * Assign a mesh node to a location
     */
    fun assignNodeToLocation(locationId: String, nodeId: String) {
        namedLocations[locationId]?.let {
            namedLocations[locationId] = it.copy(assignedNodeId = nodeId)
        }
    }
    
    /**
     * Unassign node from location
     */
    fun unassignNode(locationId: String) {
        namedLocations[locationId]?.let {
            namedLocations[locationId] = it.copy(assignedNodeId = null)
        }
    }
    
    /**
     * Get location by ID
     */
    fun getLocation(locationId: String): NamedLocation? {
        return namedLocations[locationId]
    }
    
    /**
     * Get all locations
     */
    fun getAllLocations(): List<NamedLocation> {
        return namedLocations.values.toList()
    }
    
    /**
     * Get locations that don't have assigned nodes
     */
    fun getUnassignedLocations(): List<NamedLocation> {
        return namedLocations.values.filter { it.assignedNodeId == null }
    }
    
    /**
     * Get all locations with their current status from mesh nodes
     */
    fun getLocationStatuses(nodes: List<MeshNode>): List<LocationStatus> {
        return namedLocations.values.map { location ->
            val node = nodes.find { it.nodeId == location.assignedNodeId }
            LocationStatus(
                location = location,
                status = node?.lastStatus ?: ZoneStatus.UNKNOWN,
                lastUpdate = node?.lastUpdate,
                isOnline = node?.isOnline ?: false,
                confidence = node?.lastConfidence ?: 0f
            )
        }
    }
    
    /**
     * Get location by assigned node ID
     */
    fun getLocationByNode(nodeId: String): NamedLocation? {
        return namedLocations.values.find { it.assignedNodeId == nodeId }
    }
    
    /**
     * Get preset location templates for quick setup
     */
    fun getPresetLocations(): List<String> = listOf(
        // Entrances
        "NORTH GATE",
        "SOUTH GATE",
        "EAST ENTRANCE",
        "WEST ENTRANCE",
        "MAIN ENTRANCE",
        "BACK DOOR",
        "SIDE DOOR",
        "GARAGE",
        
        // Building levels
        "STAIRS 1F",
        "STAIRS 2F",
        "STAIRS 3F",
        "BASEMENT",
        "ROOF ACCESS",
        "ELEVATOR",
        
        // Common areas
        "MAIN HALLWAY",
        "LOBBY",
        "RECEPTION",
        "BREAK ROOM",
        "CONFERENCE ROOM",
        
        // Perimeter
        "PERIMETER NORTH",
        "PERIMETER SOUTH",
        "PERIMETER EAST",
        "PERIMETER WEST",
        "FENCE LINE",
        "PARKING LOT",
        
        // Guard positions
        "GUARD POST 1",
        "GUARD POST 2",
        "GUARD POST 3",
        "COMMAND CENTER",
        "WATCHTOWER",
        
        // Other
        "CHECKPOINT A",
        "CHECKPOINT B",
        "SAFE ROOM",
        "STORAGE",
        "UTILITY ROOM"
    )
    
    /**
     * Quickly add preset locations
     */
    fun addPresetLocations(presetNames: List<String>) {
        presetNames.forEach { name ->
            addLocation(name)
        }
    }
    
    /**
     * Clear all locations
     */
    fun clearAll() {
        namedLocations.clear()
    }
    
    /**
     * Export locations as JSON-compatible list
     */
    fun exportLocations(): List<Map<String, Any?>> {
        return namedLocations.values.map { location ->
            mapOf(
                "id" to location.id,
                "name" to location.name,
                "description" to location.description,
                "assignedNodeId" to location.assignedNodeId,
                "createdAt" to location.createdAt
            )
        }
    }
}
