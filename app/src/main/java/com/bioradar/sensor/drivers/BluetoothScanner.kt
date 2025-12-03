package com.bioradar.sensor.drivers

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bioradar.core.models.BleDeviceReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bluetooth LE Scanner for detecting nearby Bluetooth devices
 * Used for presence detection through signal strength analysis
 */
@Singleton
class BluetoothScanner @Inject constructor(
    private val context: Context
) {
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    
    private val deviceReadings = mutableMapOf<String, MutableList<BleDeviceReading>>()
    
    /**
     * Check if Bluetooth is available and enabled
     */
    fun isAvailable(): Boolean {
        return bluetoothAdapter != null && 
               bluetoothAdapter.isEnabled && 
               hasPermission()
    }
    
    /**
     * Check if Bluetooth 5.0 Long Range (Coded PHY) is supported
     */
    fun supportsLongRange(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.isLeCodedPhySupported == true
        } else {
            false
        }
    }
    
    /**
     * Start scanning for BLE devices
     * Returns a Flow of device readings
     */
    fun startScanning(
        scanMode: Int = ScanSettings.SCAN_MODE_LOW_LATENCY
    ): Flow<BleDeviceReading> = callbackFlow {
        if (!isAvailable()) {
            close()
            return@callbackFlow
        }
        
        val settings = ScanSettings.Builder()
            .setScanMode(scanMode)
            .setReportDelay(0)
            .build()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val reading = BleDeviceReading(
                    address = result.device.address,
                    name = result.device.name,
                    rssi = result.rssi,
                    timestamp = System.currentTimeMillis()
                )
                
                // Store reading for variance analysis
                val history = deviceReadings.getOrPut(reading.address) { mutableListOf() }
                history.add(reading)
                if (history.size > MAX_HISTORY_SIZE) {
                    history.removeAt(0)
                }
                
                trySend(reading)
            }
            
            override fun onScanFailed(errorCode: Int) {
                close(Exception("BLE scan failed with error code: $errorCode"))
            }
        }
        
        try {
            scanner?.startScan(null, settings, scanCallback)
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }
        
        awaitClose {
            try {
                scanner?.stopScan(scanCallback)
            } catch (e: SecurityException) {
                // Ignore permission errors on cleanup
            }
        }
    }
    
    /**
     * Calculate RSSI variance for a device
     * High variance indicates movement
     */
    fun calculateRssiVariance(deviceAddress: String): Float {
        val history = deviceReadings[deviceAddress] ?: return 0f
        if (history.size < 2) return 0f
        
        val rssiValues = history.map { it.rssi.toFloat() }
        val mean = rssiValues.average().toFloat()
        return rssiValues.map { (it - mean) * (it - mean) }.average().toFloat()
    }
    
    /**
     * Get all recently seen devices with their average RSSI
     */
    fun getRecentDevices(maxAgeMs: Long = 10000): List<DeviceSummary> {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        return deviceReadings.mapNotNull { (address, readings) ->
            val recentReadings = readings.filter { it.timestamp > cutoff }
            if (recentReadings.isEmpty()) null
            else DeviceSummary(
                address = address,
                name = recentReadings.lastOrNull()?.name,
                avgRssi = recentReadings.map { it.rssi }.average().toFloat(),
                rssiVariance = calculateRssiVariance(address),
                readingCount = recentReadings.size
            )
        }
    }
    
    /**
     * Estimate distance from RSSI using log-distance path loss model
     */
    fun estimateDistance(rssi: Int, txPower: Int = TX_POWER_DEFAULT): Float {
        val ratio = rssi.toFloat() / txPower
        return if (ratio < 1.0) {
            Math.pow(ratio.toDouble(), 10.0).toFloat()
        } else {
            val accuracy = 0.89976f * Math.pow(ratio.toDouble(), 7.7095).toFloat() + 0.111f
            accuracy
        }
    }
    
    /**
     * Detect motion based on RSSI variance
     */
    fun detectMotion(deviceAddress: String, threshold: Float = MOTION_THRESHOLD): Boolean {
        return calculateRssiVariance(deviceAddress) > threshold
    }
    
    /**
     * Clear all stored readings
     */
    fun clearHistory() {
        deviceReadings.clear()
    }
    
    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
    
    companion object {
        private const val MAX_HISTORY_SIZE = 100
        private const val TX_POWER_DEFAULT = -59 // Default TX power at 1m
        private const val MOTION_THRESHOLD = 25f // RSSI variance threshold for motion
    }
}

/**
 * Summary of a detected device
 */
data class DeviceSummary(
    val address: String,
    val name: String?,
    val avgRssi: Float,
    val rssiVariance: Float,
    val readingCount: Int
)
