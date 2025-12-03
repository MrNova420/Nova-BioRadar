package com.bioradar.sensor.processors

import kotlin.math.*

/**
 * RSSI Analyzer for WiFi and Bluetooth signal analysis
 * Detects presence through signal variance patterns
 */
class RssiAnalyzer {
    
    private val signalHistory = mutableMapOf<String, MutableList<RssiSample>>()
    
    data class RssiSample(
        val rssi: Int,
        val timestamp: Long
    )
    
    data class VarianceResult(
        val variance: Float,
        val standardDeviation: Float,
        val mean: Float,
        val sampleCount: Int,
        val isStable: Boolean,
        val motionDetected: Boolean
    )
    
    /**
     * Add a new RSSI sample
     */
    fun addSample(deviceId: String, rssi: Int, timestamp: Long = System.currentTimeMillis()) {
        val history = signalHistory.getOrPut(deviceId) { mutableListOf() }
        history.add(RssiSample(rssi, timestamp))
        
        // Keep only recent samples
        val cutoff = timestamp - HISTORY_WINDOW_MS
        history.removeAll { it.timestamp < cutoff }
        
        // Limit history size
        while (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(0)
        }
    }
    
    /**
     * Calculate variance for a device
     */
    fun calculateVariance(deviceId: String): VarianceResult {
        val history = signalHistory[deviceId] ?: return VarianceResult(
            variance = 0f,
            standardDeviation = 0f,
            mean = 0f,
            sampleCount = 0,
            isStable = true,
            motionDetected = false
        )
        
        if (history.size < MIN_SAMPLES_FOR_VARIANCE) {
            return VarianceResult(
                variance = 0f,
                standardDeviation = 0f,
                mean = history.map { it.rssi }.average().toFloat(),
                sampleCount = history.size,
                isStable = true,
                motionDetected = false
            )
        }
        
        val rssiValues = history.map { it.rssi.toFloat() }
        val mean = rssiValues.average().toFloat()
        val variance = rssiValues.map { (it - mean).pow(2) }.average().toFloat()
        val stdDev = sqrt(variance)
        
        return VarianceResult(
            variance = variance,
            standardDeviation = stdDev,
            mean = mean,
            sampleCount = history.size,
            isStable = variance < STABLE_VARIANCE_THRESHOLD,
            motionDetected = variance > MOTION_VARIANCE_THRESHOLD
        )
    }
    
    /**
     * Calculate overall presence score from all tracked devices
     */
    fun calculatePresenceScore(): Float {
        if (signalHistory.isEmpty()) return 0f
        
        val variances = signalHistory.keys.map { calculateVariance(it) }
        val avgVariance = variances.map { it.variance }.average().toFloat()
        val maxVariance = variances.maxOfOrNull { it.variance } ?: 0f
        val motionDevices = variances.count { it.motionDetected }
        
        // Calculate score based on variance and motion detection
        val varianceScore = (avgVariance / VARIANCE_NORMALIZATION).coerceIn(0f, 1f)
        val motionScore = (motionDevices.toFloat() / variances.size).coerceIn(0f, 1f)
        
        return varianceScore * 0.6f + motionScore * 0.4f
    }
    
    /**
     * Detect through-wall human presence
     * Uses CSI-like analysis of RSSI patterns
     */
    fun detectThroughWallPresence(): ThroughWallResult {
        if (signalHistory.isEmpty()) {
            return ThroughWallResult(detected = false, confidence = 0f)
        }
        
        var breathingPatternCount = 0
        var walkingPatternCount = 0
        var totalDevices = 0
        
        for ((_, history) in signalHistory) {
            if (history.size < MIN_SAMPLES_FOR_PATTERN) continue
            totalDevices++
            
            // Analyze for breathing frequency (0.2-0.5 Hz)
            if (detectBreathingPattern(history)) {
                breathingPatternCount++
            }
            
            // Analyze for walking pattern (1-2 Hz)
            if (detectWalkingPattern(history)) {
                walkingPatternCount++
            }
        }
        
        if (totalDevices == 0) {
            return ThroughWallResult(detected = false, confidence = 0f)
        }
        
        val breathingRatio = breathingPatternCount.toFloat() / totalDevices
        val walkingRatio = walkingPatternCount.toFloat() / totalDevices
        
        val detected = breathingRatio > 0.3f || walkingRatio > 0.2f
        val confidence = (breathingRatio * 0.6f + walkingRatio * 0.4f).coerceIn(0f, 1f)
        
        return ThroughWallResult(
            detected = detected,
            confidence = confidence,
            isBreathing = breathingRatio > 0.3f,
            isWalking = walkingRatio > 0.2f
        )
    }
    
    /**
     * Detect breathing pattern in RSSI data
     * Breathing causes 0.2-0.5 Hz oscillations
     */
    private fun detectBreathingPattern(history: List<RssiSample>): Boolean {
        if (history.size < 20) return false
        
        // Calculate zero-crossing rate
        val rssiValues = history.map { it.rssi }
        val mean = rssiValues.average()
        val centered = rssiValues.map { it - mean }
        
        var zeroCrossings = 0
        for (i in 1 until centered.size) {
            if ((centered[i] >= 0 && centered[i-1] < 0) ||
                (centered[i] < 0 && centered[i-1] >= 0)) {
                zeroCrossings++
            }
        }
        
        // Calculate time span
        val timeSpan = (history.last().timestamp - history.first().timestamp) / 1000f
        if (timeSpan < 5f) return false
        
        val frequency = zeroCrossings / (2 * timeSpan)
        
        // Breathing frequency is typically 0.2-0.5 Hz
        return frequency in 0.15f..0.6f
    }
    
    /**
     * Detect walking pattern in RSSI data
     * Walking causes 1-2 Hz oscillations
     */
    private fun detectWalkingPattern(history: List<RssiSample>): Boolean {
        if (history.size < 10) return false
        
        val variance = history.map { it.rssi.toFloat() }
            .let { values ->
                val mean = values.average().toFloat()
                values.map { (it - mean).pow(2) }.average().toFloat()
            }
        
        // Walking typically causes higher variance
        return variance > WALKING_VARIANCE_THRESHOLD
    }
    
    /**
     * Clear history for a device
     */
    fun clearDevice(deviceId: String) {
        signalHistory.remove(deviceId)
    }
    
    /**
     * Clear all history
     */
    fun clearAll() {
        signalHistory.clear()
    }
    
    /**
     * Get tracked device count
     */
    fun getDeviceCount(): Int = signalHistory.size
    
    data class ThroughWallResult(
        val detected: Boolean,
        val confidence: Float,
        val isBreathing: Boolean = false,
        val isWalking: Boolean = false
    )
    
    companion object {
        private const val HISTORY_WINDOW_MS = 30000L  // 30 seconds
        private const val MAX_HISTORY_SIZE = 500
        private const val MIN_SAMPLES_FOR_VARIANCE = 5
        private const val MIN_SAMPLES_FOR_PATTERN = 20
        private const val STABLE_VARIANCE_THRESHOLD = 5f
        private const val MOTION_VARIANCE_THRESHOLD = 25f
        private const val WALKING_VARIANCE_THRESHOLD = 50f
        private const val VARIANCE_NORMALIZATION = 100f
    }
}
