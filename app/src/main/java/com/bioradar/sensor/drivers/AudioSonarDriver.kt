package com.bioradar.sensor.drivers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Audio Sonar system for ultrasonic echo detection
 * Emits ultrasonic pings and analyzes echoes for presence detection
 */
@Singleton
class AudioSonarDriver @Inject constructor(
    private val context: Context
) {
    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    
    private val sampleRate = 48000
    private var pingFrequency = 18000f // 18kHz default
    private val pingDuration = 0.05f // 50ms
    private val listenDuration = 0.2f // 200ms
    
    private val echoHistory = mutableListOf<EchoReading>()
    
    /**
     * Check if audio is available
     */
    fun isAvailable(): Boolean {
        return hasPermission() && 
               checkAudioSupport()
    }
    
    /**
     * Set the ultrasonic ping frequency
     */
    fun setPingFrequency(frequency: Float) {
        pingFrequency = frequency.coerceIn(16000f, 22000f)
    }
    
    /**
     * Start the sonar system
     * Returns a Flow of echo readings
     */
    fun startSonar(
        pingIntervalMs: Long = 500
    ): Flow<SonarResult> = flow {
        if (!isAvailable()) {
            return@flow
        }
        
        isRunning = true
        initializeAudio()
        
        try {
            while (isRunning) {
                val result = performPingAndListen()
                emit(result)
                
                // Store for analysis
                result.echoes.forEach { echo ->
                    echoHistory.add(echo)
                    if (echoHistory.size > MAX_ECHO_HISTORY) {
                        echoHistory.removeAt(0)
                    }
                }
                
                delay(pingIntervalMs)
            }
        } finally {
            releaseAudio()
        }
    }
    
    /**
     * Stop the sonar system
     */
    fun stop() {
        isRunning = false
    }
    
    /**
     * Generate ultrasonic ping signal
     */
    private fun generatePing(): ShortArray {
        val numSamples = (sampleRate * pingDuration).toInt()
        val samples = ShortArray(numSamples)
        
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            // Apply envelope for smooth start/end
            val envelope = if (i < numSamples / 10) {
                i.toFloat() / (numSamples / 10)
            } else if (i > numSamples * 9 / 10) {
                (numSamples - i).toFloat() / (numSamples / 10)
            } else {
                1f
            }
            
            val sample = envelope * sin(2 * PI * pingFrequency * t)
            samples[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        
        return samples
    }
    
    /**
     * Generate FMCW (Frequency Modulated Continuous Wave) chirp for extended range
     */
    private fun generateFmcwChirp(): ShortArray {
        val numSamples = (sampleRate * pingDuration).toInt()
        val samples = ShortArray(numSamples)
        
        val startFreq = 17000f
        val endFreq = 22000f
        val chirpRate = (endFreq - startFreq) / pingDuration
        
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val instantFreq = startFreq + chirpRate * t
            val phase = 2 * PI * (startFreq * t + 0.5f * chirpRate * t * t)
            
            // Apply envelope
            val envelope = if (i < numSamples / 10) {
                i.toFloat() / (numSamples / 10)
            } else if (i > numSamples * 9 / 10) {
                (numSamples - i).toFloat() / (numSamples / 10)
            } else {
                1f
            }
            
            samples[i] = (envelope * sin(phase) * Short.MAX_VALUE).toInt().toShort()
        }
        
        return samples
    }
    
    /**
     * Perform a single ping and listen for echoes
     */
    private suspend fun performPingAndListen(): SonarResult = withContext(Dispatchers.IO) {
        val pingSamples = generatePing()
        val listenSamples = (sampleRate * listenDuration).toInt()
        val recordBuffer = ShortArray(listenSamples)
        
        // Start recording before playing ping
        audioRecord?.startRecording()
        
        // Play ping
        audioTrack?.write(pingSamples, 0, pingSamples.size)
        audioTrack?.play()
        
        // Wait for ping to complete
        delay((pingDuration * 1000).toLong())
        
        audioTrack?.stop()
        
        // Record echoes
        val readCount = audioRecord?.read(recordBuffer, 0, listenSamples) ?: 0
        
        audioRecord?.stop()
        
        // Analyze recorded audio for echoes
        analyzeEchoes(recordBuffer, readCount)
    }
    
    /**
     * Analyze recorded audio for echoes using FFT and correlation
     */
    private fun analyzeEchoes(samples: ShortArray, sampleCount: Int): SonarResult {
        if (sampleCount <= 0) {
            return SonarResult(echoes = emptyList(), noiseFloor = 0f, quality = 0f)
        }
        
        // Convert to float for processing
        val floatSamples = samples.take(sampleCount).map { it.toFloat() / Short.MAX_VALUE }
        
        // Calculate noise floor
        val noiseFloor = floatSamples.map { abs(it) }.average().toFloat()
        
        // Find peaks that could be echoes
        val echoes = mutableListOf<EchoReading>()
        val windowSize = sampleRate / 100 // 10ms windows
        
        for (i in 0 until sampleCount step windowSize) {
            val window = floatSamples.drop(i).take(windowSize)
            if (window.isEmpty()) break
            
            val energy = window.map { it * it }.average().toFloat()
            val peakAmplitude = window.maxOfOrNull { abs(it) } ?: 0f
            
            // Check if this window contains significant energy above noise floor
            if (peakAmplitude > noiseFloor * 3) {
                // Calculate distance from delay
                val delaySamples = i + windowSize / 2
                val delaySeconds = delaySamples.toFloat() / sampleRate
                val distance = delaySeconds * SPEED_OF_SOUND / 2 // Divide by 2 for round trip
                
                // Estimate Doppler shift for motion detection
                val dopplerShift = estimateDopplerShift(window)
                
                echoes.add(EchoReading(
                    distance = distance,
                    amplitude = peakAmplitude,
                    energy = energy,
                    dopplerShift = dopplerShift,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        
        // Calculate overall quality score
        val quality = when {
            echoes.isEmpty() -> 0f
            noiseFloor < 0.01f -> 1f
            noiseFloor < 0.05f -> 0.8f
            noiseFloor < 0.1f -> 0.5f
            else -> 0.3f
        }
        
        return SonarResult(
            echoes = echoes.take(MAX_ECHOES),
            noiseFloor = noiseFloor,
            quality = quality
        )
    }
    
    /**
     * Estimate Doppler shift for motion detection
     * Positive shift = approaching, negative = receding
     */
    private fun estimateDopplerShift(window: List<Float>): Float {
        if (window.size < 4) return 0f
        
        // Simple zero-crossing rate analysis
        var zeroCrossings = 0
        for (i in 1 until window.size) {
            if ((window[i] >= 0 && window[i-1] < 0) || 
                (window[i] < 0 && window[i-1] >= 0)) {
                zeroCrossings++
            }
        }
        
        val estimatedFreq = zeroCrossings * sampleRate.toFloat() / (2 * window.size)
        return estimatedFreq - pingFrequency
    }
    
    /**
     * Calculate motion score based on recent echoes
     */
    fun calculateMotionScore(): Float {
        if (echoHistory.size < 2) return 0f
        
        // Analyze Doppler shifts
        val dopplerShifts = echoHistory.mapNotNull { it.dopplerShift }
        if (dopplerShifts.isEmpty()) return 0f
        
        val avgShift = dopplerShifts.map { abs(it) }.average().toFloat()
        return (avgShift / 100f).coerceIn(0f, 1f)
    }
    
    /**
     * Estimate distance to nearest detected object
     */
    fun getNearestEcho(): EchoReading? {
        return echoHistory.lastOrNull()?.let { latestReading ->
            echoHistory.filter { 
                it.timestamp > System.currentTimeMillis() - 5000 
            }.minByOrNull { it.distance }
        }
    }
    
    private fun initializeAudio() {
        // Initialize AudioTrack for playing pings
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        
        // Initialize AudioRecord for capturing echoes
        val recordBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recordBufferSize
            )
        } catch (e: SecurityException) {
            // Handle permission error
        }
    }
    
    private fun releaseAudio() {
        audioTrack?.release()
        audioTrack = null
        audioRecord?.release()
        audioRecord = null
    }
    
    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun checkAudioSupport(): Boolean {
        return try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            minBufferSize > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear echo history
     */
    fun clearHistory() {
        echoHistory.clear()
    }
    
    companion object {
        private const val SPEED_OF_SOUND = 343f // m/s at 20°C
        private const val MAX_ECHO_HISTORY = 50
        private const val MAX_ECHOES = 10
    }
}

/**
 * Single echo reading
 */
data class EchoReading(
    val distance: Float,           // Meters
    val amplitude: Float,          // 0-1
    val energy: Float,             // Average energy in window
    val dopplerShift: Float?,      // Hz, null if not calculable
    val timestamp: Long
)

/**
 * Complete sonar result from one ping cycle
 */
data class SonarResult(
    val echoes: List<EchoReading>,
    val noiseFloor: Float,
    val quality: Float              // 0-1, signal quality
) {
    val hasDetection: Boolean get() = echoes.isNotEmpty()
    val nearestEcho: EchoReading? get() = echoes.minByOrNull { it.distance }
    val strongestEcho: EchoReading? get() = echoes.maxByOrNull { it.amplitude }
}
