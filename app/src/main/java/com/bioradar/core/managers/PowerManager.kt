package com.bioradar.core.managers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.bioradar.core.models.OperatingMode
import com.bioradar.core.models.ModeProfiles
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages battery monitoring and power-aware mode switching
 * Provides estimates for remaining monitoring time and automatic mode downgrade
 */
@Singleton
class PowerManager @Inject constructor(
    private val context: Context
) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    
    /**
     * Get current battery level (0-100)
     */
    fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Check if device is currently charging
     */
    fun isCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    /**
     * Check if device is fully charged
     */
    fun isFullyCharged(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    /**
     * Get battery temperature in Celsius
     */
    fun getBatteryTemperature(): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f
    }
    
    /**
     * Get battery health status
     */
    fun getBatteryHealth(): BatteryHealth {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }
    }
    
    /**
     * Estimate remaining monitoring time based on current mode
     * Returns duration in hours and minutes
     */
    fun estimateRemainingTime(mode: OperatingMode): Duration {
        val batteryLevel = getBatteryLevel()
        
        // Estimated drain rate per hour (percentage)
        val drainRatePerHour = when (mode) {
            OperatingMode.NORMAL -> 15      // ~6-7 hours
            OperatingMode.EMERGENCY -> 3    // ~30+ hours
            OperatingMode.GUARD -> 12       // ~8 hours
            OperatingMode.STEALTH -> 5      // ~20 hours
            OperatingMode.SEARCH -> 25      // ~4 hours
            OperatingMode.LAB -> 20         // ~5 hours
            OperatingMode.SENTRY -> 8       // ~12 hours
        }
        
        // Adjust for charging
        val effectiveDrainRate = if (isCharging()) {
            // When charging, assume net gain or at least stable
            -5 // Gaining 5% per hour on average
        } else {
            drainRatePerHour
        }
        
        // Calculate remaining time
        val hoursRemaining = if (effectiveDrainRate <= 0) {
            // Infinite when charging
            24.0
        } else {
            batteryLevel.toFloat() / effectiveDrainRate
        }
        
        return Duration.ofMinutes((hoursRemaining * 60).toLong())
    }
    
    /**
     * Get recommended mode based on battery level and charging status
     */
    fun getRecommendedMode(): OperatingMode {
        val level = getBatteryLevel()
        val charging = isCharging()
        
        return when {
            charging -> OperatingMode.GUARD  // Full power when charging
            level > 50 -> OperatingMode.NORMAL
            level > 30 -> OperatingMode.STEALTH
            level > 15 -> OperatingMode.EMERGENCY
            else -> OperatingMode.EMERGENCY  // Force emergency below 15%
        }
    }
    
    /**
     * Check if current mode should be auto-downgraded
     * Returns the new mode if downgrade is needed, null otherwise
     */
    fun shouldAutoDowngrade(currentMode: OperatingMode): OperatingMode? {
        val level = getBatteryLevel()
        val profile = ModeProfiles.getProfile(currentMode)
        
        // Check if below threshold for current mode
        if (level < profile.batteryThreshold && !isCharging()) {
            // Find a more power-efficient mode
            return when {
                level < 10 -> OperatingMode.EMERGENCY
                level < 20 -> OperatingMode.STEALTH
                level < 30 -> OperatingMode.GUARD
                else -> null
            }
        }
        
        return null
    }
    
    /**
     * Check if mode requires charging
     */
    fun isModeAllowed(mode: OperatingMode): ModeAllowedResult {
        val level = getBatteryLevel()
        val charging = isCharging()
        
        // Search mode requires at least 30% or charging
        if (mode == OperatingMode.SEARCH && level < 30 && !charging) {
            return ModeAllowedResult(
                allowed = false,
                reason = "Search mode requires at least 30% battery or charging"
            )
        }
        
        // Guard mode works best when charging
        if (mode == OperatingMode.GUARD && !charging && level < 50) {
            return ModeAllowedResult(
                allowed = true,
                warning = "Guard mode is recommended with charger connected"
            )
        }
        
        return ModeAllowedResult(allowed = true)
    }
    
    /**
     * Get battery status summary
     */
    fun getBatteryStatus(): BatteryStatus {
        return BatteryStatus(
            level = getBatteryLevel(),
            isCharging = isCharging(),
            isFullyCharged = isFullyCharged(),
            temperature = getBatteryTemperature(),
            health = getBatteryHealth()
        )
    }
}

/**
 * Battery health states
 */
enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    COLD,
    UNKNOWN
}

/**
 * Battery status summary
 */
data class BatteryStatus(
    val level: Int,
    val isCharging: Boolean,
    val isFullyCharged: Boolean,
    val temperature: Float,
    val health: BatteryHealth
) {
    val isLow: Boolean get() = level < 20
    val isCritical: Boolean get() = level < 10
    val isOverheating: Boolean get() = temperature > 45f
}

/**
 * Mode allowance result
 */
data class ModeAllowedResult(
    val allowed: Boolean,
    val reason: String? = null,
    val warning: String? = null
)
