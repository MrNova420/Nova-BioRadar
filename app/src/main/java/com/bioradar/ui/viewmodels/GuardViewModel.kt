package com.bioradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioradar.core.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Guard/Perimeter screen
 */
@HiltViewModel
class GuardViewModel @Inject constructor() : ViewModel() {
    
    private val _zones = MutableStateFlow<List<PerimeterZone>>(emptyList())
    val zones: StateFlow<List<PerimeterZone>> = _zones.asStateFlow()
    
    private val _zoneStatuses = MutableStateFlow<Map<String, ZoneStatus>>(emptyMap())
    
    private val _isGuarding = MutableStateFlow(false)
    val isGuarding: StateFlow<Boolean> = _isGuarding.asStateFlow()
    
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()
    
    /**
     * Add a new monitoring zone
     */
    fun addZone(
        name: String,
        sector: Sector,
        sensitivity: Sensitivity,
        alertType: AlertType
    ) {
        val newZone = PerimeterZone(
            name = name,
            monitoringSector = sector,
            sensitivity = sensitivity,
            alertType = alertType
        )
        _zones.value = _zones.value + newZone
        _zoneStatuses.value = _zoneStatuses.value + (newZone.id to ZoneStatus.UNKNOWN)
    }
    
    /**
     * Delete a zone
     */
    fun deleteZone(zoneId: String) {
        _zones.value = _zones.value.filter { it.id != zoneId }
        _zoneStatuses.value = _zoneStatuses.value - zoneId
    }
    
    /**
     * Calibrate a specific zone
     */
    fun calibrateZone(zoneId: String) {
        viewModelScope.launch {
            _isCalibrating.value = true
            
            // Simulate calibration
            delay(3000)
            
            val zone = _zones.value.find { it.id == zoneId }
            if (zone != null) {
                val calibratedZone = zone.copy(
                    baselineCalibrated = true,
                    baselineData = SensorBaseline(
                        avgRssiVariance = 2.5f,
                        avgSonarEnergy = 0.1f,
                        avgCameraMotion = 0.05f,
                        ambientNoiseLevel = 0.2f,
                        calibrationTime = System.currentTimeMillis(),
                        sampleCount = 300
                    )
                )
                _zones.value = _zones.value.map { 
                    if (it.id == zoneId) calibratedZone else it 
                }
                _zoneStatuses.value = _zoneStatuses.value + (zoneId to ZoneStatus.GREEN_CLEAR)
            }
            
            _isCalibrating.value = false
        }
    }
    
    /**
     * Start guarding all zones
     */
    fun startGuarding() {
        _isGuarding.value = true
        viewModelScope.launch {
            while (_isGuarding.value) {
                updateZoneStatuses()
                delay(1000)
            }
        }
    }
    
    /**
     * Stop guarding
     */
    fun stopGuarding() {
        _isGuarding.value = false
    }
    
    /**
     * Get status for a zone
     */
    fun getZoneStatus(zoneId: String): ZoneStatus {
        return _zoneStatuses.value[zoneId] ?: ZoneStatus.UNKNOWN
    }
    
    /**
     * Update zone statuses (simulated)
     */
    private fun updateZoneStatuses() {
        val updatedStatuses = _zones.value.associate { zone ->
            zone.id to when {
                !zone.baselineCalibrated -> ZoneStatus.UNKNOWN
                Math.random() < 0.05 -> ZoneStatus.RED_PRESENCE
                Math.random() < 0.1 -> ZoneStatus.YELLOW_POSSIBLE
                else -> ZoneStatus.GREEN_CLEAR
            }
        }
        _zoneStatuses.value = updatedStatuses
    }
}
