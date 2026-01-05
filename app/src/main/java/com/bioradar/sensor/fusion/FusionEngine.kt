package com.bioradar.sensor.fusion

import com.bioradar.core.models.*
import com.bioradar.sensor.drivers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Sensor Fusion Engine
 * Combines data from all sensors to produce unified presence detection
 */
@Singleton
class FusionEngine @Inject constructor(
    private val bluetoothScanner: BluetoothScanner,
    private val wifiScanner: WifiScanner,
    private val audioSonarDriver: AudioSonarDriver,
    private val cameraMotionDriver: CameraMotionDriver,
    private val context: android.content.Context
) {
    // Optional sensors - initialized on-demand
    private var uwbRadarDriver: UwbRadarDriver? = null
    private var selfGeneratedWiFiSystem: SelfGeneratedWiFiSystem? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private val _targets = MutableStateFlow<List<RadarTarget>>(emptyList())
    val targets: StateFlow<List<RadarTarget>> = _targets.asStateFlow()
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private var sensorWeights = SensorWeights()
    private val targetTracker = TargetTracker()
    
    /**
     * Sensor weight configuration
     */
    data class SensorWeights(
        val wifi: Float = 0.20f,
        val bluetooth: Float = 0.20f,
        val sonar: Float = 0.25f,
        val camera: Float = 0.25f,
        val uwb: Float = 0.40f  // Applied if available
    )
    
    /**
     * Start the fusion engine
     */
    fun start(
        enabledSensors: Set<DataSource>,
        modeProfile: ModeProfile
    ) {
        if (_isActive.value) return
        _isActive.value = true
        
        // Initialize optional sensors
        if (DataSource.UWB in enabledSensors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            uwbRadarDriver = UwbRadarDriver(context)
        }
        if (DataSource.WIFI in enabledSensors) {
            selfGeneratedWiFiSystem = SelfGeneratedWiFiSystem(context)
        }
        
        scope.launch {
            // Collect data from all enabled sensors
            val flows = mutableListOf<Flow<SensorData>>()
            
            if (DataSource.BLUETOOTH in enabledSensors) {
                flows.add(collectBluetoothData())
            }
            if (DataSource.WIFI in enabledSensors) {
                flows.add(collectWifiData())
            }
            if (DataSource.SONAR in enabledSensors && modeProfile.sonarEnabled) {
                flows.add(collectSonarData(modeProfile.scanIntervalMs))
            }
            if (DataSource.UWB in enabledSensors) {
                flows.add(collectUwbData())
            }
            
            // Merge all sensor flows
            if (flows.isNotEmpty()) {
                merge(*flows.toTypedArray())
                    .collect { sensorData ->
                        processSensorData(sensorData)
                    }
            }
        }
    }
    
    /**
     * Stop the fusion engine
     */
    fun stop() {
        _isActive.value = false
        audioSonarDriver.stop()
        cameraMotionDriver.stop()
        uwbRadarDriver?.release()
        selfGeneratedWiFiSystem?.stopAll()
        uwbRadarDriver = null
        selfGeneratedWiFiSystem = null
        _targets.value = emptyList()
    }
    
    /**
     * Collect Bluetooth sensor data
     */
    private fun collectBluetoothData(): Flow<SensorData> = flow {
        if (!bluetoothScanner.isAvailable()) return@flow
        
        bluetoothScanner.startScanning()
            .collect { reading ->
                val variance = bluetoothScanner.calculateRssiVariance(reading.address)
                val distance = bluetoothScanner.estimateDistance(reading.rssi)
                val isMoving = bluetoothScanner.detectMotion(reading.address)
                
                emit(SensorData.Bluetooth(
                    deviceAddress = reading.address,
                    rssi = reading.rssi,
                    variance = variance,
                    estimatedDistance = distance,
                    isMoving = isMoving,
                    timestamp = reading.timestamp
                ))
            }
    }
    
    /**
     * Collect WiFi sensor data
     */
    private fun collectWifiData(): Flow<SensorData> = flow {
        if (!wifiScanner.isAvailable()) return@flow
        
        wifiScanner.startScanning()
            .collect { readings ->
                val presenceScore = wifiScanner.calculatePresenceScore()
                val throughWall = wifiScanner.detectThroughWallPresence()
                
                emit(SensorData.Wifi(
                    presenceScore = presenceScore,
                    throughWallDetection = throughWall,
                    apCount = readings.size,
                    timestamp = System.currentTimeMillis()
                ))
            }
    }
    
    /**
     * Collect Sonar sensor data
     */
    private fun collectSonarData(intervalMs: Long): Flow<SensorData> = flow {
        if (!audioSonarDriver.isAvailable()) return@flow
        
        audioSonarDriver.startSonar(intervalMs)
            .collect { result ->
                val motionScore = audioSonarDriver.calculateMotionScore()
                val nearestEcho = result.nearestEcho
                
                emit(SensorData.Sonar(
                    hasDetection = result.hasDetection,
                    nearestDistance = nearestEcho?.distance,
                    amplitude = nearestEcho?.amplitude ?: 0f,
                    dopplerShift = nearestEcho?.dopplerShift,
                    motionScore = motionScore,
                    quality = result.quality,
                    timestamp = System.currentTimeMillis()
                ))
            }
    }
    
    /**
     * Collect UWB sensor data
     */
    private fun collectUwbData(): Flow<SensorData> = flow {
        val uwbDriver = uwbRadarDriver ?: return@flow
        
        if (!uwbDriver.isAvailable()) return@flow
        
        // Initialize UWB
        if (!uwbDriver.initialize()) return@flow
        
        // Get local address to range with other devices
        // In production, this would come from mesh network or manual pairing
        // For now, emit placeholder data when UWB is ready
        
        // Note: Actual ranging requires a partner device with known UWB address
        // This is a placeholder - real implementation would integrate with mesh network
        
        // TODO: Integrate with mesh network to get partner UWB addresses
        // For now, emit test data to demonstrate UWB capability
        android.util.Log.i("FusionEngine", "UWB initialized and ready for ranging")
    }
    
    /**
     * Collect self-generated WiFi detection data
     */
    private fun collectSelfGeneratedWiFiData(): Flow<SensorData> = flow {
        val wifiSystem = selfGeneratedWiFiSystem ?: return@flow
        
        if (!wifiSystem.isSupported()) return@flow
        
        // Collect RF shadow detections
        wifiSystem.detectWithSelfGeneratedWiFi()
            .collect { shadow ->
                if (shadow.shadowDetected) {
                    // Convert RF shadow to WiFi sensor data
                    emit(SensorData.Wifi(
                        presenceScore = shadow.confidence,
                        throughWallDetection = ThroughWallDetection(
                            detected = true,
                            confidence = shadow.confidence,
                            estimatedDistance = shadow.estimatedDistance ?: 10f
                        ),
                        apCount = 1,  // Our own generated WiFi
                        timestamp = shadow.timestamp
                    ))
                }
            }
    }
    
    /**
     * Process incoming sensor data and update targets
     */
    private fun processSensorData(data: SensorData) {
        val detection = when (data) {
            is SensorData.Bluetooth -> processBluetoothData(data)
            is SensorData.Wifi -> processWifiData(data)
            is SensorData.Sonar -> processSonarData(data)
            is SensorData.Camera -> processCameraData(data)
            is SensorData.Uwb -> processUwbData(data)
        }
        
        detection?.let { targetTracker.update(it) }
        
        // Get fused targets
        _targets.value = targetTracker.getActiveTargets()
    }
    
    /**
     * Process Bluetooth data into detection
     */
    private fun processBluetoothData(data: SensorData.Bluetooth): Detection? {
        val confidence = calculateBluetoothConfidence(data)
        if (confidence < MIN_CONFIDENCE) return null
        
        // Bluetooth can't determine angle, use placeholder
        val angle = 0f
        
        return Detection(
            source = DataSource.BLUETOOTH,
            confidence = confidence * sensorWeights.bluetooth,
            distance = data.estimatedDistance,
            angle = angle,
            isMoving = data.isMoving,
            timestamp = data.timestamp
        )
    }
    
    /**
     * Process WiFi data into detection
     */
    private fun processWifiData(data: SensorData.Wifi): Detection? {
        val confidence = data.presenceScore
        if (confidence < MIN_CONFIDENCE) return null
        
        // WiFi through-wall detection
        val throughWall = data.throughWallDetection
        val adjustedConfidence = if (throughWall.detected) {
            confidence * 0.7f + throughWall.confidence * 0.3f
        } else {
            confidence
        }
        
        return Detection(
            source = DataSource.WIFI,
            confidence = adjustedConfidence * sensorWeights.wifi,
            distance = null, // WiFi typically can't determine precise distance
            angle = null,
            isMoving = throughWall.isMoving,
            timestamp = data.timestamp
        )
    }
    
    /**
     * Process Sonar data into detection
     */
    private fun processSonarData(data: SensorData.Sonar): Detection? {
        if (!data.hasDetection) return null
        
        val confidence = calculateSonarConfidence(data)
        if (confidence < MIN_CONFIDENCE) return null
        
        // Sonar is front-facing
        val angle = 0f // Forward direction
        
        return Detection(
            source = DataSource.SONAR,
            confidence = confidence * sensorWeights.sonar,
            distance = data.nearestDistance,
            angle = angle,
            isMoving = data.motionScore > 0.3f,
            timestamp = data.timestamp
        )
    }
    
    /**
     * Process Camera data into detection
     */
    private fun processCameraData(data: SensorData.Camera): Detection? {
        if (data.motionMagnitude < 0.1f) return null
        
        val confidence = data.motionMagnitude
        
        return Detection(
            source = DataSource.CAMERA,
            confidence = confidence * sensorWeights.camera,
            distance = null,
            angle = data.angleDegrees,
            isMoving = true,
            timestamp = data.timestamp
        )
    }
    
    /**
     * Process UWB data into detection
     */
    private fun processUwbData(data: SensorData.Uwb): Detection {
        return Detection(
            source = DataSource.UWB,
            confidence = data.confidence * sensorWeights.uwb,
            distance = data.distance,
            angle = data.angle,
            isMoving = null,
            timestamp = data.timestamp
        )
    }
    
    /**
     * Calculate confidence from Bluetooth data
     */
    private fun calculateBluetoothConfidence(data: SensorData.Bluetooth): Float {
        val varianceScore = (data.variance / 50f).coerceIn(0f, 1f)
        val distanceScore = (1f - data.estimatedDistance / 30f).coerceIn(0f, 1f)
        val rssiScore = ((data.rssi + 100) / 60f).coerceIn(0f, 1f)
        
        return varianceScore * 0.5f + distanceScore * 0.3f + rssiScore * 0.2f
    }
    
    /**
     * Calculate confidence from Sonar data
     */
    private fun calculateSonarConfidence(data: SensorData.Sonar): Float {
        val amplitudeScore = data.amplitude
        val qualityScore = data.quality
        val motionBonus = if (data.motionScore > 0.3f) 0.2f else 0f
        
        return (amplitudeScore * 0.5f + qualityScore * 0.3f + motionBonus).coerceIn(0f, 1f)
    }
    
    /**
     * Classify target type based on sensor data
     */
    fun classifyTarget(detections: List<Detection>): TargetType {
        if (detections.isEmpty()) return TargetType.UNKNOWN
        
        val avgConfidence = detections.map { it.confidence }.average().toFloat()
        val hasMotion = detections.any { it.isMoving == true }
        val sensorCount = detections.map { it.source }.distinct().size
        
        return when {
            avgConfidence > 0.75f && sensorCount >= 3 -> TargetType.HUMAN
            avgConfidence > 0.5f && hasMotion -> TargetType.POSSIBLE_LIFE
            avgConfidence > 0.3f -> TargetType.POSSIBLE_LIFE
            avgConfidence < 0.2f -> TargetType.NOISE
            else -> TargetType.UNKNOWN
        }
    }
    
    /**
     * Update sensor weights
     */
    fun setSensorWeights(weights: SensorWeights) {
        sensorWeights = weights
    }
    
    companion object {
        private const val MIN_CONFIDENCE = 0.15f
    }
}

/**
 * Unified sensor data wrapper
 */
sealed class SensorData {
    abstract val timestamp: Long
    
    data class Bluetooth(
        val deviceAddress: String,
        val rssi: Int,
        val variance: Float,
        val estimatedDistance: Float,
        val isMoving: Boolean,
        override val timestamp: Long
    ) : SensorData()
    
    data class Wifi(
        val presenceScore: Float,
        val throughWallDetection: ThroughWallDetection,
        val apCount: Int,
        override val timestamp: Long
    ) : SensorData()
    
    data class Sonar(
        val hasDetection: Boolean,
        val nearestDistance: Float?,
        val amplitude: Float,
        val dopplerShift: Float?,
        val motionScore: Float,
        val quality: Float,
        override val timestamp: Long
    ) : SensorData()
    
    data class Camera(
        val motionMagnitude: Float,
        val angleDegrees: Float,
        val sector: Int,
        override val timestamp: Long
    ) : SensorData()
    
    data class Uwb(
        val distance: Float,
        val angle: Float,
        val confidence: Float,
        override val timestamp: Long
    ) : SensorData()
}

/**
 * Single sensor detection
 */
data class Detection(
    val source: DataSource,
    val confidence: Float,
    val distance: Float?,
    val angle: Float?,
    val isMoving: Boolean?,
    val timestamp: Long
)

/**
 * Target tracking across frames
 */
class TargetTracker {
    private val activeTargets = mutableMapOf<String, TrackedTarget>()
    private val maxAge = 5000L // 5 seconds
    
    data class TrackedTarget(
        val id: String,
        val detections: MutableList<Detection>,
        var lastUpdated: Long,
        var angle: Float?,
        var distance: Float?,
        var confidence: Float,
        var isMoving: Boolean
    )
    
    /**
     * Update tracker with new detection
     */
    fun update(detection: Detection) {
        // Find existing target or create new one
        val targetId = findMatchingTarget(detection) ?: UUID.randomUUID().toString()
        
        val target = activeTargets.getOrPut(targetId) {
            TrackedTarget(
                id = targetId,
                detections = mutableListOf(),
                lastUpdated = detection.timestamp,
                angle = detection.angle,
                distance = detection.distance,
                confidence = 0f,
                isMoving = false
            )
        }
        
        // Add detection
        target.detections.add(detection)
        if (target.detections.size > 20) {
            target.detections.removeAt(0)
        }
        
        // Update target properties
        target.lastUpdated = detection.timestamp
        target.angle = detection.angle ?: target.angle
        target.distance = detection.distance ?: target.distance
        target.isMoving = detection.isMoving ?: target.isMoving
        
        // Recalculate confidence
        target.confidence = target.detections
            .map { it.confidence }
            .average()
            .toFloat()
            .coerceIn(0f, 1f)
        
        // Cleanup old targets
        cleanupOldTargets()
    }
    
    /**
     * Get all active targets as RadarTargets
     */
    fun getActiveTargets(): List<RadarTarget> {
        return activeTargets.values.map { tracked ->
            RadarTarget(
                id = tracked.id,
                angleDegrees = tracked.angle ?: 0f,
                distanceMeters = tracked.distance,
                confidence = tracked.confidence,
                type = classifyTrackedTarget(tracked),
                isMoving = tracked.isMoving,
                lastUpdated = tracked.lastUpdated,
                dataSources = tracked.detections.map { it.source }.toSet()
            )
        }
    }
    
    /**
     * Find existing target that matches new detection
     */
    private fun findMatchingTarget(detection: Detection): String? {
        val now = System.currentTimeMillis()
        
        return activeTargets.entries.find { (_, target) ->
            // Match based on similar angle/distance
            val ageOk = now - target.lastUpdated < 2000
            val angleMatch = detection.angle == null || 
                            target.angle == null ||
                            abs(detection.angle - target.angle!!) < 30f
            val distanceMatch = detection.distance == null ||
                               target.distance == null ||
                               abs(detection.distance - target.distance!!) < 3f
            
            ageOk && angleMatch && distanceMatch
        }?.key
    }
    
    /**
     * Classify tracked target
     */
    private fun classifyTrackedTarget(target: TrackedTarget): TargetType {
        val sensorCount = target.detections.map { it.source }.distinct().size
        
        return when {
            target.confidence > 0.7f && sensorCount >= 2 -> TargetType.HUMAN
            target.confidence > 0.5f -> TargetType.POSSIBLE_LIFE
            target.confidence > 0.3f -> TargetType.POSSIBLE_LIFE
            target.confidence < 0.2f -> TargetType.NOISE
            else -> TargetType.UNKNOWN
        }
    }
    
    /**
     * Remove old targets
     */
    private fun cleanupOldTargets() {
        val cutoff = System.currentTimeMillis() - maxAge
        activeTargets.entries.removeIf { it.value.lastUpdated < cutoff }
    }
    
    /**
     * Clear all targets
     */
    fun clear() {
        activeTargets.clear()
    }
}
