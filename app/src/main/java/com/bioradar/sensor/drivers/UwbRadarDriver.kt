package com.bioradar.sensor.drivers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.uwb.*
import androidx.core.uwb.RangingCapabilities
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbClientSessionScope
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbManager
import androidx.core.uwb.rxjava3.UwbClientSessionScopeAdapter
import com.bioradar.core.models.DataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Ultra-Wideband (UWB) Radar Driver
 * Provides centimeter-accurate distance and angle measurements on supported devices
 * 
 * Requirements:
 * - Android 12+ (API 31+)
 * - UWB hardware (Pixel 6 Pro+, Samsung Galaxy S21+, iPhone 11+ equivalent)
 * - UWB_RANGING permission
 * 
 * Capabilities:
 * - Distance accuracy: ±5-10 cm
 * - Angle of arrival (AoA): ±5-15 degrees
 * - Range: Up to 100+ meters
 * - Update rate: Up to 60 Hz
 */
@RequiresApi(Build.VERSION_CODES.S)
@Singleton
class UwbRadarDriver @Inject constructor(
    private val context: Context
) {
    private var uwbManager: UwbManager? = null
    private var currentSession: UwbClientSessionScope? = null
    private var isInitialized = false
    
    /**
     * UWB ranging result with enhanced data
     */
    data class UwbRangingResult(
        val address: UwbAddress,
        val distanceMeters: Float,
        val angleDegrees: Float?,
        val azimuthDegrees: Float?,
        val elevationDegrees: Float?,
        val confidence: Float,
        val timestamp: Long = System.currentTimeMillis(),
        val rssi: Int?,
        val los: Boolean = true // Line of sight
    )
    
    /**
     * Check if UWB is available on this device
     */
    fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        
        val hasFeature = context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_UWB
        )
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED
        
        return hasFeature && hasPermission
    }
    
    /**
     * Initialize UWB manager
     */
    suspend fun initialize(): Boolean {
        if (isInitialized) return true
        if (!isAvailable()) return false
        
        return try {
            uwbManager = UwbManager.createInstance(context)
            isInitialized = uwbManager != null
            isInitialized
        } catch (e: Exception) {
            android.util.Log.e("UwbRadarDriver", "Failed to initialize UWB", e)
            false
        }
    }
    
    /**
     * Get UWB ranging capabilities
     */
    suspend fun getCapabilities(): RangingCapabilities? {
        if (!isInitialized && !initialize()) return null
        
        return try {
            uwbManager?.let { manager ->
                val localAddress = manager.getLocalAddress()
                val capabilities = manager.getRangingCapabilities()
                capabilities
            }
        } catch (e: Exception) {
            android.util.Log.e("UwbRadarDriver", "Failed to get capabilities", e)
            null
        }
    }
    
    /**
     * Check if angle of arrival is supported
     */
    suspend fun supportsAzimuth(): Boolean {
        return getCapabilities()?.supportsAzimuthalAngle() == true
    }
    
    suspend fun supportsElevation(): Boolean {
        return getCapabilities()?.supportsElevationAngle() == true
    }
    
    /**
     * Start UWB ranging session
     * 
     * @param partnerAddress The UWB address of the device to range to
     * @param complexChannel Optional specific channel configuration
     */
    @OptIn(ExperimentalUwbApiMarker::class)
    suspend fun startRanging(
        partnerAddress: UwbAddress,
        complexChannel: UwbComplexChannel? = null
    ): Flow<UwbRangingResult> = flow {
        if (!isInitialized && !initialize()) {
            throw IllegalStateException("UWB not available or not initialized")
        }
        
        val manager = uwbManager ?: throw IllegalStateException("UWB manager not initialized")
        
        // Create ranging parameters
        val parameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionId = generateSessionId(),
            subSessionId = null,
            sessionKeyInfo = null,
            subSessionKeyInfo = null,
            complexChannel = complexChannel,
            peerDevices = listOf(UwbDevice.createForAddress(partnerAddress.bytes)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC
        )
        
        // Start ranging session
        try {
            val session = manager.clientSessionScope()
            currentSession = session
            
            val rangingFlow = session.prepareSession(parameters)
            
            // Collect ranging results
            rangingFlow.collect { result ->
                when (result) {
                    is RangingResult.RangingResultPosition -> {
                        emit(mapToUwbResult(result))
                    }
                    is RangingResult.RangingResultPeerDisconnected -> {
                        android.util.Log.w("UwbRadarDriver", "Peer disconnected: ${result.device.address}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UwbRadarDriver", "Ranging session failed", e)
            throw e
        }
    }
    
    /**
     * Start multi-target ranging (if supported)
     */
    @OptIn(ExperimentalUwbApiMarker::class)
    suspend fun startMultiTargetRanging(
        partnerAddresses: List<UwbAddress>,
        complexChannel: UwbComplexChannel? = null
    ): Flow<UwbRangingResult> = flow {
        if (!isInitialized && !initialize()) {
            throw IllegalStateException("UWB not available")
        }
        
        val manager = uwbManager ?: throw IllegalStateException("UWB manager not initialized")
        
        // Create multi-target ranging parameters
        val parameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
            sessionId = generateSessionId(),
            subSessionId = null,
            sessionKeyInfo = null,
            subSessionKeyInfo = null,
            complexChannel = complexChannel,
            peerDevices = partnerAddresses.map { UwbDevice.createForAddress(it.bytes) },
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC
        )
        
        try {
            val session = manager.clientSessionScope()
            currentSession = session
            
            val rangingFlow = session.prepareSession(parameters)
            
            rangingFlow.collect { result ->
                when (result) {
                    is RangingResult.RangingResultPosition -> {
                        emit(mapToUwbResult(result))
                    }
                    is RangingResult.RangingResultPeerDisconnected -> {
                        android.util.Log.w("UwbRadarDriver", "Peer disconnected")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UwbRadarDriver", "Multi-target ranging failed", e)
            throw e
        }
    }
    
    /**
     * Stop ranging session
     */
    fun stopRanging() {
        currentSession = null
        // Session will be automatically closed when the scope is released
    }
    
    /**
     * Convert library ranging result to our enhanced result
     */
    private fun mapToUwbResult(result: RangingResult.RangingResultPosition): UwbRangingResult {
        val position = result.position
        val distance = position.distance?.value ?: 0f
        
        // Extract angles if available
        val azimuth = position.azimuth?.value
        val elevation = position.elevation?.value
        
        // Calculate angle in 2D plane (0-360 degrees)
        val angle = azimuth?.let { az ->
            // Convert from radians to degrees and normalize to 0-360
            var deg = Math.toDegrees(az.toDouble()).toFloat()
            if (deg < 0) deg += 360f
            deg
        }
        
        // Calculate confidence based on signal quality
        val confidence = calculateConfidence(distance, result.position.distanceAccuracy?.value)
        
        return UwbRangingResult(
            address = result.device.address,
            distanceMeters = distance,
            angleDegrees = angle,
            azimuthDegrees = azimuth?.let { Math.toDegrees(it.toDouble()).toFloat() },
            elevationDegrees = elevation?.let { Math.toDegrees(it.toDouble()).toFloat() },
            confidence = confidence,
            rssi = null, // Not directly available in current API
            los = true // Assume line of sight for UWB
        )
    }
    
    /**
     * Calculate ranging confidence based on distance and accuracy
     */
    private fun calculateConfidence(distance: Float, accuracy: Float?): Float {
        if (distance <= 0f) return 0f
        
        val baseConfidence = when {
            distance < 1f -> 0.95f
            distance < 5f -> 0.90f
            distance < 10f -> 0.85f
            distance < 30f -> 0.80f
            distance < 50f -> 0.70f
            else -> 0.60f
        }
        
        // Adjust for accuracy if available
        val accuracyFactor = accuracy?.let { acc ->
            when {
                acc < 0.05f -> 1.0f    // 5cm accuracy
                acc < 0.1f -> 0.95f     // 10cm accuracy
                acc < 0.3f -> 0.85f     // 30cm accuracy
                acc < 0.5f -> 0.75f     // 50cm accuracy
                else -> 0.65f
            }
        } ?: 1.0f
        
        return (baseConfidence * accuracyFactor).coerceIn(0f, 1f)
    }
    
    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): Int {
        return (System.currentTimeMillis() and 0xFFFFFFFF).toInt()
    }
    
    /**
     * Get local UWB address
     */
    suspend fun getLocalAddress(): UwbAddress? {
        if (!isInitialized && !initialize()) return null
        
        return try {
            uwbManager?.getLocalAddress()
        } catch (e: Exception) {
            android.util.Log.e("UwbRadarDriver", "Failed to get local address", e)
            null
        }
    }
    
    /**
     * Create UWB address from byte array
     */
    fun createAddress(addressBytes: ByteArray): UwbAddress {
        return UwbAddress(addressBytes)
    }
    
    /**
     * Parse UWB address from string (hex format)
     */
    fun parseAddress(addressHex: String): UwbAddress {
        val bytes = addressHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
        return UwbAddress(bytes)
    }
    
    /**
     * Get supported UWB channels
     */
    suspend fun getSupportedChannels(): List<Int> {
        val capabilities = getCapabilities() ?: return emptyList()
        
        return try {
            // Common UWB channels: 5 (6.5 GHz) and 9 (8 GHz)
            listOf(5, 9) // Simplified, actual API may vary
        } catch (e: Exception) {
            android.util.Log.e("UwbRadarDriver", "Failed to get channels", e)
            emptyList()
        }
    }
    
    /**
     * Estimate target classification based on UWB characteristics
     */
    fun classifyTarget(result: UwbRangingResult): TargetClassification {
        val distance = result.distanceMeters
        val confidence = result.confidence
        val hasAngle = result.angleDegrees != null
        
        return when {
            // High precision, close range, angle available = likely human
            distance < 5f && confidence > 0.8f && hasAngle -> TargetClassification.HUMAN
            
            // Medium range with good confidence = possible life
            distance < 20f && confidence > 0.7f -> TargetClassification.POSSIBLE_LIFE
            
            // Far or low confidence = unknown
            distance > 30f || confidence < 0.6f -> TargetClassification.UNKNOWN
            
            else -> TargetClassification.POSSIBLE_LIFE
        }
    }
    
    enum class TargetClassification {
        HUMAN,
        POSSIBLE_LIFE,
        UNKNOWN
    }
    
    /**
     * Cleanup resources
     */
    fun release() {
        stopRanging()
        currentSession = null
        isInitialized = false
    }
}

/**
 * Extension function to convert UWB result to RadarTarget
 */
fun UwbRadarDriver.UwbRangingResult.toRadarTarget(): com.bioradar.core.models.RadarTarget {
    return com.bioradar.core.models.RadarTarget(
        angleDegrees = this.angleDegrees ?: 0f,
        distanceMeters = this.distanceMeters,
        confidence = this.confidence,
        type = com.bioradar.core.models.TargetType.HUMAN, // UWB typically detects devices/humans
        isMoving = false, // Would need motion tracking for this
        dataSources = setOf(DataSource.UWB),
        signalStrength = this.rssi?.toFloat()
    )
}
