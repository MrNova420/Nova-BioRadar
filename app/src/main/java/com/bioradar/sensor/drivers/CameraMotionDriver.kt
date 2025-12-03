package com.bioradar.sensor.drivers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.media.Image
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.bioradar.core.models.CameraMotionReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Camera Motion Detection Driver
 * Uses optical flow analysis to detect movement and direction
 */
@Singleton
class CameraMotionDriver @Inject constructor(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService? = null
    
    private var previousFrame: ByteArray? = null
    private var previousTimestamp: Long = 0
    
    private val motionHistory = mutableListOf<CameraMotionReading>()
    private val targetResolution = Size(160, 120) // Low resolution for performance
    
    /**
     * Check if camera is available
     */
    fun isAvailable(): Boolean {
        return hasPermission() && 
               context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    
    /**
     * Start camera motion detection
     * Returns a Flow of motion readings
     */
    fun startMotionDetection(
        lifecycleOwner: LifecycleOwner
    ): Flow<CameraMotionReading> = callbackFlow {
        if (!isAvailable()) {
            close()
            return@callbackFlow
        }
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(targetResolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor!!) { imageProxy ->
                        val motionReading = analyzeFrame(imageProxy)
                        if (motionReading != null) {
                            motionHistory.add(motionReading)
                            if (motionHistory.size > MAX_HISTORY_SIZE) {
                                motionHistory.removeAt(0)
                            }
                            trySend(motionReading)
                        }
                        imageProxy.close()
                    }
                }
            
            try {
                cameraProvider?.unbindAll()
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                close(e)
            }
        }, ContextCompat.getMainExecutor(context))
        
        awaitClose {
            stop()
        }
    }
    
    /**
     * Analyze a single frame for motion
     */
    private fun analyzeFrame(imageProxy: ImageProxy): CameraMotionReading? {
        val currentFrame = imageProxy.planes[0].buffer.toByteArray()
        val currentTimestamp = imageProxy.imageInfo.timestamp
        
        val reading = if (previousFrame != null && previousFrame!!.size == currentFrame.size) {
            calculateOpticalFlow(
                previousFrame!!,
                currentFrame,
                imageProxy.width,
                imageProxy.height
            )
        } else null
        
        previousFrame = currentFrame.copyOf()
        previousTimestamp = currentTimestamp
        
        return reading
    }
    
    /**
     * Calculate optical flow between two frames
     * Returns motion reading with magnitude and direction
     */
    private fun calculateOpticalFlow(
        prevFrame: ByteArray,
        currFrame: ByteArray,
        width: Int,
        height: Int
    ): CameraMotionReading {
        val blockSize = 16
        val motionVectors = mutableListOf<MotionVector>()
        
        // Simple block matching algorithm
        for (by in 0 until height - blockSize step blockSize) {
            for (bx in 0 until width - blockSize step blockSize) {
                val (dx, dy, sad) = findBestMatch(
                    prevFrame, currFrame,
                    bx, by, blockSize,
                    width, height
                )
                
                if (sad < MOTION_THRESHOLD && (dx != 0 || dy != 0)) {
                    motionVectors.add(MotionVector(bx, by, dx, dy, sad))
                }
            }
        }
        
        // Calculate overall motion
        if (motionVectors.isEmpty()) {
            return CameraMotionReading(
                sector = 0,
                motionMagnitude = 0f,
                angleDegrees = 0f,
                timestamp = System.currentTimeMillis()
            )
        }
        
        // Average motion vector
        val avgDx = motionVectors.map { it.dx }.average()
        val avgDy = motionVectors.map { it.dy }.average()
        
        // Calculate magnitude and angle
        val magnitude = sqrt(avgDx * avgDx + avgDy * avgDy).toFloat()
        val normalizedMagnitude = (magnitude / MAX_MOTION).coerceIn(0f, 1f)
        
        // Angle in degrees (0 = right, 90 = down, 180 = left, 270 = up)
        val angleRadians = atan2(avgDy, avgDx)
        val angleDegrees = (Math.toDegrees(angleRadians) + 360) % 360
        
        // Convert to 8 sectors (N, NE, E, SE, S, SW, W, NW)
        val sector = ((angleDegrees + 22.5) / 45).toInt() % 8
        
        return CameraMotionReading(
            sector = sector,
            motionMagnitude = normalizedMagnitude,
            angleDegrees = angleDegrees.toFloat(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Find best matching block using Sum of Absolute Differences
     */
    private fun findBestMatch(
        prevFrame: ByteArray,
        currFrame: ByteArray,
        blockX: Int,
        blockY: Int,
        blockSize: Int,
        width: Int,
        height: Int
    ): Triple<Int, Int, Int> {
        val searchRange = 8
        var bestDx = 0
        var bestDy = 0
        var bestSad = Int.MAX_VALUE
        
        for (dy in -searchRange..searchRange) {
            for (dx in -searchRange..searchRange) {
                val newX = blockX + dx
                val newY = blockY + dy
                
                if (newX < 0 || newY < 0 || 
                    newX + blockSize >= width || 
                    newY + blockSize >= height) {
                    continue
                }
                
                var sad = 0
                for (y in 0 until blockSize) {
                    for (x in 0 until blockSize) {
                        val prevIdx = (blockY + y) * width + (blockX + x)
                        val currIdx = (newY + y) * width + (newX + x)
                        
                        if (prevIdx < prevFrame.size && currIdx < currFrame.size) {
                            sad += kotlin.math.abs(
                                (prevFrame[prevIdx].toInt() and 0xFF) - 
                                (currFrame[currIdx].toInt() and 0xFF)
                            )
                        }
                    }
                }
                
                if (sad < bestSad) {
                    bestSad = sad
                    bestDx = dx
                    bestDy = dy
                }
            }
        }
        
        return Triple(bestDx, bestDy, bestSad)
    }
    
    /**
     * Get average motion in a specific sector
     */
    fun getMotionInSector(sector: Int, maxAgeMs: Long = 2000): Float {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        val recentReadings = motionHistory.filter { 
            it.timestamp > cutoff && it.sector == sector 
        }
        
        return recentReadings.map { it.motionMagnitude }.average().toFloat()
            .takeIf { !it.isNaN() } ?: 0f
    }
    
    /**
     * Get overall motion score
     */
    fun getOverallMotion(maxAgeMs: Long = 2000): Float {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        val recentReadings = motionHistory.filter { it.timestamp > cutoff }
        
        return recentReadings.map { it.motionMagnitude }.average().toFloat()
            .takeIf { !it.isNaN() } ?: 0f
    }
    
    /**
     * Get dominant motion direction
     */
    fun getDominantDirection(maxAgeMs: Long = 2000): Float? {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        val recentReadings = motionHistory.filter { 
            it.timestamp > cutoff && it.motionMagnitude > 0.1f 
        }
        
        return if (recentReadings.isNotEmpty()) {
            recentReadings.map { it.angleDegrees }.average().toFloat()
        } else null
    }
    
    /**
     * Detect specific motion patterns (e.g., walking)
     */
    fun detectWalkingPattern(): Boolean {
        if (motionHistory.size < 10) return false
        
        // Walking has a characteristic 1-2 Hz oscillation pattern
        val recentReadings = motionHistory.takeLast(20)
        
        var crossings = 0
        for (i in 1 until recentReadings.size) {
            val prev = recentReadings[i-1].motionMagnitude
            val curr = recentReadings[i].motionMagnitude
            val threshold = 0.3f
            
            if ((prev < threshold && curr >= threshold) ||
                (prev >= threshold && curr < threshold)) {
                crossings++
            }
        }
        
        // Walking typically has 2-4 crossings per second
        val timeSpan = (recentReadings.last().timestamp - recentReadings.first().timestamp) / 1000f
        val crossingsPerSecond = if (timeSpan > 0) crossings / timeSpan else 0f
        
        return crossingsPerSecond in 1.5f..4f
    }
    
    /**
     * Stop camera motion detection
     */
    fun stop() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
        cameraExecutor?.shutdown()
        cameraExecutor = null
        previousFrame = null
    }
    
    /**
     * Clear motion history
     */
    fun clearHistory() {
        motionHistory.clear()
    }
    
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
    
    companion object {
        private const val MAX_HISTORY_SIZE = 100
        private const val MOTION_THRESHOLD = 2000
        private const val MAX_MOTION = 10f
    }
}

/**
 * Motion vector from optical flow
 */
private data class MotionVector(
    val x: Int,
    val y: Int,
    val dx: Int,
    val dy: Int,
    val sad: Int
)
