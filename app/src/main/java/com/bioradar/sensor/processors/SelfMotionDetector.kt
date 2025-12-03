package com.bioradar.sensor.processors

import kotlin.math.*

/**
 * Self-motion detector using accelerometer data
 * Filters out false positives caused by device movement
 */
class SelfMotionDetector {
    
    private val accelerometerHistory = mutableListOf<AccelerometerSample>()
    private var lastStationaryTime = System.currentTimeMillis()
    private var movementStartTime: Long? = null
    
    data class AccelerometerSample(
        val x: Float,
        val y: Float,
        val z: Float,
        val timestamp: Long
    ) {
        val magnitude: Float get() = sqrt(x * x + y * y + z * z)
    }
    
    data class MotionState(
        val isMoving: Boolean,
        val isWalking: Boolean,
        val isStationary: Boolean,
        val motionMagnitude: Float,
        val movementDuration: Long,
        val stepFrequency: Float?
    )
    
    /**
     * Add accelerometer reading
     */
    fun addReading(x: Float, y: Float, z: Float, timestamp: Long = System.currentTimeMillis()) {
        val sample = AccelerometerSample(x, y, z, timestamp)
        accelerometerHistory.add(sample)
        
        // Keep only recent history
        val cutoff = timestamp - HISTORY_WINDOW_MS
        accelerometerHistory.removeAll { it.timestamp < cutoff }
        
        // Update stationary time
        if (!isMoving()) {
            lastStationaryTime = timestamp
            movementStartTime = null
        } else if (movementStartTime == null) {
            movementStartTime = timestamp
        }
    }
    
    /**
     * Check if device is currently moving
     */
    fun isMoving(): Boolean {
        if (accelerometerHistory.size < MIN_SAMPLES) return false
        
        val recentSamples = accelerometerHistory.takeLast(RECENT_SAMPLES_COUNT)
        val magnitudes = recentSamples.map { it.magnitude }
        
        // Check variance of magnitude (should be ~9.8 m/s² when stationary)
        val mean = magnitudes.average().toFloat()
        val variance = magnitudes.map { (it - mean).pow(2) }.average().toFloat()
        
        return variance > MOVEMENT_VARIANCE_THRESHOLD
    }
    
    /**
     * Check if user is walking
     */
    fun isWalking(): Boolean {
        if (accelerometerHistory.size < MIN_SAMPLES_FOR_WALKING) return false
        
        val recentSamples = accelerometerHistory.takeLast(WALKING_ANALYSIS_SAMPLES)
        
        // Detect step frequency (typically 1-2 Hz for walking)
        val stepFreq = detectStepFrequency(recentSamples) ?: return false
        
        return stepFreq in 0.8f..3f
    }
    
    /**
     * Get current motion state
     */
    fun getMotionState(): MotionState {
        val isMoving = isMoving()
        val isWalking = isWalking()
        
        val motionMagnitude = if (accelerometerHistory.isNotEmpty()) {
            val recentSamples = accelerometerHistory.takeLast(RECENT_SAMPLES_COUNT)
            val magnitudes = recentSamples.map { it.magnitude }
            val mean = magnitudes.average().toFloat()
            val variance = magnitudes.map { (it - mean).pow(2) }.average().toFloat()
            sqrt(variance).coerceIn(0f, 10f) / 10f
        } else 0f
        
        val movementDuration = movementStartTime?.let { 
            System.currentTimeMillis() - it 
        } ?: 0L
        
        val stepFrequency = if (isWalking) {
            detectStepFrequency(accelerometerHistory.takeLast(WALKING_ANALYSIS_SAMPLES))
        } else null
        
        return MotionState(
            isMoving = isMoving,
            isWalking = isWalking,
            isStationary = !isMoving && 
                System.currentTimeMillis() - lastStationaryTime > STATIONARY_CONFIRM_MS,
            motionMagnitude = motionMagnitude,
            movementDuration = movementDuration,
            stepFrequency = stepFrequency
        )
    }
    
    /**
     * Calculate motion compensation factor
     * Returns value to reduce confidence when device is moving
     */
    fun getMotionCompensation(): Float {
        val state = getMotionState()
        
        return when {
            state.isWalking -> 0.3f  // Heavy compensation for walking
            state.isMoving -> 0.5f   // Moderate compensation for movement
            state.isStationary -> 1.0f  // No compensation when stationary
            else -> 0.8f  // Slight compensation for unknown state
        }
    }
    
    /**
     * Detect step frequency from accelerometer data
     */
    private fun detectStepFrequency(samples: List<AccelerometerSample>): Float? {
        if (samples.size < 20) return null
        
        val magnitudes = samples.map { it.magnitude }
        val mean = magnitudes.average().toFloat()
        val centered = magnitudes.map { it - mean }
        
        // Count zero crossings
        var zeroCrossings = 0
        for (i in 1 until centered.size) {
            if ((centered[i] >= 0 && centered[i-1] < 0) ||
                (centered[i] < 0 && centered[i-1] >= 0)) {
                zeroCrossings++
            }
        }
        
        // Calculate time span
        val timeSpan = (samples.last().timestamp - samples.first().timestamp) / 1000f
        if (timeSpan < 1f) return null
        
        // Step frequency is half the zero-crossing rate
        return zeroCrossings / (2 * timeSpan)
    }
    
    /**
     * Get time since device was last stationary
     */
    fun getTimeSinceStationary(): Long {
        return System.currentTimeMillis() - lastStationaryTime
    }
    
    /**
     * Clear history
     */
    fun clear() {
        accelerometerHistory.clear()
        lastStationaryTime = System.currentTimeMillis()
        movementStartTime = null
    }
    
    companion object {
        private const val HISTORY_WINDOW_MS = 10000L  // 10 seconds
        private const val MIN_SAMPLES = 10
        private const val RECENT_SAMPLES_COUNT = 20
        private const val MIN_SAMPLES_FOR_WALKING = 50
        private const val WALKING_ANALYSIS_SAMPLES = 100
        private const val MOVEMENT_VARIANCE_THRESHOLD = 0.5f
        private const val STATIONARY_CONFIRM_MS = 3000L
    }
}
