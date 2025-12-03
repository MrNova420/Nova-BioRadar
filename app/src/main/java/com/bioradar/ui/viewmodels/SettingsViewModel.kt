package com.bioradar.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Settings data class
 */
data class AppSettings(
    // Radar
    val maxRange: Float = 10f,
    val sweepSpeed: Float = 3f,
    val showGridLines: Boolean = true,
    
    // Sensors
    val wifiEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = true,
    val sonarEnabled: Boolean = true,
    val sonarFrequency: Float = 18000f,
    val cameraEnabled: Boolean = true,
    
    // Alerts
    val soundAlertsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val screenFlashEnabled: Boolean = true,
    
    // Battery
    val autoDowngrade: Boolean = true,
    val downgradeThreshold: Int = 20,
    
    // Security
    val encryptLogs: Boolean = false,
    val panicWipeEnabled: Boolean = false
)

/**
 * ViewModel for the Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    // Radar settings
    fun setMaxRange(value: Float) {
        _settings.value = _settings.value.copy(maxRange = value)
    }
    
    fun setSweepSpeed(value: Float) {
        _settings.value = _settings.value.copy(sweepSpeed = value)
    }
    
    fun setShowGridLines(value: Boolean) {
        _settings.value = _settings.value.copy(showGridLines = value)
    }
    
    // Sensor settings
    fun setWifiEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(wifiEnabled = value)
    }
    
    fun setBluetoothEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(bluetoothEnabled = value)
    }
    
    fun setSonarEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(sonarEnabled = value)
    }
    
    fun setSonarFrequency(value: Float) {
        _settings.value = _settings.value.copy(sonarFrequency = value)
    }
    
    fun setCameraEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(cameraEnabled = value)
    }
    
    // Alert settings
    fun setSoundAlertsEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(soundAlertsEnabled = value)
    }
    
    fun setVibrationEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(vibrationEnabled = value)
    }
    
    fun setScreenFlashEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(screenFlashEnabled = value)
    }
    
    // Battery settings
    fun setAutoDowngrade(value: Boolean) {
        _settings.value = _settings.value.copy(autoDowngrade = value)
    }
    
    fun setDowngradeThreshold(value: Int) {
        _settings.value = _settings.value.copy(downgradeThreshold = value)
    }
    
    // Security settings
    fun setEncryptLogs(value: Boolean) {
        _settings.value = _settings.value.copy(encryptLogs = value)
    }
    
    fun setPanicWipeEnabled(value: Boolean) {
        _settings.value = _settings.value.copy(panicWipeEnabled = value)
    }
    
    /**
     * Execute panic wipe - delete all data
     */
    fun executePanicWipe() {
        // In production, this would:
        // 1. Overwrite all log files with random data
        // 2. Delete all logs from database
        // 3. Clear encrypted shared preferences
        // 4. Reset all settings
        _settings.value = AppSettings()
    }
}
