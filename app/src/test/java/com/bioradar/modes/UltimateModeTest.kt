package com.bioradar.modes

import android.content.Context
import com.bioradar.core.utils.CapabilityDetector
import com.bioradar.sensor.drivers.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for Ultimate Mode
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UltimateModeTest {
    
    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var capabilityDetector: CapabilityDetector
    
    @Mock
    private lateinit var bluetoothScanner: BluetoothScanner
    
    @Mock
    private lateinit var wifiScanner: WifiScanner
    
    @Mock
    private lateinit var audioSonarDriver: AudioSonarDriver
    
    @Mock
    private lateinit var cameraMotionDriver: CameraMotionDriver
    
    @Mock
    private lateinit var selfGeneratedWiFiSystem: SelfGeneratedWiFiSystem
    
    private lateinit var ultimateMode: UltimateMode
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        ultimateMode = UltimateMode(
            context = context,
            capabilityDetector = capabilityDetector,
            bluetoothScanner = bluetoothScanner,
            wifiScanner = wifiScanner,
            audioSonarDriver = audioSonarDriver,
            cameraMotionDriver = cameraMotionDriver,
            selfGeneratedWiFiSystem = selfGeneratedWiFiSystem
        )
    }
    
    @Test
    fun `ultimate mode starts inactive`() {
        assert(!ultimateMode.isActive.value)
        assert(ultimateMode.configuration.value == null)
    }
    
    @Test
    fun `ultimate mode activates and creates configuration`() = runTest {
        // Setup mocks for sensor availability
        whenever(bluetoothScanner.isAvailable()).thenReturn(true)
        whenever(wifiScanner.isAvailable()).thenReturn(true)
        whenever(audioSonarDriver.isAvailable()).thenReturn(true)
        whenever(cameraMotionDriver.isAvailable()).thenReturn(false)
        whenever(selfGeneratedWiFiSystem.isSupported()).thenReturn(true)
        
        val config = ultimateMode.activate()
        
        assert(ultimateMode.isActive.value)
        assert(ultimateMode.configuration.value != null)
        assert(config.enabledSensors.isNotEmpty())
        assert(config.detectionMethods.isNotEmpty())
    }
    
    @Test
    fun `ultimate mode deactivates cleanly`() = runTest {
        whenever(bluetoothScanner.isAvailable()).thenReturn(true)
        whenever(wifiScanner.isAvailable()).thenReturn(true)
        
        ultimateMode.activate()
        assert(ultimateMode.isActive.value)
        
        ultimateMode.deactivate()
        assert(!ultimateMode.isActive.value)
        assert(ultimateMode.configuration.value == null)
    }
    
    @Test
    fun `device tier classification works`() = runTest {
        whenever(bluetoothScanner.isAvailable()).thenReturn(true)
        whenever(wifiScanner.isAvailable()).thenReturn(true)
        whenever(audioSonarDriver.isAvailable()).thenReturn(true)
        whenever(cameraMotionDriver.isAvailable()).thenReturn(true)
        
        val config = ultimateMode.activate()
        
        // Should classify device tier
        assert(config.deviceTier != UltimateMode.DeviceTier.UNKNOWN)
    }
    
    @Test
    fun `performance metrics are calculated`() = runTest {
        whenever(bluetoothScanner.isAvailable()).thenReturn(true)
        whenever(wifiScanner.isAvailable()).thenReturn(true)
        
        val config = ultimateMode.activate()
        val metrics = config.metrics
        
        assert(metrics.totalSensors > 0)
        assert(metrics.detectionMethods > 0)
        assert(metrics.estimatedRange > 0f)
        assert(metrics.updateRateHz > 0)
        assert(metrics.expectedConfidence > 0f)
        assert(metrics.batteryLifeHours > 0f)
    }
}
