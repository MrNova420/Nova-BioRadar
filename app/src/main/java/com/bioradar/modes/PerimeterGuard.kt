package com.bioradar.modes

import com.bioradar.core.managers.AlertManager
import com.bioradar.core.models.*
import com.bioradar.data.repository.DetectionLogRepository
import com.bioradar.sensor.fusion.FusionEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Perimeter Guard System
 * Monitors a defined area and alerts on detected presence
 * Works completely offline
 */
@Singleton
class PerimeterGuard @Inject constructor(
    private val fusionEngine: FusionEngine,
    private val alertManager: AlertManager,
    private val logRepository: DetectionLogRepository
) {
    private var baseline: SensorBaseline? = null
    private var zone: PerimeterZone? = null
    private var guardJob: Job? = null
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()
    
    private val _currentStatus = MutableStateFlow(ZoneStatus.UNKNOWN)
    val currentStatus: StateFlow<ZoneStatus> = _currentStatus.asStateFlow()
    
    private val _deviation = MutableStateFlow(0f)
    val deviation: StateFlow<Float> = _deviation.asStateFlow()
    
    // Store recent readings for analysis
    private val readingHistory = mutableListOf<SensorReadingSnapshot>()
    
    /**
     * Calibrate baseline - records "empty" environment
     * Should be run with no one in monitored area
     */
    suspend fun calibrate(durationMs: Long = 30000): SensorBaseline {
        _isCalibrating.value = true
        readingHistory.clear()
        
        val startTime = System.currentTimeMillis()
        var rssiVariances = mutableListOf<Float>()
        var sonarEnergies = mutableListOf<Float>()
        var cameraMotions = mutableListOf<Float>()
        var noiseFloors = mutableListOf<Float>()
        
        try {
            while (System.currentTimeMillis() - startTime < durationMs) {
                // Collect current fusion state
                val targets = fusionEngine.targets.value
                
                // Sample from fusion engine (simulated for now)
                rssiVariances.add(calculateCurrentRssiVariance())
                sonarEnergies.add(calculateCurrentSonarEnergy())
                cameraMotions.add(calculateCurrentCameraMotion())
                noiseFloors.add(calculateCurrentNoiseFloor())
                
                delay(100)
            }
            
            baseline = SensorBaseline(
                avgRssiVariance = rssiVariances.average().toFloat(),
                avgSonarEnergy = sonarEnergies.average().toFloat(),
                avgCameraMotion = cameraMotions.average().toFloat(),
                ambientNoiseLevel = noiseFloors.average().toFloat(),
                calibrationTime = System.currentTimeMillis(),
                sampleCount = rssiVariances.size,
                environmentType = detectEnvironmentType()
            )
            
            return baseline!!
        } finally {
            _isCalibrating.value = false
        }
    }
    
    /**
     * Configure the monitoring zone
     */
    fun configureZone(config: PerimeterZone) {
        this.zone = config.copy(
            baselineCalibrated = baseline != null,
            baselineData = baseline
        )
    }
    
    /**
     * Start guarding the perimeter
     */
    fun startGuarding(scope: CoroutineScope) {
        if (baseline == null) {
            throw IllegalStateException("Must calibrate before guarding")
        }
        if (zone == null) {
            throw IllegalStateException("Must configure zone before guarding")
        }
        
        _isActive.value = true
        _currentStatus.value = ZoneStatus.GREEN_CLEAR
        
        guardJob = scope.launch(Dispatchers.Default) {
            while (_isActive.value) {
                try {
                    val deviation = calculateDeviation()
                    _deviation.value = deviation
                    
                    val status = evaluateStatus(deviation)
                    _currentStatus.value = status
                    
                    if (status != ZoneStatus.GREEN_CLEAR) {
                        handleAlert(status, deviation)
                    }
                    
                    delay(getScanInterval())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Log error but continue guarding
                    delay(1000)
                }
            }
        }
    }
    
    /**
     * Stop guarding
     */
    fun stopGuarding() {
        _isActive.value = false
        guardJob?.cancel()
        guardJob = null
        _currentStatus.value = ZoneStatus.UNKNOWN
        alertManager.stopAlert()
    }
    
    /**
     * Calculate deviation from baseline
     */
    private fun calculateDeviation(): Float {
        val currentBaseline = baseline ?: return 0f
        
        // Get current sensor values
        val currentRssiVariance = calculateCurrentRssiVariance()
        val currentSonarEnergy = calculateCurrentSonarEnergy()
        val currentCameraMotion = calculateCurrentCameraMotion()
        val currentBluetoothVariance = calculateCurrentBluetoothVariance()
        
        // Calculate deviation for each sensor
        val wifiDeviation = if (currentBaseline.avgRssiVariance > 0) {
            ((currentRssiVariance - currentBaseline.avgRssiVariance) / currentBaseline.avgRssiVariance) * 100
        } else 0f
        
        val bluetoothDeviation = if (currentBaseline.avgRssiVariance > 0) {
            ((currentBluetoothVariance - currentBaseline.avgRssiVariance) / currentBaseline.avgRssiVariance) * 100
        } else 0f
        
        val sonarDeviation = if (currentBaseline.avgSonarEnergy > 0) {
            ((currentSonarEnergy - currentBaseline.avgSonarEnergy) / currentBaseline.avgSonarEnergy) * 100
        } else 0f
        
        val cameraDeviation = if (currentBaseline.avgCameraMotion > 0) {
            ((currentCameraMotion - currentBaseline.avgCameraMotion) / currentBaseline.avgCameraMotion) * 100
        } else 0f
        
        // Weighted average based on zone's active sensors
        val weights = zone?.activeSensors?.let { sensors ->
            mapOf(
                DataSource.WIFI to (if (DataSource.WIFI in sensors) 1f else 0f),
                DataSource.BLUETOOTH to (if (DataSource.BLUETOOTH in sensors) 1f else 0f),
                DataSource.SONAR to (if (DataSource.SONAR in sensors) 1.5f else 0f),
                DataSource.CAMERA to (if (DataSource.CAMERA in sensors) 1.5f else 0f)
            )
        } ?: mapOf(
            DataSource.WIFI to 1f,
            DataSource.BLUETOOTH to 1f,
            DataSource.SONAR to 1.5f,
            DataSource.CAMERA to 1.5f
        )
        
        val totalWeight = weights.values.sum()
        
        return if (totalWeight > 0) {
            (wifiDeviation * (weights[DataSource.WIFI] ?: 0f) +
             bluetoothDeviation * (weights[DataSource.BLUETOOTH] ?: 0f) +
             sonarDeviation * (weights[DataSource.SONAR] ?: 0f) +
             cameraDeviation * (weights[DataSource.CAMERA] ?: 0f)) / totalWeight
        } else 0f
    }
    
    /**
     * Evaluate zone status based on deviation
     */
    private fun evaluateStatus(deviation: Float): ZoneStatus {
        val thresholds = zone?.sensitivity?.getThresholds() 
            ?: AlertThresholds(yellow = 25f, red = 50f)
        
        return when {
            deviation > thresholds.red -> ZoneStatus.RED_PRESENCE
            deviation > thresholds.yellow -> ZoneStatus.YELLOW_POSSIBLE
            deviation < 0 -> ZoneStatus.GREEN_CLEAR  // Below baseline = clear
            else -> ZoneStatus.GREEN_CLEAR
        }
    }
    
    /**
     * Handle alert based on status
     */
    private suspend fun handleAlert(status: ZoneStatus, deviation: Float) {
        val currentZone = zone ?: return
        
        // Log the detection event
        val event = DetectionEvent(
            targets = fusionEngine.targets.value,
            mode = OperatingMode.GUARD,
            deviceStable = true,
            batteryLevel = 100, // TODO: Get actual battery level
            activeSensors = currentZone.activeSensors,
            locationLabel = currentZone.name
        )
        
        logRepository.logEvent(event)
        
        // Trigger alert
        alertManager.triggerAlert(status, currentZone.alertType)
    }
    
    /**
     * Get scan interval based on zone sensitivity
     */
    private fun getScanInterval(): Long {
        return when (zone?.sensitivity) {
            Sensitivity.HIGH -> 200L
            Sensitivity.MEDIUM -> 500L
            Sensitivity.LOW -> 1000L
            Sensitivity.CUSTOM -> 500L
            null -> 500L
        }
    }
    
    // Simulated sensor value calculations
    // In production, these would get actual values from fusion engine
    
    private fun calculateCurrentRssiVariance(): Float {
        val targets = fusionEngine.targets.value
        val wifiTargets = targets.filter { DataSource.WIFI in it.dataSources }
        return if (wifiTargets.isEmpty()) {
            baseline?.avgRssiVariance ?: 2.0f
        } else {
            val avgConfidence = wifiTargets.map { it.confidence }.average().toFloat()
            (baseline?.avgRssiVariance ?: 2.0f) * (1 + avgConfidence * 5)
        }
    }
    
    private fun calculateCurrentBluetoothVariance(): Float {
        val targets = fusionEngine.targets.value
        val btTargets = targets.filter { DataSource.BLUETOOTH in it.dataSources }
        return if (btTargets.isEmpty()) {
            baseline?.avgRssiVariance ?: 2.0f
        } else {
            val avgConfidence = btTargets.map { it.confidence }.average().toFloat()
            (baseline?.avgRssiVariance ?: 2.0f) * (1 + avgConfidence * 4)
        }
    }
    
    private fun calculateCurrentSonarEnergy(): Float {
        val targets = fusionEngine.targets.value
        val sonarTargets = targets.filter { DataSource.SONAR in it.dataSources }
        return if (sonarTargets.isEmpty()) {
            baseline?.avgSonarEnergy ?: 0.1f
        } else {
            val avgConfidence = sonarTargets.map { it.confidence }.average().toFloat()
            (baseline?.avgSonarEnergy ?: 0.1f) * (1 + avgConfidence * 10)
        }
    }
    
    private fun calculateCurrentCameraMotion(): Float {
        val targets = fusionEngine.targets.value
        val movingTargets = targets.filter { it.isMoving }
        return if (movingTargets.isEmpty()) {
            baseline?.avgCameraMotion ?: 0.05f
        } else {
            val motionScore = movingTargets.size * 0.2f
            (baseline?.avgCameraMotion ?: 0.05f) * (1 + motionScore * 5)
        }
    }
    
    private fun calculateCurrentNoiseFloor(): Float {
        return baseline?.ambientNoiseLevel ?: 0.2f
    }
    
    private fun detectEnvironmentType(): String {
        // Would analyze signal characteristics to detect indoor/outdoor
        return "indoor"
    }
    
    /**
     * Get current guard status summary
     */
    fun getGuardStatus(): GuardStatus {
        return GuardStatus(
            isActive = _isActive.value,
            isCalibrating = _isCalibrating.value,
            currentStatus = _currentStatus.value,
            deviation = _deviation.value,
            zone = zone,
            baseline = baseline
        )
    }
}

/**
 * Alert thresholds for zone evaluation
 */
data class AlertThresholds(
    val yellow: Float,
    val red: Float
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

/**
 * Snapshot of sensor reading for history
 */
data class SensorReadingSnapshot(
    val timestamp: Long,
    val rssiVariance: Float,
    val sonarEnergy: Float,
    val cameraMotion: Float,
    val targetCount: Int
)

/**
 * Guard status summary
 */
data class GuardStatus(
    val isActive: Boolean,
    val isCalibrating: Boolean,
    val currentStatus: ZoneStatus,
    val deviation: Float,
    val zone: PerimeterZone?,
    val baseline: SensorBaseline?
)
