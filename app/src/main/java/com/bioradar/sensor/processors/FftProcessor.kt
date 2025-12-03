package com.bioradar.sensor.processors

import kotlin.math.*

/**
 * FFT Processor for frequency domain analysis
 * Used for sonar echo analysis and frequency detection
 */
class FftProcessor {
    
    /**
     * Perform FFT on real-valued input
     * Returns complex array [real, imag, real, imag, ...]
     */
    fun fft(input: FloatArray): FloatArray {
        val n = input.size
        if (n == 0 || n and (n - 1) != 0) {
            throw IllegalArgumentException("Input size must be a power of 2")
        }
        
        // Prepare complex array (interleaved real/imag)
        val complex = FloatArray(n * 2)
        for (i in 0 until n) {
            complex[i * 2] = input[i]     // real
            complex[i * 2 + 1] = 0f       // imaginary
        }
        
        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n) {
            if (j > i) {
                val tempReal = complex[j * 2]
                val tempImag = complex[j * 2 + 1]
                complex[j * 2] = complex[i * 2]
                complex[j * 2 + 1] = complex[i * 2 + 1]
                complex[i * 2] = tempReal
                complex[i * 2 + 1] = tempImag
            }
            var m = n / 2
            while (m >= 1 && j >= m) {
                j -= m
                m /= 2
            }
            j += m
        }
        
        // Cooley-Tukey FFT
        var mmax = 1
        while (n > mmax) {
            val istep = mmax * 2
            val theta = -PI.toFloat() / mmax
            val wtemp = sin(0.5f * theta)
            val wpr = -2.0f * wtemp * wtemp
            val wpi = sin(theta)
            var wr = 1.0f
            var wi = 0.0f
            
            for (m in 0 until mmax) {
                for (i in m until n step istep) {
                    val jIdx = i + mmax
                    val tempr = wr * complex[jIdx * 2] - wi * complex[jIdx * 2 + 1]
                    val tempi = wr * complex[jIdx * 2 + 1] + wi * complex[jIdx * 2]
                    complex[jIdx * 2] = complex[i * 2] - tempr
                    complex[jIdx * 2 + 1] = complex[i * 2 + 1] - tempi
                    complex[i * 2] += tempr
                    complex[i * 2 + 1] += tempi
                }
                val wtemp2 = wr
                wr = wr * wpr - wi * wpi + wr
                wi = wi * wpr + wtemp2 * wpi + wi
            }
            mmax = istep
        }
        
        return complex
    }
    
    /**
     * Calculate magnitude spectrum from FFT result
     */
    fun magnitude(fftResult: FloatArray): FloatArray {
        val n = fftResult.size / 2
        val magnitudes = FloatArray(n)
        
        for (i in 0 until n) {
            val real = fftResult[i * 2]
            val imag = fftResult[i * 2 + 1]
            magnitudes[i] = sqrt(real * real + imag * imag)
        }
        
        return magnitudes
    }
    
    /**
     * Calculate power spectrum (magnitude squared)
     */
    fun powerSpectrum(fftResult: FloatArray): FloatArray {
        val n = fftResult.size / 2
        val powers = FloatArray(n)
        
        for (i in 0 until n) {
            val real = fftResult[i * 2]
            val imag = fftResult[i * 2 + 1]
            powers[i] = real * real + imag * imag
        }
        
        return powers
    }
    
    /**
     * Find peak frequency in spectrum
     */
    fun findPeakFrequency(
        magnitudes: FloatArray,
        sampleRate: Int,
        minFreq: Float = 0f,
        maxFreq: Float = Float.MAX_VALUE
    ): PeakResult? {
        val n = magnitudes.size
        val binWidth = sampleRate.toFloat() / (n * 2)
        
        val minBin = (minFreq / binWidth).toInt().coerceAtLeast(1)
        val maxBin = (maxFreq / binWidth).toInt().coerceAtMost(n - 1)
        
        if (minBin >= maxBin) return null
        
        var peakBin = minBin
        var peakMagnitude = magnitudes[minBin]
        
        for (i in minBin..maxBin) {
            if (magnitudes[i] > peakMagnitude) {
                peakMagnitude = magnitudes[i]
                peakBin = i
            }
        }
        
        // Quadratic interpolation for more accurate frequency
        val freq = if (peakBin > 0 && peakBin < n - 1) {
            val alpha = magnitudes[peakBin - 1]
            val beta = magnitudes[peakBin]
            val gamma = magnitudes[peakBin + 1]
            val p = 0.5f * (alpha - gamma) / (alpha - 2 * beta + gamma)
            (peakBin + p) * binWidth
        } else {
            peakBin * binWidth
        }
        
        return PeakResult(
            frequency = freq,
            magnitude = peakMagnitude,
            bin = peakBin
        )
    }
    
    /**
     * Find multiple peaks in spectrum
     */
    fun findPeaks(
        magnitudes: FloatArray,
        sampleRate: Int,
        threshold: Float,
        minPeakDistance: Int = 5
    ): List<PeakResult> {
        val peaks = mutableListOf<PeakResult>()
        val binWidth = sampleRate.toFloat() / (magnitudes.size * 2)
        
        for (i in 1 until magnitudes.size - 1) {
            if (magnitudes[i] > threshold &&
                magnitudes[i] > magnitudes[i - 1] &&
                magnitudes[i] > magnitudes[i + 1]) {
                
                // Check minimum distance from previous peak
                if (peaks.isEmpty() || i - peaks.last().bin >= minPeakDistance) {
                    peaks.add(PeakResult(
                        frequency = i * binWidth,
                        magnitude = magnitudes[i],
                        bin = i
                    ))
                }
            }
        }
        
        return peaks.sortedByDescending { it.magnitude }
    }
    
    /**
     * Detect Doppler shift between expected and observed frequency
     */
    fun detectDopplerShift(
        magnitudes: FloatArray,
        sampleRate: Int,
        expectedFreq: Float,
        searchRange: Float = 500f
    ): DopplerResult {
        val peak = findPeakFrequency(
            magnitudes,
            sampleRate,
            expectedFreq - searchRange,
            expectedFreq + searchRange
        ) ?: return DopplerResult(
            shift = 0f,
            detectedFreq = null,
            isMoving = false,
            direction = MovementDirection.UNKNOWN
        )
        
        val shift = peak.frequency - expectedFreq
        
        return DopplerResult(
            shift = shift,
            detectedFreq = peak.frequency,
            magnitude = peak.magnitude,
            isMoving = abs(shift) > DOPPLER_THRESHOLD,
            direction = when {
                shift > DOPPLER_THRESHOLD -> MovementDirection.APPROACHING
                shift < -DOPPLER_THRESHOLD -> MovementDirection.RECEDING
                else -> MovementDirection.STATIONARY
            }
        )
    }
    
    /**
     * Apply Hamming window to reduce spectral leakage
     */
    fun applyHammingWindow(samples: FloatArray): FloatArray {
        val n = samples.size
        val windowed = FloatArray(n)
        
        for (i in 0 until n) {
            val window = 0.54f - 0.46f * cos(2 * PI.toFloat() * i / (n - 1))
            windowed[i] = samples[i] * window
        }
        
        return windowed
    }
    
    /**
     * Apply Hann window
     */
    fun applyHannWindow(samples: FloatArray): FloatArray {
        val n = samples.size
        val windowed = FloatArray(n)
        
        for (i in 0 until n) {
            val window = 0.5f * (1 - cos(2 * PI.toFloat() * i / (n - 1)))
            windowed[i] = samples[i] * window
        }
        
        return windowed
    }
    
    data class PeakResult(
        val frequency: Float,
        val magnitude: Float,
        val bin: Int
    )
    
    data class DopplerResult(
        val shift: Float,
        val detectedFreq: Float?,
        val magnitude: Float = 0f,
        val isMoving: Boolean,
        val direction: MovementDirection
    )
    
    enum class MovementDirection {
        APPROACHING,
        RECEDING,
        STATIONARY,
        UNKNOWN
    }
    
    companion object {
        private const val DOPPLER_THRESHOLD = 20f  // Hz
    }
}

/**
 * Utility to get next power of 2
 */
fun nextPowerOf2(n: Int): Int {
    var p = 1
    while (p < n) p = p shl 1
    return p
}
