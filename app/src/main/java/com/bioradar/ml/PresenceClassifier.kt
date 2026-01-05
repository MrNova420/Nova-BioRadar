package com.bioradar.ml

import android.content.Context
import com.bioradar.core.models.DataSource
import com.bioradar.core.models.RadarTarget
import com.bioradar.core.models.TargetType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device ML classifier for presence detection
 * Uses TensorFlow Lite for inference (placeholder implementation)
 * 
 * In production, this would load a .tflite model bundled in the APK
 * and perform real inference on sensor features
 */
@Singleton
class PresenceClassifier @Inject constructor(
    private val context: Context
) {
    private var isModelLoaded = false
    private var modelVersion = "1.0.0"
    
    // Classification confidence thresholds
    private val humanThreshold = 0.75f
    private val lifeFormThreshold = 0.50f
    private val noiseThreshold = 0.25f
    
    /**
     * Initialize the classifier and load the model
     */
    fun initialize(): Boolean {
        return try {
            // In production, load TensorFlow Lite model:
            // val model = loadModel("presence_classifier.tflite")
            isModelLoaded = true
            true
        } catch (e: Exception) {
            isModelLoaded = false
            false
        }
    }
    
    /**
     * Classify detected presence based on sensor features
     */
    fun classify(features: PresenceFeatures): ClassificationResult {
        if (!isModelLoaded) {
            // Fallback to rule-based classification
            return classifyRuleBased(features)
        }
        
        // In production, run TensorFlow Lite inference:
        // val output = runInference(features.toFloatArray())
        // return ClassificationResult.fromOutput(output)
        
        return classifyRuleBased(features)
    }
    
    /**
     * Classify a radar target
     */
    fun classifyTarget(target: RadarTarget): TargetType {
        val features = extractFeatures(target)
        val result = classify(features)
        return result.targetType
    }
    
    /**
     * Rule-based classification fallback
     * Used when ML model is not available
     */
    private fun classifyRuleBased(features: PresenceFeatures): ClassificationResult {
        // Calculate composite scores for each class
        val humanScore = calculateHumanScore(features)
        val lifeFormScore = calculateLifeFormScore(features)
        val noiseScore = calculateNoiseScore(features)
        
        // Determine classification
        val (type, confidence) = when {
            humanScore > humanThreshold -> TargetType.HUMAN to humanScore
            lifeFormScore > lifeFormThreshold -> TargetType.POSSIBLE_LIFE to lifeFormScore
            noiseScore > features.overallConfidence -> TargetType.NOISE to (1f - noiseScore)
            else -> TargetType.UNKNOWN to features.overallConfidence
        }
        
        return ClassificationResult(
            targetType = type,
            confidence = confidence,
            humanProbability = humanScore,
            lifeFormProbability = lifeFormScore,
            noiseProbability = noiseScore,
            features = features
        )
    }
    
    /**
     * Calculate score for human presence
     */
    private fun calculateHumanScore(features: PresenceFeatures): Float {
        var score = 0f
        var weights = 0f
        
        // Motion characteristics (humans have specific motion patterns)
        if (features.hasMotion) {
            score += 0.3f
            weights += 0.3f
            
            // Walking pattern detection (1-2 Hz oscillation)
            if (features.motionFrequency in 0.8f..2.5f) {
                score += 0.2f
            }
        }
        weights += 0.2f
        
        // Breathing detection (0.2-0.5 Hz subtle oscillation)
        if (features.breathingSignature in 0.15f..0.6f) {
            score += 0.25f
        }
        weights += 0.25f
        
        // Multi-sensor confirmation
        if (features.sensorCount >= 2) {
            score += 0.15f * (features.sensorCount.coerceAtMost(4) / 4f)
        }
        weights += 0.15f
        
        // Size/signature estimation
        if (features.estimatedSize in 0.3f..2.5f) { // Human-sized
            score += 0.1f
        }
        weights += 0.1f
        
        return (score / weights).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate score for any life form
     */
    private fun calculateLifeFormScore(features: PresenceFeatures): Float {
        var score = 0f
        
        // Any significant motion
        if (features.hasMotion) {
            score += 0.3f
        }
        
        // Signal variance (living things cause variable signals)
        if (features.signalVariance > 10f) {
            score += 0.2f
        }
        
        // Temperature anomaly would indicate life (future sensor)
        // score += features.thermalAnomaly * 0.3f
        
        // General presence confidence
        score += features.overallConfidence * 0.5f
        
        return score.coerceIn(0f, 1f)
    }
    
    /**
     * Calculate score for noise/interference
     */
    private fun calculateNoiseScore(features: PresenceFeatures): Float {
        var score = 0f
        
        // Very erratic signals indicate noise
        if (features.signalVariance > 50f) {
            score += 0.3f
        }
        
        // Single sensor only - less reliable
        if (features.sensorCount == 1) {
            score += 0.2f
        }
        
        // Very short duration detections
        if (features.duration < 500) {
            score += 0.3f
        }
        
        // Low confidence readings
        if (features.overallConfidence < 0.3f) {
            score += 0.2f
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    /**
     * Extract features from a radar target
     */
    private fun extractFeatures(target: RadarTarget): PresenceFeatures {
        return PresenceFeatures(
            hasMotion = target.isMoving,
            motionFrequency = target.velocity?.let { 
                // Estimate step frequency from velocity
                (it / 0.7f).coerceIn(0f, 5f) 
            } ?: 0f,
            breathingSignature = 0f, // Would come from CSI analysis
            signalVariance = (target.confidence * 30f), // Estimated
            estimatedSize = 1.5f, // Average human
            sensorCount = target.dataSources.size,
            overallConfidence = target.confidence,
            duration = System.currentTimeMillis() - target.lastUpdated,
            dataSources = target.dataSources
        )
    }
    
    /**
     * Get model information
     */
    fun getModelInfo(): ModelInfo {
        return ModelInfo(
            isLoaded = isModelLoaded,
            version = modelVersion,
            type = if (isModelLoaded) "TensorFlow Lite" else "Rule-Based Fallback"
        )
    }
    
    /**
     * Release model resources
     */
    fun release() {
        isModelLoaded = false
        // In production: interpreter?.close()
    }
}

/**
 * Features extracted for classification
 */
data class PresenceFeatures(
    val hasMotion: Boolean,
    val motionFrequency: Float,      // Hz - walking ~1-2Hz
    val breathingSignature: Float,   // Hz - breathing ~0.2-0.5Hz
    val signalVariance: Float,       // RSSI/CSI variance
    val estimatedSize: Float,        // Meters
    val sensorCount: Int,
    val overallConfidence: Float,
    val duration: Long,              // Milliseconds
    val dataSources: Set<DataSource>
) {
    /**
     * Convert to float array for ML inference
     */
    fun toFloatArray(): FloatArray {
        return floatArrayOf(
            if (hasMotion) 1f else 0f,
            motionFrequency,
            breathingSignature,
            signalVariance,
            estimatedSize,
            sensorCount.toFloat(),
            overallConfidence,
            (duration / 1000f).coerceAtMost(60f) // Cap at 60 seconds
        )
    }
}

/**
 * Classification result
 */
data class ClassificationResult(
    val targetType: TargetType,
    val confidence: Float,
    val humanProbability: Float,
    val lifeFormProbability: Float,
    val noiseProbability: Float,
    val features: PresenceFeatures
) {
    companion object {
        /**
         * Create from ML model output
         */
        fun fromOutput(output: FloatArray): ClassificationResult {
            val humanProb = output.getOrElse(0) { 0f }
            val lifeProb = output.getOrElse(1) { 0f }
            val noiseProb = output.getOrElse(2) { 0f }
            val unknownProb = output.getOrElse(3) { 0f }
            
            val (type, conf) = when {
                humanProb > lifeProb && humanProb > noiseProb && humanProb > unknownProb ->
                    TargetType.HUMAN to humanProb
                lifeProb > noiseProb && lifeProb > unknownProb ->
                    TargetType.POSSIBLE_LIFE to lifeProb
                noiseProb > unknownProb ->
                    TargetType.NOISE to noiseProb
                else ->
                    TargetType.UNKNOWN to unknownProb
            }
            
            return ClassificationResult(
                targetType = type,
                confidence = conf,
                humanProbability = humanProb,
                lifeFormProbability = lifeProb,
                noiseProbability = noiseProb,
                features = PresenceFeatures(
                    hasMotion = false,
                    motionFrequency = 0f,
                    breathingSignature = 0f,
                    signalVariance = 0f,
                    estimatedSize = 0f,
                    sensorCount = 0,
                    overallConfidence = conf,
                    duration = 0,
                    dataSources = emptySet()
                )
            )
        }
    }
}

/**
 * Model information
 */
data class ModelInfo(
    val isLoaded: Boolean,
    val version: String,
    val type: String
)
