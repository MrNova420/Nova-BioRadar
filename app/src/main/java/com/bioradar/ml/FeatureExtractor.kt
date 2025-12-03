package com.bioradar.ml

import com.bioradar.core.models.DataSource
import com.bioradar.core.models.SensorReading
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Feature extractor for ML classification
 * Processes raw sensor data into features suitable for the classifier
 */
@Singleton
class FeatureExtractor @Inject constructor() {
    
    // Circular buffers for temporal analysis
    private val rssiHistory = mutableListOf<Float>()
    private val motionHistory = mutableListOf<Float>()
    private val sonarHistory = mutableListOf<Float>()
    
    private val maxHistorySize = 100
    
    /**
     * Extract features from a collection of sensor readings
     */
    fun extractFeatures(readings: List<SensorReading>): PresenceFeatures {
        // Group readings by source
        val bySource = readings.groupBy { it.source }
        
        // Process each sensor type
        val rssiFeatures = extractRssiFeatures(
            bySource[DataSource.WIFI].orEmpty() + bySource[DataSource.BLUETOOTH].orEmpty()
        )
        
        val sonarFeatures = extractSonarFeatures(
            bySource[DataSource.SONAR].orEmpty()
        )
        
        val cameraFeatures = extractCameraFeatures(
            bySource[DataSource.CAMERA].orEmpty()
        )
        
        // Combine into unified features
        return PresenceFeatures(
            hasMotion = cameraFeatures.hasMotion || rssiFeatures.hasVariance,
            motionFrequency = cameraFeatures.motionFrequency,
            breathingSignature = detectBreathingSignature(),
            signalVariance = rssiFeatures.variance,
            estimatedSize = estimateTargetSize(readings),
            sensorCount = bySource.keys.size,
            overallConfidence = calculateOverallConfidence(rssiFeatures, sonarFeatures, cameraFeatures),
            duration = calculateDetectionDuration(readings),
            dataSources = bySource.keys
        )
    }
    
    /**
     * Extract features from RSSI readings (WiFi + Bluetooth)
     */
    private fun extractRssiFeatures(readings: List<SensorReading>): RssiFeatures {
        if (readings.isEmpty()) {
            return RssiFeatures(variance = 0f, mean = 0f, hasVariance = false)
        }
        
        val values = readings.flatMap { it.values.toList() }
        
        // Update history
        values.forEach { value ->
            rssiHistory.add(value)
            if (rssiHistory.size > maxHistorySize) {
                rssiHistory.removeAt(0)
            }
        }
        
        // Calculate statistics
        val mean = rssiHistory.average().toFloat()
        val variance = rssiHistory.map { (it - mean) * (it - mean) }.average().toFloat()
        
        return RssiFeatures(
            variance = variance,
            mean = mean,
            hasVariance = variance > 5f
        )
    }
    
    /**
     * Extract features from sonar readings
     */
    private fun extractSonarFeatures(readings: List<SensorReading>): SonarFeatures {
        if (readings.isEmpty()) {
            return SonarFeatures(energy = 0f, hasEcho = false, distance = null)
        }
        
        val values = readings.flatMap { it.values.toList() }
        
        // Update history
        values.forEach { value ->
            sonarHistory.add(value)
            if (sonarHistory.size > maxHistorySize) {
                sonarHistory.removeAt(0)
            }
        }
        
        // Calculate energy
        val energy = sonarHistory.map { it * it }.average().toFloat()
        val maxValue = sonarHistory.maxOrNull() ?: 0f
        
        // Extract distance from metadata if available
        val distance = readings.lastOrNull()?.metadata?.get("distance") as? Float
        
        return SonarFeatures(
            energy = energy,
            hasEcho = maxValue > 0.1f,
            distance = distance
        )
    }
    
    /**
     * Extract features from camera motion readings
     */
    private fun extractCameraFeatures(readings: List<SensorReading>): CameraFeatures {
        if (readings.isEmpty()) {
            return CameraFeatures(hasMotion = false, motionMagnitude = 0f, motionFrequency = 0f)
        }
        
        val values = readings.flatMap { it.values.toList() }
        
        // Update history
        values.forEach { value ->
            motionHistory.add(value)
            if (motionHistory.size > maxHistorySize) {
                motionHistory.removeAt(0)
            }
        }
        
        // Calculate motion magnitude
        val magnitude = motionHistory.average().toFloat()
        val hasMotion = magnitude > 0.1f
        
        // Estimate motion frequency from zero-crossings
        val frequency = if (motionHistory.size > 10) {
            calculateMotionFrequency()
        } else 0f
        
        return CameraFeatures(
            hasMotion = hasMotion,
            motionMagnitude = magnitude,
            motionFrequency = frequency
        )
    }
    
    /**
     * Detect breathing signature from signal patterns
     * Breathing causes 0.2-0.5 Hz oscillation in signals
     */
    private fun detectBreathingSignature(): Float {
        if (rssiHistory.size < 50) return 0f
        
        // Simple frequency analysis
        val recentHistory = rssiHistory.takeLast(50)
        val mean = recentHistory.average()
        
        // Count zero-crossings (around mean)
        var crossings = 0
        for (i in 1 until recentHistory.size) {
            if ((recentHistory[i] >= mean && recentHistory[i-1] < mean) ||
                (recentHistory[i] < mean && recentHistory[i-1] >= mean)) {
                crossings++
            }
        }
        
        // Estimate frequency (assuming ~10Hz sample rate)
        val frequency = crossings * 10f / (2 * recentHistory.size)
        
        // Return frequency if in breathing range
        return if (frequency in 0.15f..0.6f) frequency else 0f
    }
    
    /**
     * Calculate motion frequency from optical flow history
     */
    private fun calculateMotionFrequency(): Float {
        val recentMotion = motionHistory.takeLast(30)
        val mean = recentMotion.average()
        
        var crossings = 0
        for (i in 1 until recentMotion.size) {
            if ((recentMotion[i] >= mean && recentMotion[i-1] < mean) ||
                (recentMotion[i] < mean && recentMotion[i-1] >= mean)) {
                crossings++
            }
        }
        
        // Estimate frequency (assuming ~30Hz camera)
        return crossings * 30f / (2 * recentMotion.size)
    }
    
    /**
     * Estimate target size from sensor readings
     */
    private fun estimateTargetSize(readings: List<SensorReading>): Float {
        // Would use sonar echoes and camera blob size
        // Placeholder: return average human size
        return 1.5f
    }
    
    /**
     * Calculate overall detection confidence
     */
    private fun calculateOverallConfidence(
        rssi: RssiFeatures,
        sonar: SonarFeatures,
        camera: CameraFeatures
    ): Float {
        var confidence = 0f
        var weights = 0f
        
        if (rssi.hasVariance) {
            confidence += 0.3f
            weights += 0.3f
        }
        
        if (sonar.hasEcho) {
            confidence += 0.35f
            weights += 0.35f
        }
        
        if (camera.hasMotion) {
            confidence += 0.35f
            weights += 0.35f
        }
        
        return if (weights > 0) confidence / weights else 0f
    }
    
    /**
     * Calculate detection duration
     */
    private fun calculateDetectionDuration(readings: List<SensorReading>): Long {
        if (readings.isEmpty()) return 0
        
        val timestamps = readings.map { it.timestamp }
        return (timestamps.maxOrNull() ?: 0) - (timestamps.minOrNull() ?: 0)
    }
    
    /**
     * Clear all history buffers
     */
    fun clearHistory() {
        rssiHistory.clear()
        motionHistory.clear()
        sonarHistory.clear()
    }
}

/**
 * RSSI feature summary
 */
private data class RssiFeatures(
    val variance: Float,
    val mean: Float,
    val hasVariance: Boolean
)

/**
 * Sonar feature summary
 */
private data class SonarFeatures(
    val energy: Float,
    val hasEcho: Boolean,
    val distance: Float?
)

/**
 * Camera feature summary
 */
private data class CameraFeatures(
    val hasMotion: Boolean,
    val motionMagnitude: Float,
    val motionFrequency: Float
)
