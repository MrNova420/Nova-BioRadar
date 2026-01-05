package com.bioradar.sensor.drivers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SoftApConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Self-Generated WiFi System
 * 
 * Creates WiFi signals when no infrastructure exists (blackout scenarios).
 * The device becomes a WiFi Access Point or WiFi Direct group owner, generating
 * RF signals that can be used for RF shadow detection by other devices.
 * 
 * Key Capabilities:
 * - WiFi Hotspot creation (2.4GHz/5GHz)
 * - WiFi Direct group owner
 * - RF shadow mapping detection
 * - Range: 50-200+ meters
 * - No internet or infrastructure required
 * 
 * Use Cases:
 * - Complete blackout scenarios (no existing WiFi)
 * - Emergency/disaster response
 * - Off-grid security monitoring
 * - Perimeter defense without infrastructure
 */
@Singleton
class SelfGeneratedWiFiSystem @Inject constructor(
    private val context: Context
) {
    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private val wifiP2pManager: WifiP2pManager? by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    }
    
    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null
    private var wifiP2pChannel: WifiP2pManager.Channel? = null
    private var isHotspotActive = false
    private var isWifiDirectActive = false
    
    /**
     * Hotspot creation result
     */
    data class HotspotResult(
        val success: Boolean,
        val ssid: String = "",
        val passphrase: String = "",
        val frequency: Int = 0,
        val channel: Int = 0,
        val band: Int = 0,
        val powerDbm: Int = 20, // Typical hotspot power
        val method: String
    )
    
    /**
     * WiFi Direct group result
     */
    data class WiFiDirectResult(
        val success: Boolean,
        val networkName: String = "",
        val passphrase: String = "",
        val frequency: Int = 0,
        val isGroupOwner: Boolean = false,
        val method: String,
        val expectedRange: Float = 100f
    )
    
    /**
     * RF shadow detection result
     */
    data class RFShadowDetection(
        val ssid: String,
        val bssid: String,
        val baseRSSI: Float,
        val currentRSSI: Float,
        val variance: Float,
        val absorption: Float,
        val shadowDetected: Boolean,
        val estimatedDistance: Float?,
        val confidence: Float,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Broadcast status for continuous RF generation
     */
    data class BroadcastStatus(
        val active: Boolean,
        val mode: String,
        val channels: List<Int>,
        val powerDbm: Int,
        val range: String,
        val connectionRequired: Boolean
    )
    
    /**
     * Check if self-generated WiFi is supported
     */
    fun isSupported(): Boolean {
        val hasWiFi = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)
        val hasPermissions = hasRequiredPermissions()
        
        return hasWiFi && hasPermissions && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
    
    private fun hasRequiredPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Method 1: WiFi Hotspot Creation
     * Device becomes an Access Point, generates 2.4GHz/5GHz RF signals
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(allOf = [
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    suspend fun createWiFiHotspot(
        ssid: String = "NovaBioRadar-${Random.nextInt(1000, 9999)}",
        passphrase: String = generateSecurePassphrase(),
        band: Int = SoftApConfiguration.BAND_2GHZ // 2.4GHz for maximum range
    ): HotspotResult {
        if (!isSupported()) {
            return HotspotResult(false, method = "NOT_SUPPORTED")
        }
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                createHotspotModern(ssid, passphrase, band)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8-10 (API 26-29)
                createHotspotLegacy()
            } else {
                HotspotResult(false, method = "VERSION_TOO_OLD")
            }
        } catch (e: Exception) {
            android.util.Log.e("SelfGeneratedWiFi", "Hotspot creation failed", e)
            HotspotResult(false, method = "ERROR: ${e.message}")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun createHotspotModern(
        ssid: String,
        passphrase: String,
        band: Int
    ): HotspotResult {
        return try {
            val config = SoftApConfiguration.Builder()
                .setSsid(ssid)
                .setPassphrase(passphrase, SoftApConfiguration.SECURITY_TYPE_WPA2_PSK)
                .setBand(band)
                .setMaxNumberOfClients(8) // Support multiple detector devices
                .setAutoShutdownEnabled(false) // Keep running
                .build()
            
            var result: HotspotResult? = null
            
            wifiManager.startLocalOnlyHotspot(config, context.mainExecutor,
                object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                        hotspotReservation = reservation
                        isHotspotActive = true
                        
                        val apConfig = reservation.softApConfiguration
                        result = HotspotResult(
                            success = true,
                            ssid = apConfig.ssid,
                            passphrase = apConfig.passphrase ?: "",
                            frequency = calculateFrequency(apConfig.channel, apConfig.band),
                            channel = apConfig.channel,
                            band = apConfig.band,
                            powerDbm = 20, // Typical max power
                            method = "LOCAL_ONLY_HOTSPOT_MODERN"
                        )
                        
                        android.util.Log.i("SelfGeneratedWiFi", "Hotspot started: $ssid")
                    }
                    
                    override fun onFailed(reason: Int) {
                        result = HotspotResult(false, method = "FAILED_$reason")
                        android.util.Log.e("SelfGeneratedWiFi", "Hotspot failed: $reason")
                    }
                }
            )
            
            // Wait for callback
            var attempts = 0
            while (result == null && attempts < 50) {
                delay(100)
                attempts++
            }
            
            result ?: HotspotResult(false, method = "TIMEOUT")
        } catch (e: Exception) {
            HotspotResult(false, method = "EXCEPTION: ${e.message}")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createHotspotLegacy(): HotspotResult {
        return try {
            var result: HotspotResult? = null
            
            wifiManager.startLocalOnlyHotspot(
                object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                        hotspotReservation = reservation
                        isHotspotActive = true
                        
                        val config = reservation.wifiConfiguration
                        result = HotspotResult(
                            success = true,
                            ssid = config?.SSID ?: "Unknown",
                            passphrase = config?.preSharedKey ?: "",
                            frequency = 2437, // Channel 6 default
                            channel = 6,
                            band = SoftApConfiguration.BAND_2GHZ,
                            method = "LOCAL_ONLY_HOTSPOT_LEGACY"
                        )
                    }
                    
                    override fun onFailed(reason: Int) {
                        result = HotspotResult(false, method = "FAILED_$reason")
                    }
                },
                null
            )
            
            // Wait for callback
            var attempts = 0
            while (result == null && attempts < 50) {
                delay(100)
                attempts++
            }
            
            result ?: HotspotResult(false, method = "TIMEOUT")
        } catch (e: Exception) {
            HotspotResult(false, method = "EXCEPTION: ${e.message}")
        }
    }
    
    /**
     * Method 2: WiFi Direct Group Owner
     * Creates P2P connection with strong RF signal
     */
    @RequiresPermission(allOf = [
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    fun createWiFiDirect(): WiFiDirectResult {
        val manager = wifiP2pManager ?: return WiFiDirectResult(
            false, method = "NOT_AVAILABLE", expectedRange = 0f
        )
        
        if (wifiP2pChannel == null) {
            wifiP2pChannel = manager.initialize(context, context.mainLooper, null)
        }
        
        val channel = wifiP2pChannel ?: return WiFiDirectResult(
            false, method = "CHANNEL_FAILED", expectedRange = 0f
        )
        
        var result: WiFiDirectResult? = null
        
        // Create WiFi Direct group (device becomes group owner)
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                isWifiDirectActive = true
                
                // Request group info
                manager.requestGroupInfo(channel) { group ->
                    if (group != null) {
                        result = WiFiDirectResult(
                            success = true,
                            networkName = group.networkName,
                            passphrase = group.passphrase,
                            frequency = group.frequency,
                            isGroupOwner = group.isGroupOwner,
                            method = "WIFI_DIRECT_GROUP_OWNER",
                            expectedRange = 100f // WiFi Direct typical range
                        )
                        
                        android.util.Log.i("SelfGeneratedWiFi", 
                            "WiFi Direct group created: ${group.networkName}")
                    }
                }
            }
            
            override fun onFailure(reason: Int) {
                result = WiFiDirectResult(
                    false, 
                    method = "FAILED_$reason",
                    expectedRange = 0f
                )
            }
        })
        
        // Wait briefly for result
        Thread.sleep(2000)
        
        return result ?: WiFiDirectResult(
            false, 
            method = "TIMEOUT",
            expectedRange = 0f
        )
    }
    
    /**
     * Detect using self-generated WiFi
     * Monitor our own hotspot/direct signals for RF shadows/absorption
     */
    fun detectWithSelfGeneratedWiFi(): Flow<RFShadowDetection> = flow {
        if (!isHotspotActive && !isWifiDirectActive) {
            android.util.Log.w("SelfGeneratedWiFi", "No active self-generated WiFi")
            return@flow
        }
        
        // Scan for our own network
        val scanResults = wifiManager.scanResults
        
        // Find our generated networks
        val ourNetworks = scanResults.filter { result ->
            result.SSID.contains("NovaBioRadar") || 
            result.SSID.startsWith("DIRECT-")
        }
        
        for (network in ourNetworks) {
            // Monitor RSSI variations over time
            val rssiHistory = mutableListOf<Int>()
            repeat(20) { // 20 samples over 2 seconds
                wifiManager.startScan()
                delay(100)
                
                val current = wifiManager.scanResults
                    .find { it.BSSID == network.BSSID }
                    ?.level
                
                if (current != null) {
                    rssiHistory.add(current)
                }
            }
            
            if (rssiHistory.size >= 10) {
                val baseRSSI = rssiHistory.take(5).average().toFloat()
                val currentRSSI = rssiHistory.takeLast(5).average().toFloat()
                val variance = calculateVariance(rssiHistory.map { it.toFloat() })
                val absorption = abs(currentRSSI - baseRSSI) / max(abs(baseRSSI), 1f)
                
                // Detect shadows (significant RSSI changes)
                val shadowDetected = variance > 3f || absorption > 0.15f
                
                if (shadowDetected) {
                    emit(RFShadowDetection(
                        ssid = network.SSID,
                        bssid = network.BSSID,
                        baseRSSI = baseRSSI,
                        currentRSSI = currentRSSI,
                        variance = variance,
                        absorption = absorption,
                        shadowDetected = true,
                        estimatedDistance = estimateDistanceFromAbsorption(absorption),
                        confidence = calculateShadowConfidence(variance, absorption)
                    ))
                }
            }
        }
    }
    
    /**
     * Stop hotspot
     */
    fun stopHotspot() {
        hotspotReservation?.close()
        hotspotReservation = null
        isHotspotActive = false
    }
    
    /**
     * Stop WiFi Direct
     */
    fun stopWiFiDirect() {
        wifiP2pManager?.removeGroup(wifiP2pChannel, null)
        isWifiDirectActive = false
    }
    
    /**
     * Stop all self-generated WiFi
     */
    fun stopAll() {
        stopHotspot()
        stopWiFiDirect()
    }
    
    /**
     * Calculate channel frequency
     */
    private fun calculateFrequency(channel: Int, band: Int): Int {
        return when (band) {
            SoftApConfiguration.BAND_2GHZ -> 2407 + (channel * 5)
            SoftApConfiguration.BAND_5GHZ -> 5000 + (channel * 5)
            else -> 2437 // Default channel 6
        }
    }
    
    /**
     * Calculate variance of RSSI values
     */
    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average().toFloat()
        return values.map { (it - mean).pow(2) }.average().toFloat()
    }
    
    /**
     * Estimate distance from RF absorption
     */
    private fun estimateDistanceFromAbsorption(absorption: Float): Float {
        // Empirical formula: higher absorption = closer presence
        return when {
            absorption > 0.4f -> 2f      // Very close (< 2m)
            absorption > 0.3f -> 5f      // Close (2-5m)
            absorption > 0.2f -> 10f     // Medium (5-10m)
            absorption > 0.15f -> 15f    // Far (10-15m)
            else -> 20f                  // Very far (15-20m)
        }
    }
    
    /**
     * Calculate shadow detection confidence
     */
    private fun calculateShadowConfidence(variance: Float, absorption: Float): Float {
        val varianceScore = (variance / 10f).coerceIn(0f, 1f)
        val absorptionScore = (absorption / 0.5f).coerceIn(0f, 1f)
        
        return ((varianceScore + absorptionScore) / 2f).coerceIn(0f, 1f)
    }
    
    /**
     * Generate secure passphrase
     */
    private fun generateSecurePassphrase(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }
    
    private fun abs(value: Float) = kotlin.math.abs(value)
    private fun max(a: Float, b: Float) = kotlin.math.max(a, b)
    private fun Float.pow(n: Int) = kotlin.math.pow(this.toDouble(), n.toDouble()).toFloat()
}
