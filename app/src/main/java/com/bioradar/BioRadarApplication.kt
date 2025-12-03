package com.bioradar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * BioRadar Application class
 * Initializes Hilt dependency injection and notification channels
 */
@HiltAndroidApp
class BioRadarApplication : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_RADAR = "radar_service"
        const val NOTIFICATION_CHANNEL_ALERTS = "detection_alerts"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Radar Service Channel
            val radarChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_RADAR,
                getString(R.string.notification_channel_radar),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background radar monitoring service"
                setShowBadge(false)
            }
            
            // Detection Alerts Channel
            val alertsChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ALERTS,
                getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Detection and presence alerts"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(radarChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }
}
