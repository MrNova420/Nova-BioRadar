package com.bioradar.modes

import android.content.Context
import com.bioradar.sensor.drivers.SelfGeneratedWiFiSystem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for Blackout Mode
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BlackoutModeTest {
    
    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var selfGeneratedWiFiSystem: SelfGeneratedWiFiSystem
    
    private lateinit var blackoutMode: BlackoutMode
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        blackoutMode = BlackoutMode(
            context = context,
            selfGeneratedWiFiSystem = selfGeneratedWiFiSystem
        )
    }
    
    @Test
    fun `blackout mode starts inactive`() {
        assert(blackoutMode.status.value is BlackoutMode.BlackoutStatus.Inactive)
    }
    
    @Test
    fun `blackout mode activates with profile`() = runTest {
        whenever(selfGeneratedWiFiSystem.isSupported()).thenReturn(true)
        
        val result = blackoutMode.activate(BlackoutMode.BlackoutProfile.BALANCED)
        
        assert(result.isSuccess)
        assert(blackoutMode.status.value is BlackoutMode.BlackoutStatus.Active)
    }
    
    @Test
    fun `blackout mode deactivates cleanly`() = runTest {
        whenever(selfGeneratedWiFiSystem.isSupported()).thenReturn(true)
        
        blackoutMode.activate(BlackoutMode.BlackoutProfile.BALANCED)
        assert(blackoutMode.status.value is BlackoutMode.BlackoutStatus.Active)
        
        blackoutMode.deactivate()
        assert(blackoutMode.status.value is BlackoutMode.BlackoutStatus.Inactive)
    }
    
    @Test
    fun `profile configurations are distinct`() {
        val maxRange = blackoutMode.createConfiguration(BlackoutMode.BlackoutProfile.MAXIMUM_RANGE)
        val endurance = blackoutMode.createConfiguration(BlackoutMode.BlackoutProfile.MAXIMUM_ENDURANCE)
        val stealth = blackoutMode.createConfiguration(BlackoutMode.BlackoutProfile.STEALTH)
        
        // Maximum Range should have highest range, lowest battery
        assert(maxRange.estimatedRange > endurance.estimatedRange)
        assert(maxRange.estimatedBatteryHours < endurance.estimatedBatteryHours)
        
        // Stealth should have no WiFi generation
        assert(stealth.wifiGeneration == BlackoutMode.WiFiGenerationMode.DISABLED)
        assert(maxRange.wifiGeneration != BlackoutMode.WiFiGenerationMode.DISABLED)
    }
    
    @Test
    fun `recommended profile adapts to battery level`() {
        val highBattery = blackoutMode.getRecommendedProfile(80, false)
        val mediumBattery = blackoutMode.getRecommendedProfile(40, false)
        val lowBattery = blackoutMode.getRecommendedProfile(20, false)
        val charging = blackoutMode.getRecommendedProfile(30, true)
        
        // Should recommend more conservative profiles as battery drops
        assert(highBattery == BlackoutMode.BlackoutProfile.BALANCED)
        assert(lowBattery == BlackoutMode.BlackoutProfile.STEALTH || 
               lowBattery == BlackoutMode.BlackoutProfile.MAXIMUM_ENDURANCE)
        assert(charging == BlackoutMode.BlackoutProfile.MAXIMUM_RANGE)
    }
    
    @Test
    fun `remaining time estimation works`() {
        val time100 = blackoutMode.estimateRemainingTime(100, BlackoutMode.BlackoutProfile.BALANCED)
        val time50 = blackoutMode.estimateRemainingTime(50, BlackoutMode.BlackoutProfile.BALANCED)
        val time25 = blackoutMode.estimateRemainingTime(25, BlackoutMode.BlackoutProfile.BALANCED)
        
        // Time should decrease linearly with battery
        assert(time100 > time50)
        assert(time50 > time25)
        assert(time100 / time50 in 1.9f..2.1f) // Approximately 2x
    }
    
    @Test
    fun `profile descriptions are provided`() {
        BlackoutMode.BlackoutProfile.values().forEach { profile ->
            val description = blackoutMode.getProfileDescription(profile)
            assert(description.isNotEmpty())
            assert(description.length > 20) // Should be descriptive
        }
    }
}
