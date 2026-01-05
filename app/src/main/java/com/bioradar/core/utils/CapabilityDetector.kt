package com.bioradar.core.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.util.Size

/**
 * Device capability profile containing all hardware features
 */
data class DeviceCapabilities(
    // Basic sensors
    val hasWifi: Boolean,
    val hasBluetooth: Boolean,
    val hasBle: Boolean,
    val hasCamera: Boolean,
    val hasMicrophone: Boolean,
    val hasAccelerometer: Boolean,
    val hasGyroscope: Boolean,
    val hasMagnetometer: Boolean,
    
    // Advanced capabilities
    val hasUwb: Boolean,
    val hasWifiDirect: Boolean,
    val hasWifiAware: Boolean,
    val hasWifiRtt: Boolean,
    val hasBluetooth5: Boolean,
    val hasBleCodedPhy: Boolean,
    val hasBleDirectionFinding: Boolean,
    
    // Audio capabilities
    val hasUltrasonicSupport: Boolean,
    val hasMultiMicrophone: Boolean,
    val hasNoiseSuppression: Boolean,
    
    // Camera capabilities
    val cameraResolution: Size?,
    val hasCamera60fps: Boolean,
    val hasInfraredCamera: Boolean,
    val hasDepthCamera: Boolean,
    
    // Processing power
    val cpuCores: Int,
    val ramMb: Int,
    val hasGpuAcceleration: Boolean,
    val hasNnApi: Boolean,
    
    // Android version
    val androidVersion: Int,
    val batteryCapacity: Int,
    val hasBackgroundLocationAccess: Boolean
) {
    /**
     * Calculate device tier based on capabilities
     */
    val tier: DeviceTier get() = when {
        hasBleDirectionFinding && hasInfraredCamera -> DeviceTier.TIER_3_ADVANCED
        hasUwb || hasWifiRtt -> DeviceTier.TIER_2_UWB
        hasWifi && hasBluetooth && hasMicrophone && hasCamera -> DeviceTier.TIER_1_STANDARD
        else -> DeviceTier.TIER_0_BASIC
    }
    
    /**
     * Get list of available data sources
     */
    val availableDataSources: Set<com.bioradar.core.models.DataSource> get() {
        val sources = mutableSetOf<com.bioradar.core.models.DataSource>()
        if (hasWifi) sources.add(com.bioradar.core.models.DataSource.WIFI)
        if (hasBluetooth || hasBle) sources.add(com.bioradar.core.models.DataSource.BLUETOOTH)
        if (hasMicrophone) sources.add(com.bioradar.core.models.DataSource.SONAR)
        if (hasCamera) sources.add(com.bioradar.core.models.DataSource.CAMERA)
        if (hasUwb) sources.add(com.bioradar.core.models.DataSource.UWB)
        if (hasAccelerometer) sources.add(com.bioradar.core.models.DataSource.ACCELEROMETER)
        if (hasMagnetometer) sources.add(com.bioradar.core.models.DataSource.MAGNETOMETER)
        return sources
    }
    
    /**
     * Estimate maximum detection range based on capabilities
     */
    val estimatedMaxRange: Float get() = when {
        hasUwb -> 50f
        hasWifiRtt -> 20f
        hasBleCodedPhy -> 30f
        hasWifi && hasBluetooth -> 15f
        else -> 5f
    }
    
    /**
     * Estimate through-wall detection range
     */
    val throughWallRange: Float get() = when {
        hasUwb -> 10f
        hasWifiRtt -> 15f
        hasWifi -> 8f
        else -> 0f
    }
}

/**
 * Device tier classification
 */
enum class DeviceTier {
    TIER_0_BASIC,      // Minimal features
    TIER_1_STANDARD,   // All basic sensors
    TIER_2_UWB,        // UWB + advanced features
    TIER_3_ADVANCED    // All features including direction finding
}

/**
 * Capability detector - runs at app startup to detect device hardware
 */
class CapabilityDetector(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    /**
     * Detect all device capabilities
     */
    fun detectCapabilities(): DeviceCapabilities {
        return DeviceCapabilities(
            // Basic sensors
            hasWifi = hasSystemFeature(PackageManager.FEATURE_WIFI),
            hasBluetooth = hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            hasBle = hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE),
            hasCamera = hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
            hasMicrophone = hasSystemFeature(PackageManager.FEATURE_MICROPHONE),
            hasAccelerometer = hasSensor(Sensor.TYPE_ACCELEROMETER),
            hasGyroscope = hasSensor(Sensor.TYPE_GYROSCOPE),
            hasMagnetometer = hasSensor(Sensor.TYPE_MAGNETIC_FIELD),
            
            // Advanced capabilities
            hasUwb = checkUwbSupport(),
            hasWifiDirect = hasSystemFeature("android.hardware.wifi.direct"),
            hasWifiAware = checkWifiAwareSupport(),
            hasWifiRtt = checkWifiRttSupport(),
            hasBluetooth5 = checkBluetooth5Support(),
            hasBleCodedPhy = checkBleCodedPhySupport(),
            hasBleDirectionFinding = checkBleDirectionFindingSupport(),
            
            // Audio capabilities
            hasUltrasonicSupport = checkUltrasonicSupport(),
            hasMultiMicrophone = checkMultiMicrophoneSupport(),
            hasNoiseSuppression = checkNoiseSuppressionSupport(),
            
            // Camera capabilities
            cameraResolution = getCameraMaxResolution(),
            hasCamera60fps = check60fpsSupport(),
            hasInfraredCamera = checkInfraredCamera(),
            hasDepthCamera = checkDepthCamera(),
            
            // Processing power
            cpuCores = Runtime.getRuntime().availableProcessors(),
            ramMb = getAvailableRam(),
            hasGpuAcceleration = checkGpuAcceleration(),
            hasNnApi = checkNnApiSupport(),
            
            // Android version
            androidVersion = Build.VERSION.SDK_INT,
            batteryCapacity = getBatteryCapacity(),
            hasBackgroundLocationAccess = checkBackgroundLocationAccess()
        )
    }
    
    private fun hasSystemFeature(feature: String): Boolean {
        return packageManager.hasSystemFeature(feature)
    }
    
    private fun hasSensor(type: Int): Boolean {
        return sensorManager.getDefaultSensor(type) != null
    }
    
    private fun checkUwbSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                hasSystemFeature("android.hardware.uwb")
            } catch (e: Exception) {
                false
            }
        } else false
    }
    
    private fun checkWifiAwareSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hasSystemFeature("android.hardware.wifi.aware")
        } else false
    }
    
    private fun checkWifiRttSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            hasSystemFeature("android.hardware.wifi.rtt")
        } else false
    }
    
    private fun checkBluetooth5Support(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter?.isLe2MPhySupported == true
            } catch (e: Exception) {
                false
            }
        } else false
    }
    
    private fun checkBleCodedPhySupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter?.isLeCodedPhySupported == true
            } catch (e: Exception) {
                false
            }
        } else false
    }
    
    private fun checkBleDirectionFindingSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter?.isLePeriodicAdvertisingSupported == true
            } catch (e: Exception) {
                false
            }
        } else false
    }
    
    private fun checkUltrasonicSupport(): Boolean {
        // Most modern phones support at least 20kHz audio
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
    
    private fun checkMultiMicrophoneSupport(): Boolean {
        // Check for multiple audio sources
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.getProperty(android.media.AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) != null
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkNoiseSuppressionSupport(): Boolean {
        return android.media.audiofx.NoiseSuppressor.isAvailable()
    }
    
    private fun getCameraMaxResolution(): Size? {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return null
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val configs = characteristics.get(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = configs?.getOutputSizes(android.graphics.ImageFormat.JPEG)
            sizes?.maxByOrNull { it.width * it.height }?.let { Size(it.width, it.height) }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun check60fpsSupport(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return false
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val fpsRanges = characteristics.get(android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            fpsRanges?.any { it.upper >= 60 } == true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkInfraredCamera(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            cameraManager.cameraIdList.any { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val capabilities = characteristics.get(android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                capabilities?.contains(android.hardware.camera2.CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME) == true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkDepthCamera(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                cameraManager.cameraIdList.any { cameraId ->
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val capabilities = characteristics.get(android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                    capabilities?.contains(android.hardware.camera2.CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) == true
                }
            } else false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getAvailableRam(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.totalMem / (1024 * 1024)).toInt()
    }
    
    private fun checkGpuAcceleration(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val configInfo = activityManager.deviceConfigurationInfo
            configInfo.reqGlEsVersion >= 0x30000 // OpenGL ES 3.0+
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkNnApiSupport(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }
    
    private fun getBatteryCapacity(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1
        }
    }
    
    private fun checkBackgroundLocationAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == 
                PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == 
                PackageManager.PERMISSION_GRANTED
        }
    }
}
