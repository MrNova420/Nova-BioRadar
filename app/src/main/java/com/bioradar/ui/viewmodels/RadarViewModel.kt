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
import kotlin.random.Random

/**
 * Radar state containing UI and sensor information
 */
data class RadarState(
    val batteryLevel: Int = 85,
    val maxRange: Float = 10f,
    val activeSensors: Set<DataSource> = setOf(
        DataSource.WIFI,
        DataSource.BLUETOOTH,
        DataSource.SONAR,
        DataSource.CAMERA
    ),
    val isCalibrating: Boolean = false,
    val calibrationProgress: Float = 0f
)

/**
 * ViewModel for the main Radar screen
 */
@HiltViewModel
class RadarViewModel @Inject constructor() : ViewModel() {
    
    private val _radarState = MutableStateFlow(RadarState())
    val radarState: StateFlow<RadarState> = _radarState.asStateFlow()
    
    private val _targets = MutableStateFlow<List<RadarTarget>>(emptyList())
    val targets: StateFlow<List<RadarTarget>> = _targets.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _currentMode = MutableStateFlow(OperatingMode.NORMAL)
    val currentMode: StateFlow<OperatingMode> = _currentMode.asStateFlow()
    
    /**
     * Start radar scanning
     */
    fun startScanning() {
        _isScanning.value = true
        viewModelScope.launch {
            while (_isScanning.value) {
                updateTargets()
                val profile = ModeProfiles.getProfile(_currentMode.value)
                delay(profile.scanIntervalMs)
            }
        }
    }
    
    /**
     * Stop radar scanning
     */
    fun stopScanning() {
        _isScanning.value = false
        _targets.value = emptyList()
    }
    
    /**
     * Set operating mode
     */
    fun setMode(mode: OperatingMode) {
        _currentMode.value = mode
        val profile = ModeProfiles.getProfile(mode)
        _radarState.value = _radarState.value.copy(
            activeSensors = profile.enabledSensors
        )
    }
    
    /**
     * Calibrate the radar baseline
     */
    fun calibrate() {
        viewModelScope.launch {
            _radarState.value = _radarState.value.copy(
                isCalibrating = true,
                calibrationProgress = 0f
            )
            
            for (i in 1..100) {
                delay(30)
                _radarState.value = _radarState.value.copy(
                    calibrationProgress = i / 100f
                )
            }
            
            _radarState.value = _radarState.value.copy(
                isCalibrating = false,
                calibrationProgress = 1f
            )
        }
    }
    
    /**
     * Update targets with simulated data
     * In production, this would come from the FusionEngine
     */
    private fun updateTargets() {
        // Simulate target detection
        // In production, this would receive data from the sensor fusion engine
        val currentTargets = _targets.value.toMutableList()
        
        // Update existing targets
        currentTargets.forEach { target ->
            // Add some movement to existing targets
        }
        
        // Randomly add/remove targets for demo
        if (Random.nextFloat() < 0.1f && currentTargets.size < 5) {
            currentTargets.add(
                RadarTarget(
                    angleDegrees = Random.nextFloat() * 360f,
                    distanceMeters = Random.nextFloat() * _radarState.value.maxRange,
                    confidence = Random.nextFloat() * 0.5f + 0.3f,
                    type = if (Random.nextFloat() > 0.3f) TargetType.HUMAN else TargetType.POSSIBLE_LIFE,
                    isMoving = Random.nextBoolean(),
                    dataSources = _radarState.value.activeSensors
                )
            )
        }
        
        if (Random.nextFloat() < 0.05f && currentTargets.isNotEmpty()) {
            currentTargets.removeAt(Random.nextInt(currentTargets.size))
        }
        
        // Update confidence of existing targets
        val updatedTargets = currentTargets.map { target ->
            target.copy(
                angleDegrees = target.angleDegrees + (Random.nextFloat() - 0.5f) * 2f,
                distanceMeters = target.distanceMeters?.let { 
                    (it + (Random.nextFloat() - 0.5f) * 0.2f).coerceIn(0.5f, _radarState.value.maxRange)
                },
                confidence = (target.confidence + (Random.nextFloat() - 0.5f) * 0.1f).coerceIn(0.1f, 1f),
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        _targets.value = updatedTargets
    }
    
    /**
     * Set maximum detection range
     */
    fun setMaxRange(range: Float) {
        _radarState.value = _radarState.value.copy(maxRange = range)
    }
    
    /**
     * Update battery level
     */
    fun updateBatteryLevel(level: Int) {
        _radarState.value = _radarState.value.copy(batteryLevel = level)
    }
}
