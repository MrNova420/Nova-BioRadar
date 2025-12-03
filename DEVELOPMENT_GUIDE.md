# Nova BioRadar - All-in-One Autonomous Development Guide

> **Complete Blueprint, Roadmap & Technical Specification**
> Transform any Android phone into a futuristic life-form detection radar

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Reality Check - What's Possible](#2-reality-check---whats-possible)
3. [Project Vision](#3-project-vision)
4. [System Architecture](#4-system-architecture)
5. [Hardware Tiers](#5-hardware-tiers)
6. [Core Data Models](#6-core-data-models)
7. [Offline-First Design](#7-offline-first-design)
8. [Emergency & Field Modes](#8-emergency--field-modes)
9. [Perimeter Guard System](#9-perimeter-guard-system)
10. [NovaMesh - Multi-Device Networking](#10-novamesh---multi-device-networking)
11. [Security & Privacy](#11-security--privacy)
12. [Phased Development Roadmap](#12-phased-development-roadmap)
13. [Detailed Implementation Specifications](#13-detailed-implementation-specifications)
14. [UI/UX Design](#14-uiux-design)
15. [Testing Strategy](#15-testing-strategy)
16. [Accessibility & Universal Compatibility](#16-accessibility--universal-compatibility)
17. [Ethics & Legal Considerations](#17-ethics--legal-considerations)

---

## 1. Executive Summary

**Nova BioRadar** is a futuristic Android application that transforms any Android phone into a handheld life-form detection radar. Using the phone's built-in sensors (WiFi, Bluetooth, microphone, camera, accelerometer, magnetometer, and UWB where available), the app detects and visualizes nearby living presence with distance estimation, directional information, motion status, and confidence scoring.

### Core Mission
> "Detect likely living presence (primarily humans) near the phone and visualize distance + direction + confidence score — all working completely offline."

### Key Differentiators
- **Offline-First**: Works without internet, cellular, or GPS
- **Multi-Sensor Fusion**: Combines 6+ sensor types for accuracy
- **Emergency Ready**: Designed for field use in disasters/conflicts
- **Multi-Device Mesh**: Multiple phones form distributed radar network
- **Privacy-Focused**: No identity tracking, encrypted logs, panic wipe

---

## 2. Reality Check - What's Possible

### Available Sensors & Their Capabilities

| Sensor | Detection Method | Capability | Limitations |
|--------|-----------------|------------|-------------|
| **WiFi** | RSSI fluctuation analysis | Motion detection, rough distance | Requires nearby APs |
| **Bluetooth/BLE** | Signal strength variance | Presence detection, device proximity | Range ~10-30m |
| **Microphone** | Ultrasonic echo/sonar | Front-facing motion, distance | Requires audio emission |
| **Camera** | Optical flow analysis | Direction, motion vectors | Needs line of sight |
| **Accelerometer/Gyro** | Device motion detection | Stabilization, false-positive filtering | Only detects phone movement |
| **Magnetometer** | EM field detection | Anomaly/metal detection | Limited bio-detection |
| **UWB** (select phones) | True radar ranging | Precise distance & angle | Not on all devices |

### What We ARE Building
- Probabilistic presence detection
- Motion and movement tracking
- Approximate distance estimation
- Directional awareness (front/back/sides)
- Confidence scoring (0-100%)
- Multi-target tracking

### What We Are NOT Building
- "See through walls" magic
- Face/identity recognition
- Exact GPS coordinates of individuals
- Covert surveillance tools
- Weapons targeting systems

---

## 3. Project Vision

### Primary Goals
Turn an Android phone into a live-presence radar displaying:

1. **Distance** - Rough estimate in meters
2. **Direction/Angle** - 360° bearing from device
3. **Motion State** - Still vs. moving targets
4. **Classification** - Human / Possible Life / Noise / Unknown
5. **Confidence Score** - 0-100% certainty level

### Use Cases

#### Consumer/Civilian
- Smart home presence monitoring
- Personal security awareness
- Sci-fi gadget experimentation
- Educational sensor exploration

#### Emergency/Field
- Search & rescue operations
- Perimeter security monitoring
- Disaster response coordination
- Camp/building protection
- Offline area surveillance

### Design Principles
1. **Offline-First** - Works without any network
2. **Universal** - Runs on any Android 8.0+ device
3. **Accessible** - Usable by anyone
4. **Ethical** - Defense/awareness only, never targeting
5. **Modular** - Easy to extend and upgrade
6. **Battery-Aware** - Efficient power management
7. **Robust** - Crash recovery, data persistence

---

## 4. System Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       NOVA BIORADAR ARCHITECTURE                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                 LAYER 1: HARDWARE (Phone Sensors)                   │ │
│  │   WiFi | Bluetooth | Microphone | Camera | Accel | Gyro | UWB      │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                    │                                     │
│                                    ▼                                     │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    LAYER 2: SENSOR DRIVERS                          │ │
│  │   WiFiDriver | BluetoothDriver | AudioSonarDriver |                 │ │
│  │   CameraMotionDriver | MotionDriver | UWBRadarDriver                │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                    │                                     │
│                                    ▼                                     │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                  LAYER 3: SIGNAL PROCESSING                         │ │
│  │   FFT Processor | Noise Filter | RSSI Analyzer |                    │ │
│  │   Doppler Detector | Optical Flow | Baseline Calibration            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                    │                                     │
│                                    ▼                                     │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                 LAYER 4: FUSION & DETECTION ENGINE                  │ │
│  │   Combines all sensors → Unified "Presence Map"                     │ │
│  │   Outputs: [{angle, distance, confidence, type}, ...]               │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                    │                                     │
│                                    ▼                                     │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │               LAYER 5: ML CLASSIFICATION (On-Device)                │ │
│  │   TensorFlow Lite model bundled in APK                              │ │
│  │   Classifies: HUMAN | POSSIBLE_LIFE | NOISE | UNKNOWN               │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                    │                                     │
│              ┌─────────────────────┼─────────────────────┐              │
│              ▼                     ▼                     ▼              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐      │
│  │   LAYER 6A:      │  │   LAYER 6B:      │  │   LAYER 6C:      │      │
│  │   RADAR UI       │  │   GUARD/MESH     │  │   LOGGING        │      │
│  │   - Polar view   │  │   - Perimeter    │  │   - Events       │      │
│  │   - Targets      │  │   - NovaMesh     │  │   - Export       │      │
│  │   - Modes        │  │   - Alerts       │  │   - Encryption   │      │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: 26 (Android 8.0+)
- **Target SDK**: 34 (Android 14)
- **ML Runtime**: TensorFlow Lite
- **Database**: Room (SQLite)
- **Networking**: WiFi Direct, Bluetooth LE
- **Security**: Android Keystore, AES-256-GCM

---

## 5. Hardware Tiers

The app adapts to device capabilities automatically:

### Tier 1 - Any Android Phone (Minimum)
**Requirements**: Android 8.0+, any hardware

**Available Sensors**:
- WiFi RSSI scanning
- Bluetooth/BLE RSSI scanning
- Microphone (sonar)
- Camera (motion detection)
- Accelerometer
- Gyroscope

**Features**:
- Basic motion presence detection
- Fuzzy distance estimation
- Directional awareness (8 sectors)
- Full radar UI

### Tier 2 - Phones with UWB
**Requirements**: Android 12+, UWB hardware

**Additional Capabilities**:
- Ultra-Wideband ranging
- Precise distance (cm accuracy)
- Accurate angle of arrival
- Real radar-like functionality

**Examples**: Pixel 6 Pro+, Samsung Galaxy S21+, iPhone 11+ (if ported)

### Tier 3 - External Modules (Future)
**Hardware**: ESP32 + mmWave/ultrasonic sensors

**Connection**: Bluetooth or WiFi to phone

**Features**:
- Extended detection range
- Through-wall sensing
- Higher accuracy
- Multiple sensor nodes

---

## 6. Core Data Models

### Kotlin Data Classes

```kotlin
// ============================================
// CORE TARGET REPRESENTATION
// ============================================

/**
 * Represents a detected presence/target on the radar
 */
data class RadarTarget(
    val id: String = UUID.randomUUID().toString(),
    val angleDegrees: Float,           // 0-360 bearing from device
    val distanceMeters: Float?,        // null if unknown
    val confidence: Float,             // 0.0-1.0
    val type: TargetType,
    val velocity: Float? = null,       // m/s, null if unknown
    val isMoving: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val dataSources: Set<DataSource> = emptySet(),
    val signalStrength: Float? = null  // Combined signal strength
)

/**
 * Classification of detected targets
 */
enum class TargetType {
    HUMAN,           // High confidence human presence
    POSSIBLE_LIFE,   // Likely living but uncertain
    NOISE,           // Environmental noise/interference
    UNKNOWN,         // Unclassified detection
    VEHICLE,         // Large moving object (future)
    ANIMAL           // Non-human life form (future ML)
}

/**
 * Available sensor data sources
 */
enum class DataSource {
    WIFI,
    BLUETOOTH,
    SONAR,
    CAMERA,
    UWB,
    ACCELEROMETER,
    MAGNETOMETER,
    EXTERNAL_MODULE
}

// ============================================
// SENSOR READINGS
// ============================================

/**
 * Generic sensor reading wrapper
 */
data class SensorReading(
    val source: DataSource,
    val timestamp: Long = System.currentTimeMillis(),
    val values: FloatArray,
    val metadata: Map<String, Any> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SensorReading
        return source == other.source && 
               timestamp == other.timestamp && 
               values.contentEquals(other.values)
    }
    
    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + values.contentHashCode()
        return result
    }
}

/**
 * Bluetooth device reading with RSSI
 */
data class BleDeviceReading(
    val address: String,
    val name: String?,
    val rssi: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * WiFi access point reading
 */
data class WifiApReading(
    val bssid: String,
    val ssid: String?,
    val rssi: Int,
    val frequency: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// ============================================
// DETECTION EVENTS & LOGGING
// ============================================

/**
 * Detection event for logging and history
 */
data class DetectionEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val targets: List<RadarTarget>,
    val mode: OperatingMode,
    val deviceStable: Boolean,
    val batteryLevel: Int,
    val activeSensors: Set<DataSource>,
    val locationLabel: String? = null,  // e.g., "NORTH GATE"
    val encrypted: Boolean = false
)

// ============================================
// OPERATING MODES
// ============================================

/**
 * App operating modes
 */
enum class OperatingMode {
    NORMAL,      // Full features, standard battery usage
    EMERGENCY,   // Low power, minimal UI, extended runtime
    GUARD,       // Fixed position monitoring
    STEALTH,     // Silent operation, no emissions
    SEARCH,      // Aggressive scanning, high accuracy
    LAB,         // Debug mode with raw sensor data
    SENTRY       // Automated perimeter protection
}

/**
 * Emergency mode sub-profiles
 */
enum class EmergencyProfile {
    SILENT_SENTRY,  // No sounds, haptic only
    GUARDIAN,       // Full sensors (when charging)
    RECON,          // Walking mode, self-motion compensation
    BLACKOUT        // Minimum power, maximum stealth
}

// ============================================
// PERIMETER & ZONE CONFIGURATION
// ============================================

/**
 * Perimeter zone configuration
 */
data class PerimeterZone(
    val id: String = UUID.randomUUID().toString(),
    val name: String,                    // "NORTH GATE", "STAIRS 2F"
    val monitoringSector: Sector,
    val sensitivity: Sensitivity,
    val baselineCalibrated: Boolean = false,
    val baselineData: SensorBaseline? = null,
    val alertType: AlertType,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Monitoring sector options
 */
enum class Sector {
    FORWARD_CONE,    // ~90° front
    LEFT_SECTOR,     // ~90° left
    RIGHT_SECTOR,    // ~90° right
    REAR_SECTOR,     // ~90° back
    FRONT_WIDE,      // ~180° front
    FULL_360         // All directions
}

/**
 * Detection sensitivity levels
 */
enum class Sensitivity {
    LOW,      // Fewer false positives, may miss subtle movement
    MEDIUM,   // Balanced
    HIGH,     // More sensitive, more false positives
    CUSTOM    // User-defined thresholds
}

/**
 * Alert type options
 */
enum class AlertType {
    SOUND_AND_VIBRATION,
    VIBRATION_ONLY,
    SILENT_LOG_ONLY,
    VISUAL_ONLY,
    FLASH_AND_VIBRATION
}

/**
 * Sensor baseline for calibration
 */
data class SensorBaseline(
    val avgRssiVariance: Float,
    val avgSonarEnergy: Float,
    val avgCameraMotion: Float,
    val ambientNoiseLevel: Float,
    val calibrationTime: Long,
    val sampleCount: Int,
    val environmentType: String? = null  // "indoor", "outdoor", etc.
)

// ============================================
// ZONE STATUS
// ============================================

/**
 * Zone alert status
 */
enum class ZoneStatus {
    GREEN_CLEAR,       // No activity detected
    YELLOW_POSSIBLE,   // Low-level movement/noise
    RED_PRESENCE,      // Strong presence detected
    UNKNOWN            // Calibrating or error
}

// ============================================
// NOVAMESH NETWORKING
// ============================================

/**
 * Mesh node representation
 */
data class MeshNode(
    val nodeId: String,
    val nodeName: String,              // User-friendly name
    val zoneName: String,              // Assigned location label
    val lastStatus: ZoneStatus,
    val lastAngle: Float?,
    val lastConfidence: Float,
    val lastUpdate: Long,
    val connectionType: ConnectionType,
    val batteryLevel: Int?,
    val isOnline: Boolean = true,
    val signalStrength: Int? = null    // Connection quality
)

/**
 * Connection types for mesh networking
 */
enum class ConnectionType {
    WIFI_DIRECT,
    BLUETOOTH,
    LOCAL_HOTSPOT,
    WIRED_USB          // Future: USB OTG connection
}

/**
 * Compact mesh alert message (for transmission)
 */
data class MeshAlert(
    val nodeId: String,
    val time: Long,
    val zone: String,
    val level: ZoneStatus,
    val angle: Float?,
    val confidence: Float,
    val batteryLevel: Int? = null
)

// ============================================
// MODE PROFILES
// ============================================

/**
 * Complete mode profile configuration
 */
data class ModeProfile(
    val mode: OperatingMode,
    val scanIntervalMs: Long,
    val enabledSensors: Set<DataSource>,
    val uiBrightness: Float,           // 0.0-1.0
    val animationsEnabled: Boolean,
    val sonarEnabled: Boolean,
    val cameraEnabled: Boolean,
    val alertTypes: Set<AlertType>,
    val batteryThreshold: Int,         // Auto-downgrade below this %
    val description: String
)

/**
 * Predefined mode profiles
 */
object ModeProfiles {
    val NORMAL = ModeProfile(
        mode = OperatingMode.NORMAL,
        scanIntervalMs = 100,
        enabledSensors = DataSource.values().toSet(),
        uiBrightness = 1.0f,
        animationsEnabled = true,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION, AlertType.VISUAL_ONLY),
        batteryThreshold = 20,
        description = "Full features, standard battery usage"
    )
    
    val EMERGENCY = ModeProfile(
        mode = OperatingMode.EMERGENCY,
        scanIntervalMs = 2000,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.ACCELEROMETER),
        uiBrightness = 0.1f,
        animationsEnabled = false,
        sonarEnabled = false,
        cameraEnabled = false,
        alertTypes = setOf(AlertType.VIBRATION_ONLY),
        batteryThreshold = 5,
        description = "Maximum battery life, minimal visibility"
    )
    
    val GUARD = ModeProfile(
        mode = OperatingMode.GUARD,
        scanIntervalMs = 500,
        enabledSensors = DataSource.values().toSet(),
        uiBrightness = 0.3f,
        animationsEnabled = false,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION),
        batteryThreshold = 10,
        description = "Fixed position monitoring - plug in recommended"
    )
    
    val STEALTH = ModeProfile(
        mode = OperatingMode.STEALTH,
        scanIntervalMs = 1000,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH, DataSource.CAMERA),
        uiBrightness = 0.0f,
        animationsEnabled = false,
        sonarEnabled = false,  // No sound emission
        cameraEnabled = true,
        alertTypes = setOf(AlertType.VIBRATION_ONLY),
        batteryThreshold = 15,
        description = "Silent operation, no sound emissions"
    )
    
    val SEARCH = ModeProfile(
        mode = OperatingMode.SEARCH,
        scanIntervalMs = 50,
        enabledSensors = DataSource.values().toSet(),
        uiBrightness = 0.8f,
        animationsEnabled = true,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.SOUND_AND_VIBRATION, AlertType.VISUAL_ONLY),
        batteryThreshold = 30,
        description = "Aggressive scanning, highest accuracy, high battery"
    )
    
    val LAB = ModeProfile(
        mode = OperatingMode.LAB,
        scanIntervalMs = 100,
        enabledSensors = DataSource.values().toSet(),
        uiBrightness = 1.0f,
        animationsEnabled = true,
        sonarEnabled = true,
        cameraEnabled = true,
        alertTypes = setOf(AlertType.VISUAL_ONLY),
        batteryThreshold = 20,
        description = "Debug mode - shows raw sensor data and graphs"
    )
}
```

---

## 7. Offline-First Design

### Core Principles

Nova BioRadar is designed to work **completely offline**. The internet is treated as an optional bonus, never a requirement.

#### 1. All Processing Happens Locally
- Sensor reading and signal processing on-device
- Fusion engine runs locally
- No cloud API calls required
- No "must log in" requirements

#### 2. ML Models Bundled in APK
- TensorFlow Lite models included in app
- Models are small (<1MB each)
- No downloading or updates required
- Works immediately after install

#### 3. Local Storage Only
- SQLite database via Room
- Encrypted shared preferences
- Local file storage for logs
- No cloud sync required

#### 4. Network is Optional Bonus
When network IS available:
- Optional cloud backup of logs
- Firmware/model updates
- Map tile downloads for visualization
- Remote monitoring (future)

When network is NOT available:
- **Everything still works**
- Only cloud extras disappear
- Core radar functionality unchanged

### Implementation

```kotlin
/**
 * Capability detector - runs at app startup
 */
class CapabilityDetector(private val context: Context) {
    
    fun detectCapabilities(): DeviceCapabilities {
        return DeviceCapabilities(
            hasWifi = hasSystemFeature(PackageManager.FEATURE_WIFI),
            hasBluetooth = hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            hasBle = hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE),
            hasCamera = hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
            hasMicrophone = hasSystemFeature(PackageManager.FEATURE_MICROPHONE),
            hasAccelerometer = hasSensor(Sensor.TYPE_ACCELEROMETER),
            hasGyroscope = hasSensor(Sensor.TYPE_GYROSCOPE),
            hasMagnetometer = hasSensor(Sensor.TYPE_MAGNETIC_FIELD),
            hasUwb = checkUwbSupport(),
            androidVersion = Build.VERSION.SDK_INT,
            batteryCapacity = getBatteryCapacity(),
            hasWifiDirect = hasSystemFeature("android.hardware.wifi.direct"),
            hasWifiAware = hasSystemFeature("android.hardware.wifi.aware")
        )
    }
    
    private fun hasSystemFeature(feature: String): Boolean {
        return context.packageManager.hasSystemFeature(feature)
    }
    
    private fun hasSensor(type: Int): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(type) != null
    }
    
    private fun checkUwbSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            try {
                hasSystemFeature("android.hardware.uwb")
            } catch (e: Exception) {
                false
            }
        } else false
    }
    
    private fun getBatteryCapacity(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}

data class DeviceCapabilities(
    val hasWifi: Boolean,
    val hasBluetooth: Boolean,
    val hasBle: Boolean,
    val hasCamera: Boolean,
    val hasMicrophone: Boolean,
    val hasAccelerometer: Boolean,
    val hasGyroscope: Boolean,
    val hasMagnetometer: Boolean,
    val hasUwb: Boolean,
    val androidVersion: Int,
    val batteryCapacity: Int,
    val hasWifiDirect: Boolean,
    val hasWifiAware: Boolean
) {
    val tier: Int get() = when {
        hasUwb -> 2
        else -> 1
    }
}
```

---

## 8. Emergency & Field Modes

### Emergency Mode Design Philosophy

In emergencies (disasters, conflicts, blackouts), Nova BioRadar becomes critical survival equipment. The app must:

1. **Maximize battery life** - Run for hours/days
2. **Minimize visibility** - Dark UI, no sounds if needed
3. **Remain functional** - Core detection always works
4. **Be robust** - Handle crashes, restarts gracefully

### Mode Profiles

#### Normal Mode
- Full UI with animations
- All sensors active
- Fast scan rate (10Hz)
- Standard battery usage
- All alert types available

#### Emergency Mode
- Near-black UI theme
- Screen brightness minimal (10%)
- Scan rate reduced (0.5Hz)
- Only essential sensors
- Vibration-only alerts
- Extended battery life (8-12+ hours)

#### Guard Mode
- Designed for fixed position
- All sensors active
- Plugged-in recommended
- Automatic perimeter calibration
- Alert on any deviation from baseline

#### Stealth Mode
- **No sound emissions** (sonar disabled)
- Screen can go fully black
- Haptic-only feedback
- Passive sensors only (WiFi/BT/Camera)
- Invisible operation

#### Search Mode
- Maximum scanning frequency
- All sensors at full power
- Highest accuracy mode
- Aggressive battery usage
- For active searching operations

#### Lab Mode
- Raw sensor data displayed
- Signal graphs and charts
- Debug information
- For development and calibration

### Emergency Sub-Profiles

```kotlin
/**
 * Emergency mode configurations
 */
object EmergencyProfiles {
    
    /**
     * Silent Sentry - Maximum stealth
     * No sounds, no visible animations, haptic only
     */
    val SILENT_SENTRY = EmergencyConfig(
        name = "Silent Sentry",
        sonarEnabled = false,
        screenWake = ScreenWakeMode.ON_ALERT_ONLY,
        alertMode = AlertType.VIBRATION_ONLY,
        scanIntervalMs = 2000,
        uiBrightness = 0.0f,
        keepScreenOff = true
    )
    
    /**
     * Guardian - Full protection when charging
     * All sensors, high accuracy, plug in required
     */
    val GUARDIAN = EmergencyConfig(
        name = "Guardian",
        sonarEnabled = true,
        screenWake = ScreenWakeMode.ALWAYS_ON,
        alertMode = AlertType.SOUND_AND_VIBRATION,
        scanIntervalMs = 200,
        uiBrightness = 0.3f,
        requiresCharging = true
    )
    
    /**
     * Recon - Walking mode
     * Compensates for user movement, reduced false positives
     */
    val RECON = EmergencyConfig(
        name = "Recon",
        sonarEnabled = true,
        screenWake = ScreenWakeMode.PULSE,
        alertMode = AlertType.VIBRATION_ONLY,
        scanIntervalMs = 500,
        uiBrightness = 0.2f,
        selfMotionCompensation = true
    )
    
    /**
     * Blackout - Minimum power consumption
     * Only passive radio scanning, maximum battery life
     */
    val BLACKOUT = EmergencyConfig(
        name = "Blackout",
        sonarEnabled = false,
        cameraEnabled = false,
        screenWake = ScreenWakeMode.ON_ALERT_ONLY,
        alertMode = AlertType.VIBRATION_ONLY,
        scanIntervalMs = 5000,
        uiBrightness = 0.0f,
        keepScreenOff = true,
        enabledSensors = setOf(DataSource.WIFI, DataSource.BLUETOOTH)
    )
}

data class EmergencyConfig(
    val name: String,
    val sonarEnabled: Boolean = true,
    val cameraEnabled: Boolean = true,
    val screenWake: ScreenWakeMode = ScreenWakeMode.ALWAYS_ON,
    val alertMode: AlertType = AlertType.SOUND_AND_VIBRATION,
    val scanIntervalMs: Long = 1000,
    val uiBrightness: Float = 0.5f,
    val keepScreenOff: Boolean = false,
    val selfMotionCompensation: Boolean = false,
    val requiresCharging: Boolean = false,
    val enabledSensors: Set<DataSource> = DataSource.values().toSet()
)

enum class ScreenWakeMode {
    ALWAYS_ON,      // Screen always visible
    PULSE,          // Periodic wake (every N seconds)
    ON_ALERT_ONLY,  // Wake only when alert triggered
    NEVER           // Screen always off
}
```

### Battery Management

```kotlin
/**
 * Intelligent battery management
 */
class BatteryManager(private val context: Context) {
    
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    
    /**
     * Get current battery level (0-100)
     */
    fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Check if device is charging
     */
    fun isCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
               status == BatteryManager.BATTERY_STATUS_FULL
    }
    
    /**
     * Estimate remaining monitoring time based on current mode
     */
    fun estimateRemainingTime(mode: OperatingMode): Duration {
        val batteryLevel = getBatteryLevel()
        val drainRatePerHour = when (mode) {
            OperatingMode.NORMAL -> 15      // ~6-7 hours
            OperatingMode.EMERGENCY -> 3    // ~30+ hours
            OperatingMode.GUARD -> 12       // ~8 hours
            OperatingMode.STEALTH -> 5      // ~20 hours
            OperatingMode.SEARCH -> 25      // ~4 hours
            OperatingMode.LAB -> 20         // ~5 hours
            OperatingMode.SENTRY -> 8       // ~12 hours
        }
        val hoursRemaining = batteryLevel.toFloat() / drainRatePerHour
        return Duration.ofMinutes((hoursRemaining * 60).toLong())
    }
    
    /**
     * Get recommended mode based on battery level
     */
    fun getRecommendedMode(): OperatingMode {
        val level = getBatteryLevel()
        val charging = isCharging()
        
        return when {
            charging -> OperatingMode.GUARD
            level > 50 -> OperatingMode.NORMAL
            level > 30 -> OperatingMode.STEALTH
            level > 15 -> OperatingMode.EMERGENCY
            else -> OperatingMode.EMERGENCY // Force emergency below 15%
        }
    }
    
    /**
     * Auto-downgrade mode when battery is critical
     */
    fun shouldAutoDowngrade(currentMode: OperatingMode): OperatingMode? {
        val level = getBatteryLevel()
        val profile = ModeProfiles.entries.find { it.mode == currentMode }
        
        if (level < (profile?.batteryThreshold ?: 20)) {
            return OperatingMode.EMERGENCY
        }
        return null
    }
}
```

---

## 9. Perimeter Guard System

### Overview

The Perimeter Guard System turns any phone into an autonomous sentry that monitors an area and alerts on detected presence. This works **completely offline**.

### How It Works

1. **Place Phone** - Position device facing the area to monitor
2. **Calibrate Baseline** - 30-60 second calibration records "normal"
3. **Define Zone** - Choose monitoring sector and sensitivity
4. **Activate Guard** - Continuous monitoring begins
5. **Alert on Detection** - Alarm when presence exceeds threshold

### Zone Configuration

```kotlin
/**
 * Perimeter Guard implementation
 */
class PerimeterGuard(
    private val fusionEngine: FusionEngine,
    private val alertManager: AlertManager,
    private val logRepository: LogRepository
) {
    private var baseline: SensorBaseline? = null
    private var zone: PerimeterZone? = null
    private var isActive = false
    private var guardJob: Job? = null
    
    /**
     * Calibrate baseline - records "empty" environment
     * Should be run with no one in monitored area
     */
    suspend fun calibrate(durationMs: Long = 30000): SensorBaseline {
        val readings = mutableListOf<SensorReading>()
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < durationMs) {
            readings.addAll(fusionEngine.collectRawReadings())
            delay(100)
        }
        
        baseline = SensorBaseline(
            avgRssiVariance = calculateAvgVariance(readings, DataSource.BLUETOOTH),
            avgSonarEnergy = calculateAvgEnergy(readings, DataSource.SONAR),
            avgCameraMotion = calculateAvgMotion(readings, DataSource.CAMERA),
            ambientNoiseLevel = calculateNoiseLevel(readings),
            calibrationTime = System.currentTimeMillis(),
            sampleCount = readings.size
        )
        
        return baseline!!
    }
    
    /**
     * Configure the monitoring zone
     */
    fun configureZone(config: PerimeterZone) {
        this.zone = config
    }
    
    /**
     * Start guarding the perimeter
     */
    fun startGuarding(scope: CoroutineScope) {
        require(baseline != null) { "Must calibrate before guarding" }
        require(zone != null) { "Must configure zone before guarding" }
        
        isActive = true
        
        guardJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val currentReadings = fusionEngine.collectRawReadings()
                val deviation = calculateDeviation(currentReadings, baseline!!)
                val status = evaluateStatus(deviation)
                
                if (status != ZoneStatus.GREEN_CLEAR) {
                    handleAlert(status, deviation)
                }
                
                delay(zone!!.getScanInterval())
            }
        }
    }
    
    /**
     * Stop guarding
     */
    fun stopGuarding() {
        isActive = false
        guardJob?.cancel()
    }
    
    private fun evaluateStatus(deviation: Float): ZoneStatus {
        val thresholds = zone!!.getThresholds()
        return when {
            deviation > thresholds.red -> ZoneStatus.RED_PRESENCE
            deviation > thresholds.yellow -> ZoneStatus.YELLOW_POSSIBLE
            else -> ZoneStatus.GREEN_CLEAR
        }
    }
    
    private suspend fun handleAlert(status: ZoneStatus, deviation: Float) {
        // Log the event
        val event = DetectionEvent(
            targets = fusionEngine.getTargets(),
            mode = OperatingMode.GUARD,
            deviceStable = true,
            batteryLevel = getBatteryLevel(),
            activeSensors = zone!!.activeSensors,
            locationLabel = zone!!.name
        )
        logRepository.logEvent(event)
        
        // Trigger alert based on zone configuration
        alertManager.triggerAlert(status, zone!!.alertType)
    }
}

/**
 * Alert thresholds configuration
 */
data class AlertThresholds(
    val yellow: Float,  // Possible movement threshold
    val red: Float      // Confirmed presence threshold
)

/**
 * Extension to get thresholds from sensitivity
 */
fun Sensitivity.getThresholds(): AlertThresholds {
    return when (this) {
        Sensitivity.LOW -> AlertThresholds(yellow = 40f, red = 70f)
        Sensitivity.MEDIUM -> AlertThresholds(yellow = 25f, red = 50f)
        Sensitivity.HIGH -> AlertThresholds(yellow = 15f, red = 35f)
        Sensitivity.CUSTOM -> AlertThresholds(yellow = 20f, red = 45f)
    }
}
```

### Alert Manager

```kotlin
/**
 * Manages alerts and notifications
 */
class AlertManager(private val context: Context) {
    
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var mediaPlayer: MediaPlayer? = null
    
    /**
     * Trigger an alert based on zone status and type
     */
    fun triggerAlert(status: ZoneStatus, type: AlertType) {
        when (type) {
            AlertType.SOUND_AND_VIBRATION -> {
                playAlarmSound(status)
                vibratePattern(status)
            }
            AlertType.VIBRATION_ONLY -> {
                vibratePattern(status)
            }
            AlertType.SILENT_LOG_ONLY -> {
                // Just log, no physical alert
            }
            AlertType.VISUAL_ONLY -> {
                // Flash screen or LED (handled by UI)
            }
            AlertType.FLASH_AND_VIBRATION -> {
                flashScreen()
                vibratePattern(status)
            }
        }
    }
    
    private fun vibratePattern(status: ZoneStatus) {
        val pattern = when (status) {
            ZoneStatus.RED_PRESENCE -> longArrayOf(0, 500, 200, 500, 200, 500)
            ZoneStatus.YELLOW_POSSIBLE -> longArrayOf(0, 200, 200, 200)
            else -> longArrayOf(0, 100)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    private fun playAlarmSound(status: ZoneStatus) {
        val soundRes = when (status) {
            ZoneStatus.RED_PRESENCE -> R.raw.alert_red
            ZoneStatus.YELLOW_POSSIBLE -> R.raw.alert_yellow
            else -> return
        }
        
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundRes)
        mediaPlayer?.start()
    }
    
    private fun flashScreen() {
        // Handled by UI layer via broadcast
        val intent = Intent("com.nova.bioradar.FLASH_ALERT")
        context.sendBroadcast(intent)
    }
    
    /**
     * Stop any ongoing alerts
     */
    fun stopAlert() {
        vibrator.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
```

### Tripwire Mode

For specific entry points (doorways, hallways, paths):

```kotlin
/**
 * Tripwire mode - binary presence detection
 */
class TripwireGuard(
    private val perimeterGuard: PerimeterGuard
) {
    /**
     * Quick setup for doorway monitoring
     */
    fun setupDoorway(name: String = "Doorway") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FORWARD_CONE,
            sensitivity = Sensitivity.HIGH,
            alertType = AlertType.VIBRATION_ONLY
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for hallway monitoring
     */
    fun setupHallway(name: String = "Hallway") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FRONT_WIDE,
            sensitivity = Sensitivity.MEDIUM,
            alertType = AlertType.SOUND_AND_VIBRATION
        )
        perimeterGuard.configureZone(zone)
    }
    
    /**
     * Quick setup for room monitoring
     */
    fun setupRoom(name: String = "Room") {
        val zone = PerimeterZone(
            name = name,
            monitoringSector = Sector.FULL_360,
            sensitivity = Sensitivity.LOW,
            alertType = AlertType.SOUND_AND_VIBRATION
        )
        perimeterGuard.configureZone(zone)
    }
}
```

---

## 10. NovaMesh - Multi-Device Networking

### Overview

NovaMesh allows multiple phones running Nova BioRadar to form a distributed sensor network - **completely offline**. No internet, no cell towers, just device-to-device communication.

### Network Topology

```
┌─────────────────────────────────────────────────────────────────┐
│                      NOVAMESH TOPOLOGY                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                        ┌─────────────┐                          │
│                        │   HUB/BASE  │                          │
│                        │  (Plugged   │                          │
│                        │    In)      │                          │
│                        └──────┬──────┘                          │
│                               │                                  │
│              ┌────────────────┼────────────────┐                │
│              │                │                │                │
│              ▼                ▼                ▼                │
│       ┌──────────┐     ┌──────────┐     ┌──────────┐           │
│       │  NODE 1  │     │  NODE 2  │     │  NODE 3  │           │
│       │ "NORTH   │     │ "STAIRS  │     │ "EAST    │           │
│       │  GATE"   │     │   2F"    │     │  ROOM"   │           │
│       └──────────┘     └──────────┘     └──────────┘           │
│                                                                  │
│  Connection Options:                                            │
│  • Wi-Fi Direct (P2P, no router)                               │
│  • Local Hotspot (hub creates AP)                              │
│  • Bluetooth mesh                                               │
│                                                                  │
│  ═══════════════════════════════════════════════════════════   │
│  ║           NO INTERNET REQUIRED                          ║   │
│  ═══════════════════════════════════════════════════════════   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Implementation

```kotlin
/**
 * NovaMesh networking manager
 */
class NovaMeshManager(
    private val context: Context,
    private val alertHandler: (MeshAlert) -> Unit
) {
    private var role: MeshRole = MeshRole.STANDALONE
    private val connectedNodes = mutableMapOf<String, MeshNode>()
    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    
    enum class MeshRole {
        STANDALONE,  // Not connected to mesh
        HUB,         // Central coordinator
        NODE         // Remote sensor node
    }
    
    /**
     * Initialize as hub (creates local network)
     */
    fun initializeAsHub(scope: CoroutineScope) {
        role = MeshRole.HUB
        
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager?.initialize(context, Looper.getMainLooper(), null)
        
        // Create Wi-Fi Direct group (acts as AP)
        wifiP2pManager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                startListeningForNodes(scope)
            }
            override fun onFailure(reason: Int) {
                // Fallback to Bluetooth
                initializeBluetoothHub(scope)
            }
        })
    }
    
    /**
     * Initialize as node (connects to hub)
     */
    fun initializeAsNode(hubAddress: String, scope: CoroutineScope) {
        role = MeshRole.NODE
        connectToHub(hubAddress, scope)
    }
    
    /**
     * Send alert to hub (from node)
     */
    fun sendAlert(alert: MeshAlert) {
        if (role != MeshRole.NODE) return
        
        val json = serializeAlert(alert)
        sendToHub(json)
    }
    
    /**
     * Broadcast alert to all nodes (from hub)
     */
    fun broadcastAlert(alert: MeshAlert) {
        if (role != MeshRole.HUB) return
        
        val json = serializeAlert(alert)
        connectedNodes.keys.forEach { nodeId ->
            sendToNode(nodeId, json)
        }
    }
    
    /**
     * Get network status
     */
    fun getNetworkStatus(): NetworkStatus {
        return NetworkStatus(
            role = role,
            connectedNodes = connectedNodes.values.toList(),
            isHealthy = checkNetworkHealth()
        )
    }
    
    private fun startListeningForNodes(scope: CoroutineScope) {
        isRunning = true
        
        scope.launch(Dispatchers.IO) {
            serverSocket = ServerSocket(MESH_PORT)
            
            while (isRunning) {
                try {
                    val clientSocket = serverSocket?.accept() ?: continue
                    handleNodeConnection(clientSocket, scope)
                } catch (e: Exception) {
                    if (isRunning) {
                        delay(1000) // Retry after error
                    }
                }
            }
        }
    }
    
    private fun handleNodeConnection(socket: Socket, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            
            while (isRunning && !socket.isClosed) {
                try {
                    val message = reader.readLine() ?: break
                    val alert = deserializeAlert(message)
                    processNodeAlert(alert)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }
    
    private fun processNodeAlert(alert: MeshAlert) {
        // Update node status
        connectedNodes[alert.nodeId] = MeshNode(
            nodeId = alert.nodeId,
            nodeName = alert.nodeId,
            zoneName = alert.zone,
            lastStatus = alert.level,
            lastAngle = alert.angle,
            lastConfidence = alert.confidence,
            lastUpdate = alert.time,
            connectionType = ConnectionType.WIFI_DIRECT,
            batteryLevel = alert.batteryLevel
        )
        
        // Forward to handler
        alertHandler(alert)
    }
    
    /**
     * Shutdown mesh networking
     */
    fun shutdown() {
        isRunning = false
        serverSocket?.close()
        wifiP2pManager?.removeGroup(channel, null)
    }
    
    companion object {
        const val MESH_PORT = 19420
    }
}

data class NetworkStatus(
    val role: NovaMeshManager.MeshRole,
    val connectedNodes: List<MeshNode>,
    val isHealthy: Boolean
)
```

### Mapless Operation - Named Posts

Instead of GPS (which may be blocked or unavailable), use named locations:

```kotlin
/**
 * Location labeling system (GPS-independent)
 */
class LocationManager {
    private val namedLocations = mutableMapOf<String, NamedLocation>()
    
    data class NamedLocation(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val description: String? = null,
        val assignedNodeId: String? = null,
        val createdAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Add a new named location
     */
    fun addLocation(name: String, description: String? = null): NamedLocation {
        val location = NamedLocation(name = name, description = description)
        namedLocations[location.id] = location
        return location
    }
    
    /**
     * Assign a mesh node to a location
     */
    fun assignNodeToLocation(locationId: String, nodeId: String) {
        namedLocations[locationId]?.let {
            namedLocations[locationId] = it.copy(assignedNodeId = nodeId)
        }
    }
    
    /**
     * Get all locations with their current status
     */
    fun getLocationStatuses(meshManager: NovaMeshManager): List<LocationStatus> {
        val nodes = meshManager.getNetworkStatus().connectedNodes
        
        return namedLocations.values.map { location ->
            val node = nodes.find { it.nodeId == location.assignedNodeId }
            LocationStatus(
                location = location,
                status = node?.lastStatus ?: ZoneStatus.UNKNOWN,
                lastUpdate = node?.lastUpdate,
                isOnline = node?.isOnline ?: false
            )
        }
    }
    
    /**
     * Preset location templates
     */
    fun getPresetLocations(): List<String> = listOf(
        "NORTH GATE",
        "SOUTH GATE",
        "EAST ENTRANCE",
        "WEST ENTRANCE",
        "STAIRS 1F",
        "STAIRS 2F",
        "STAIRS 3F",
        "MAIN HALLWAY",
        "BACK DOOR",
        "ROOF ACCESS",
        "BASEMENT",
        "GARAGE",
        "PERIMETER NORTH",
        "PERIMETER SOUTH",
        "PERIMETER EAST",
        "PERIMETER WEST",
        "GUARD POST 1",
        "GUARD POST 2",
        "COMMAND CENTER"
    )
}

data class LocationStatus(
    val location: LocationManager.NamedLocation,
    val status: ZoneStatus,
    val lastUpdate: Long?,
    val isOnline: Boolean
)
```

---

## 11. Security & Privacy

### Data Encryption

```kotlin
class SecureStorage(private val context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context, "nova_secure_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun encryptLog(event: DetectionEvent): ByteArray {
        val json = Json.encodeToString(event)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(json.toByteArray())
        return iv + encrypted
    }
}
```

### Panic Wipe
```kotlin
class PanicWipe(private val context: Context, private val logRepository: LogRepository) {
    fun executeWipe() {
        logRepository.getAllLogFiles().forEach { file ->
            overwriteWithRandom(file)
            file.delete()
        }
        logRepository.deleteAllLogs()
    }
    
    private fun overwriteWithRandom(file: File) {
        val random = SecureRandom()
        repeat(3) {
            RandomAccessFile(file, "rw").use { raf ->
                val buffer = ByteArray(4096)
                var remaining = file.length()
                while (remaining > 0) {
                    random.nextBytes(buffer)
                    raf.write(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                    remaining -= buffer.size
                }
            }
        }
    }
}
```

### Privacy Guardrails
- **No identity tracking** - No face recognition, no personal data
- **No MAC address logging** tied to individuals
- **Encrypted logs** - PIN/passphrase protected
- **Clear disclaimers** - "For presence detection and safety only"
- **Visible indicators** when sensors are active

---

## 12. Phased Development Roadmap

### Phase 0: Foundation (Weeks 1-2)
- [ ] Create Android Studio project
- [ ] Set up Kotlin + Jetpack Compose
- [ ] Configure permissions in AndroidManifest.xml
- [ ] Create package structure
- [ ] Implement CapabilityDetector
- [ ] Basic "Hello Radar" screen

### Phase 1: Radio Presence Detection (Weeks 3-4)
- [ ] BluetoothScanner implementation
- [ ] WifiScanner implementation
- [ ] RssiAnalyzer for variance calculation
- [ ] RadioActivityAnalyzer
- [ ] Basic presence bar UI

### Phase 2: Audio Sonar (Weeks 5-6)
- [ ] AudioEmitter (18kHz ping)
- [ ] AudioRecorder
- [ ] FFTProcessor
- [ ] SonarProcessor
- [ ] Front-cone detection UI

### Phase 3: Camera Motion (Weeks 7-8)
- [ ] CameraMotionDriver with CameraX
- [ ] Optical flow processor
- [ ] Sector-based motion analysis
- [ ] 8-sector radar visualization

### Phase 4: UWB Integration (Weeks 9-10)
- [ ] UwbRadarDriver (Android 12+)
- [ ] UwbTargetTracker
- [ ] Distance rings UI
- [ ] Precise angle display

### Phase 5: Fusion Engine & ML (Weeks 11-14)
- [ ] FusionEngine combining all sources
- [ ] FeatureExtractor
- [ ] TensorFlow Lite model integration
- [ ] Adaptive threshold tuning

### Phase 6: Polished Radar UI (Weeks 15-16)
- [ ] Circular radar with sweep animation
- [ ] Target rendering (glowing dots)
- [ ] Mode selector
- [ ] Details panel
- [ ] Log viewer

### Phase 7: Emergency & Guard Modes (Weeks 17-18)
- [ ] Emergency mode profiles
- [ ] Perimeter Guard system
- [ ] Baseline calibration
- [ ] Alert manager
- [ ] Battery-aware scanning

### Phase 8: NovaMesh (Weeks 19-22)
- [ ] WiFi Direct networking
- [ ] Bluetooth fallback
- [ ] Hub/Node architecture
- [ ] Named locations system
- [ ] Multi-device UI

---

## 13. Package Structure

```
com.nova.bioradar/
├── MainActivity.kt
├── NovaApplication.kt
├── core/
│   ├── sensors/
│   │   ├── BluetoothScanner.kt
│   │   ├── WifiScanner.kt
│   │   ├── AudioSonarDriver.kt
│   │   ├── CameraMotionDriver.kt
│   │   ├── MotionSensorDriver.kt
│   │   └── UwbRadarDriver.kt
│   ├── signal/
│   │   ├── FFTProcessor.kt
│   │   ├── RssiAnalyzer.kt
│   │   ├── NoiseFilter.kt
│   │   └── DopplerDetector.kt
│   ├── fusion/
│   │   ├── FusionEngine.kt
│   │   ├── TargetTracker.kt
│   │   └── SectorAnalyzer.kt
│   └── ml/
│       ├── ClassifierModel.kt
│       └── FeatureExtractor.kt
├── modes/
│   ├── ModeManager.kt
│   ├── EmergencyMode.kt
│   ├── GuardMode.kt
│   └── PerimeterGuard.kt
├── mesh/
│   ├── NovaMeshManager.kt
│   ├── LocationManager.kt
│   └── MeshProtocol.kt
├── security/
│   ├── SecureStorage.kt
│   ├── PanicWipe.kt
│   └── PrivacyManager.kt
├── ui/
│   ├── radar/
│   │   ├── RadarScreen.kt
│   │   ├── RadarView.kt
│   │   └── TargetRenderer.kt
│   ├── guard/
│   │   ├── GuardScreen.kt
│   │   └── CalibrationScreen.kt
│   ├── mesh/
│   │   ├── MeshScreen.kt
│   │   └── NodeListView.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   └── theme/
│       ├── Theme.kt
│       └── Color.kt
└── data/
    ├── local/
    │   ├── NovaDatabase.kt
    │   └── DetectionDao.kt
    └── repository/
        └── LogRepository.kt
```

---

## 14. AndroidManifest Permissions

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Network -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />
    
    <!-- Bluetooth -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    
    <!-- Sensors -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
    <!-- Background -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <!-- UWB -->
    <uses-permission android:name="android.permission.UWB_RANGING" />
</manifest>
```

---

## 15. Ethics & Legal

### This Tool Is For:
- Personal security awareness
- Search & rescue training
- Perimeter monitoring
- Educational exploration

### This Tool Is NOT For:
- Stalking or surveillance
- Weapons targeting
- Identity tracking
- Privacy invasion

### Built-In Safeguards:
- No face recognition
- No personal data storage
- Visible sensor indicators
- Clear user disclaimers
- Encrypted, wipeable logs

---

## 16. Summary

Nova BioRadar transforms any Android phone into a futuristic presence detection radar:

| Feature | Description |
|---------|-------------|
| **Sensors** | WiFi, Bluetooth, Sonar, Camera, UWB |
| **Offline** | Works without internet |
| **Modes** | Normal, Emergency, Guard, Stealth |
| **Mesh** | Multi-phone networking |
| **Security** | Encrypted logs, panic wipe |
| **Universal** | Any Android 8.0+ device |

**Build the future. Detect the present. Stay safe.**

---

*Nova BioRadar - All-in-One Autonomous Development Guide v1.0*
