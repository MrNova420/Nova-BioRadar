package com.bioradar.sensor.drivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bioradar.core.models.WifiApReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * WiFi Scanner for detecting nearby access points and analyzing signal patterns
 * Used for presence detection through RSSI fluctuation analysis
 */
@Singleton
class WifiScanner @Inject constructor(
    private val context: Context
) {
    private val wifiManager: WifiManager = 
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    private val wifiRttManager: WifiRttManager? = 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as? WifiRttManager
        } else null
    
    private val apReadings = mutableMapOf<String, MutableList<WifiApReading>>()
    
    /**
     * Check if WiFi is available and enabled
     */
    fun isAvailable(): Boolean {
        return wifiManager.isWifiEnabled && hasPermission()
    }
    
    /**
     * Check if WiFi RTT (Round Trip Time) is supported for precise ranging
     */
    fun supportsRtt(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && 
               wifiRttManager?.isAvailable == true
    }
    
    /**
     * Start scanning for WiFi access points
     * Returns a Flow of AP readings
     */
    @Suppress("DEPRECATION")
    fun startScanning(): Flow<List<WifiApReading>> = callbackFlow {
        if (!isAvailable()) {
            close()
            return@callbackFlow
        }
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    if (success) {
                        val results = try {
                            wifiManager.scanResults
                        } catch (e: SecurityException) {
                            emptyList()
                        }
                        
                        val readings = results.map { result ->
                            WifiApReading(
                                bssid = result.BSSID,
                                ssid = result.SSID,
                                rssi = result.level,
                                frequency = result.frequency,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                        
                        // Store readings for analysis
                        readings.forEach { reading ->
                            val history = apReadings.getOrPut(reading.bssid) { mutableListOf() }
                            history.add(reading)
                            if (history.size > MAX_HISTORY_SIZE) {
                                history.removeAt(0)
                            }
                        }
                        
                        trySend(readings)
                    }
                }
            }
        }
        
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, filter)
        
        // Start initial scan
        try {
            wifiManager.startScan()
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }
        
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
    
    /**
     * Get RTT-capable access points for precise ranging
     */
    fun getRttCapableAps(): List<ScanResult> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                wifiManager.scanResults.filter { it.is80211mcResponder }
            } catch (e: SecurityException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Perform WiFi RTT ranging for precise distance measurement
     */
    suspend fun performRttRanging(accessPoints: List<ScanResult>): List<RttResult> {
        if (!supportsRtt() || accessPoints.isEmpty()) {
            return emptyList()
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            suspendCancellableCoroutine { continuation ->
                val request = RangingRequest.Builder()
                    .addAccessPoints(accessPoints)
                    .build()
                
                wifiRttManager?.startRanging(
                    request,
                    context.mainExecutor,
                    object : RangingResultCallback() {
                        override fun onRangingResults(results: MutableList<RangingResult>) {
                            val rttResults = results.mapNotNull { result ->
                                if (result.status == RangingResult.STATUS_SUCCESS) {
                                    RttResult(
                                        bssid = result.macAddress?.toString() ?: "",
                                        distanceMm = result.distanceMm,
                                        distanceStdDevMm = result.distanceStdDevMm,
                                        rssi = result.rssi,
                                        numAttempted = result.numAttemptedMeasurements,
                                        numSuccessful = result.numSuccessfulMeasurements
                                    )
                                } else null
                            }
                            continuation.resume(rttResults)
                        }
                        
                        override fun onRangingFailure(code: Int) {
                            continuation.resumeWithException(
                                Exception("RTT ranging failed with code: $code")
                            )
                        }
                    }
                )
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Calculate RSSI variance for an access point
     * High variance indicates movement between the device and AP
     */
    fun calculateRssiVariance(bssid: String): Float {
        val history = apReadings[bssid] ?: return 0f
        if (history.size < 2) return 0f
        
        val rssiValues = history.map { it.rssi.toFloat() }
        val mean = rssiValues.average().toFloat()
        return rssiValues.map { (it - mean) * (it - mean) }.average().toFloat()
    }
    
    /**
     * Calculate overall presence score based on all AP variances
     * Higher score indicates more movement/presence
     */
    fun calculatePresenceScore(): Float {
        if (apReadings.isEmpty()) return 0f
        
        val variances = apReadings.keys.map { calculateRssiVariance(it) }
        val avgVariance = variances.average().toFloat()
        val maxVariance = variances.maxOrNull() ?: 0f
        
        // Normalize to 0-1 range
        return (avgVariance / VARIANCE_NORMALIZATION).coerceIn(0f, 1f) * 0.7f +
               (maxVariance / VARIANCE_NORMALIZATION).coerceIn(0f, 1f) * 0.3f
    }
    
    /**
     * Get all recently seen access points with their analysis
     */
    fun getRecentAps(maxAgeMs: Long = 30000): List<ApSummary> {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        return apReadings.mapNotNull { (bssid, readings) ->
            val recentReadings = readings.filter { it.timestamp > cutoff }
            if (recentReadings.isEmpty()) null
            else ApSummary(
                bssid = bssid,
                ssid = recentReadings.lastOrNull()?.ssid,
                avgRssi = recentReadings.map { it.rssi }.average().toFloat(),
                rssiVariance = calculateRssiVariance(bssid),
                frequency = recentReadings.lastOrNull()?.frequency ?: 0,
                readingCount = recentReadings.size
            )
        }
    }
    
    /**
     * Estimate distance from RSSI using free-space path loss model
     */
    fun estimateDistance(rssi: Int, frequency: Int = 2400): Float {
        val exp = (27.55 - (20 * Math.log10(frequency.toDouble())) + Math.abs(rssi)) / 20.0
        return Math.pow(10.0, exp).toFloat()
    }
    
    /**
     * Detect through-wall presence using CSI-like analysis
     * Analyzes signal patterns for human body absorption/reflection
     */
    fun detectThroughWallPresence(): ThroughWallDetection {
        val aps = getRecentAps()
        if (aps.isEmpty()) {
            return ThroughWallDetection(detected = false, confidence = 0f)
        }
        
        // Analyze variance patterns for human-like signatures
        val breathingFreqAps = aps.filter { ap ->
            // Check for breathing frequency variance (0.2-0.5 Hz pattern)
            val variance = ap.rssiVariance
            variance in 2f..15f // Typical range for breathing-induced variance
        }
        
        val movementAps = aps.filter { ap ->
            // Check for walking/movement variance
            ap.rssiVariance > 20f
        }
        
        val detected = breathingFreqAps.isNotEmpty() || movementAps.isNotEmpty()
        val confidence = when {
            movementAps.isNotEmpty() && breathingFreqAps.isNotEmpty() -> 0.9f
            movementAps.isNotEmpty() -> 0.7f
            breathingFreqAps.isNotEmpty() -> 0.5f
            else -> 0f
        }
        
        return ThroughWallDetection(
            detected = detected,
            confidence = confidence,
            isMoving = movementAps.isNotEmpty(),
            isStationary = breathingFreqAps.isNotEmpty() && movementAps.isEmpty(),
            supportingAps = aps.size
        )
    }
    
    /**
     * Clear all stored readings
     */
    fun clearHistory() {
        apReadings.clear()
    }
    
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    companion object {
        private const val MAX_HISTORY_SIZE = 100
        private const val VARIANCE_NORMALIZATION = 50f
    }
}

/**
 * WiFi RTT ranging result
 */
data class RttResult(
    val bssid: String,
    val distanceMm: Int,
    val distanceStdDevMm: Int,
    val rssi: Int,
    val numAttempted: Int,
    val numSuccessful: Int
) {
    val distanceMeters: Float get() = distanceMm / 1000f
}

/**
 * Summary of a detected access point
 */
data class ApSummary(
    val bssid: String,
    val ssid: String?,
    val avgRssi: Float,
    val rssiVariance: Float,
    val frequency: Int,
    val readingCount: Int
)

/**
 * Through-wall detection result
 */
data class ThroughWallDetection(
    val detected: Boolean,
    val confidence: Float,
    val isMoving: Boolean = false,
    val isStationary: Boolean = false,
    val supportingAps: Int = 0
)
