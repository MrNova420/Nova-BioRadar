package com.bioradar.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Boot receiver for auto-starting BioRadar on device boot
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Check if auto-start is enabled in preferences
            val prefs = context.getSharedPreferences("bioradar_prefs", Context.MODE_PRIVATE)
            val autoStartEnabled = prefs.getBoolean("auto_start_on_boot", false)
            
            if (autoStartEnabled) {
                // Start the radar service
                val serviceIntent = Intent(context, com.bioradar.sensor.RadarService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}

/**
 * Alert receiver for handling detection alerts
 */
class AlertReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FLASH_ALERT -> {
                // Handle screen flash alert
                // This would be handled by the UI
            }
            ACTION_TARGET_UPDATE -> {
                // Handle target update
                val targetCount = intent.getIntExtra("target_count", 0)
                val maxConfidence = intent.getFloatExtra("max_confidence", 0f)
                // Update widgets or notifications
            }
            ACTION_ALERT -> {
                // Handle presence alert
                val zoneName = intent.getStringExtra("zone_name")
                val alertLevel = intent.getStringExtra("alert_level")
                // Show notification
            }
        }
    }
    
    companion object {
        const val ACTION_FLASH_ALERT = "com.bioradar.FLASH_ALERT"
        const val ACTION_TARGET_UPDATE = "com.bioradar.TARGET_UPDATE"
        const val ACTION_ALERT = "com.bioradar.ALERT"
    }
}
