package com.bioradar.core.managers

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.bioradar.core.models.AlertType
import com.bioradar.core.models.ZoneStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages alerts and notifications for detection events
 * Supports multiple alert types: sound, vibration, visual, and combinations
 */
@Singleton
class AlertManager @Inject constructor(
    private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var isAlertActive = false
    
    /**
     * Trigger an alert based on zone status and alert type
     */
    fun triggerAlert(status: ZoneStatus, type: AlertType) {
        if (status == ZoneStatus.GREEN_CLEAR || status == ZoneStatus.UNKNOWN) {
            return
        }
        
        isAlertActive = true
        
        when (type) {
            AlertType.SOUND_AND_VIBRATION -> {
                playAlarmSound(status)
                vibratePattern(status)
            }
            AlertType.VIBRATION_ONLY -> {
                vibratePattern(status)
            }
            AlertType.SILENT_LOG_ONLY -> {
                // Just log, no physical alert - handled by caller
            }
            AlertType.VISUAL_ONLY -> {
                flashScreen()
            }
            AlertType.FLASH_AND_VIBRATION -> {
                flashScreen()
                vibratePattern(status)
            }
        }
    }
    
    /**
     * Play vibration pattern based on alert level
     */
    private fun vibratePattern(status: ZoneStatus) {
        val pattern = when (status) {
            ZoneStatus.RED_PRESENCE -> {
                // Urgent: 3 long pulses
                longArrayOf(0, 500, 200, 500, 200, 500)
            }
            ZoneStatus.YELLOW_POSSIBLE -> {
                // Warning: 2 short pulses
                longArrayOf(0, 200, 200, 200)
            }
            else -> {
                // Minimal: 1 short pulse
                longArrayOf(0, 100)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = when (status) {
                ZoneStatus.RED_PRESENCE -> intArrayOf(0, 255, 0, 255, 0, 255)
                ZoneStatus.YELLOW_POSSIBLE -> intArrayOf(0, 200, 0, 200)
                else -> intArrayOf(0, 150)
            }
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Play alarm sound based on alert level
     */
    private fun playAlarmSound(status: ZoneStatus) {
        try {
            // Release any existing player
            mediaPlayer?.release()
            
            // Get appropriate alarm sound
            val alarmUri: Uri = when (status) {
                ZoneStatus.RED_PRESENCE -> {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                }
                ZoneStatus.YELLOW_POSSIBLE -> {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
                else -> {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            }
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                prepare()
                
                // Set volume based on status
                val volume = when (status) {
                    ZoneStatus.RED_PRESENCE -> 1.0f
                    ZoneStatus.YELLOW_POSSIBLE -> 0.7f
                    else -> 0.5f
                }
                setVolume(volume, volume)
                
                // Loop for red alert
                isLooping = status == ZoneStatus.RED_PRESENCE
                
                start()
            }
        } catch (e: Exception) {
            // Fallback: use system notification sound
            try {
                val notification = RingtoneManager.getRingtone(
                    context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                )
                notification?.play()
            } catch (e2: Exception) {
                // Silent fail - vibration will still work
            }
        }
    }
    
    /**
     * Flash screen for visual alert
     * Sends broadcast for UI to handle
     */
    private fun flashScreen() {
        val intent = Intent(ACTION_FLASH_ALERT).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
    
    /**
     * Stop any active alert
     */
    fun stopAlert() {
        isAlertActive = false
        
        // Stop vibration
        vibrator.cancel()
        
        // Stop sound
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    /**
     * Check if an alert is currently active
     */
    fun isAlertActive(): Boolean = isAlertActive
    
    /**
     * Quick alert for immediate presence detection
     */
    fun quickAlert() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
    
    /**
     * Haptic feedback for UI interactions
     */
    fun hapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
    
    companion object {
        const val ACTION_FLASH_ALERT = "com.bioradar.FLASH_ALERT"
    }
}
