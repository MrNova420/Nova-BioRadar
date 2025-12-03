package com.bioradar.sensor

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bioradar.BioRadarApplication
import com.bioradar.MainActivity
import com.bioradar.R
import com.bioradar.core.models.DataSource
import com.bioradar.core.models.ModeProfiles
import com.bioradar.core.models.OperatingMode
import com.bioradar.sensor.fusion.FusionEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for continuous radar monitoring
 */
@AndroidEntryPoint
class RadarService : Service() {
    
    @Inject
    lateinit var fusionEngine: FusionEngine
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentMode = OperatingMode.NORMAL
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startScanning()
            ACTION_STOP -> stopScanning()
            ACTION_SET_MODE -> {
                val mode = intent.getStringExtra(EXTRA_MODE)?.let {
                    try { OperatingMode.valueOf(it) } catch (e: Exception) { null }
                }
                mode?.let { setMode(it) }
            }
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
        serviceScope.cancel()
    }
    
    private fun startScanning() {
        val profile = ModeProfiles.getProfile(currentMode)
        
        serviceScope.launch {
            fusionEngine.start(
                enabledSensors = profile.enabledSensors,
                modeProfile = profile
            )
            
            // Observe targets and broadcast updates
            fusionEngine.targets.collect { targets ->
                if (targets.isNotEmpty()) {
                    broadcastTargetUpdate(targets.size, targets.maxOfOrNull { it.confidence } ?: 0f)
                }
            }
        }
        
        updateNotification("Scanning in ${currentMode.name} mode")
    }
    
    private fun stopScanning() {
        fusionEngine.stop()
        updateNotification("Scanning stopped")
    }
    
    private fun setMode(mode: OperatingMode) {
        currentMode = mode
        
        // Restart scanning with new mode if active
        if (fusionEngine.isActive.value) {
            stopScanning()
            startScanning()
        }
        
        updateNotification("Mode: ${mode.name}")
    }
    
    private fun broadcastTargetUpdate(targetCount: Int, maxConfidence: Float) {
        val intent = Intent(com.bioradar.core.AlertReceiver.ACTION_TARGET_UPDATE).apply {
            putExtra("target_count", targetCount)
            putExtra("max_confidence", maxConfidence)
        }
        sendBroadcast(intent)
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, BioRadarApplication.NOTIFICATION_CHANNEL_RADAR)
            .setContentTitle("BioRadar")
            .setContentText("Radar monitoring active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, BioRadarApplication.NOTIFICATION_CHANNEL_RADAR)
            .setContentTitle("BioRadar")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.bioradar.action.START"
        const val ACTION_STOP = "com.bioradar.action.STOP"
        const val ACTION_SET_MODE = "com.bioradar.action.SET_MODE"
        const val EXTRA_MODE = "extra_mode"
    }
}
