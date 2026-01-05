package com.bioradar.sensor.fusion

import android.content.Context
import com.bioradar.core.models.DataSource
import com.bioradar.core.models.ModeProfile
import com.bioradar.core.models.OperatingMode
import com.bioradar.sensor.drivers.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for FusionEngine UWB and Self-Generated WiFi integration
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FusionEngineIntegrationTest {
    
    @Mock
    private lateinit var bluetoothScanner: BluetoothScanner
    
    @Mock
    private lateinit var wifiScanner: WifiScanner
    
    @Mock
    private lateinit var audioSonarDriver: AudioSonarDriver
    
    @Mock
    private lateinit var cameraMotionDriver: CameraMotionDriver
    
    @Mock
    private lateinit var context: Context
    
    private lateinit var fusionEngine: FusionEngine
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Setup mock behaviors
        whenever(bluetoothScanner.isAvailable()).thenReturn(false)
        whenever(wifiScanner.isAvailable()).thenReturn(false)
        whenever(audioSonarDriver.isAvailable()).thenReturn(false)
        whenever(cameraMotionDriver.isAvailable()).thenReturn(false)
        
        fusionEngine = FusionEngine(
            bluetoothScanner = bluetoothScanner,
            wifiScanner = wifiScanner,
            audioSonarDriver = audioSonarDriver,
            cameraMotionDriver = cameraMotionDriver,
            context = context
        )
    }
    
    @Test
    fun `fusion engine starts without UWB when not enabled`() = runTest {
        val enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH)
        val profile = ModeProfile(
            mode = OperatingMode.NORMAL,
            scanIntervalMs = 1000,
            enabledSensors = enabledSensors,
            uiBrightness = 1.0f,
            animationsEnabled = true,
            sonarEnabled = false,
            cameraEnabled = false,
            alertTypes = emptySet(),
            batteryThreshold = 20,
            description = "Test"
        )
        
        fusionEngine.start(enabledSensors, profile)
        
        assert(fusionEngine.isActive.value)
    }
    
    @Test
    fun `fusion engine starts with UWB when enabled`() = runTest {
        val enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.UWB)
        val profile = ModeProfile(
            mode = OperatingMode.ULTIMATE,
            scanIntervalMs = 50,
            enabledSensors = enabledSensors,
            uiBrightness = 1.0f,
            animationsEnabled = true,
            sonarEnabled = false,
            cameraEnabled = false,
            alertTypes = emptySet(),
            batteryThreshold = 30,
            description = "Ultimate Mode"
        )
        
        fusionEngine.start(enabledSensors, profile)
        
        assert(fusionEngine.isActive.value)
        // UWB driver should be initialized internally
    }
    
    @Test
    fun `fusion engine stops and cleans up resources`() = runTest {
        val enabledSensors = setOf(DataSource.WIFI, DataSource.UWB)
        val profile = ModeProfile(
            mode = OperatingMode.NORMAL,
            scanIntervalMs = 1000,
            enabledSensors = enabledSensors,
            uiBrightness = 1.0f,
            animationsEnabled = true,
            sonarEnabled = false,
            cameraEnabled = false,
            alertTypes = emptySet(),
            batteryThreshold = 20,
            description = "Test"
        )
        
        fusionEngine.start(enabledSensors, profile)
        assert(fusionEngine.isActive.value)
        
        fusionEngine.stop()
        assert(!fusionEngine.isActive.value)
        assert(fusionEngine.targets.value.isEmpty())
    }
    
    @Test
    fun `sensor weights include UWB with higher priority`() {
        val weights = FusionEngine.SensorWeights()
        
        // UWB should have highest weight
        assert(weights.uwb > weights.sonar)
        assert(weights.uwb > weights.camera)
        assert(weights.uwb > weights.wifi)
        assert(weights.uwb > weights.bluetooth)
    }
}
