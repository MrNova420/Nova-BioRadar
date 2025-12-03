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

## 17. Extended Range Research & Implementation (5-50m+)

### 17.1 Range Improvement Goals

The primary objective is to achieve reliable detection at **5-10 meters minimum**, with stretch goals of **20-50+ meters** using advanced techniques. The system must automatically adapt to each device's capabilities.

#### Target Range Specifications

| Detection Method | Current Range | Target Range | Stretch Goal |
|-----------------|---------------|--------------|--------------|
| WiFi CSI | 3-5m | 10-15m | 30m+ |
| Bluetooth 5.0+ | 10m | 30m | 50m |
| Audio Sonar | 3-5m | 8-12m | 15m |
| Multi-Frequency Sonar | N/A | 10-15m | 20m |
| Camera AI | 5m | 15m | 30m |
| UWB | 10m | 50m | 100m |
| WiFi RTT | N/A | 15m | 30m |
| Sensor Fusion | 5m | 15-20m | 50m+ |

---

### 17.2 WiFi Channel State Information (CSI) Detection

#### Overview
WiFi CSI exploits the fine-grained channel information from WiFi signals to detect human presence and movement through walls. Unlike simple RSSI, CSI provides amplitude and phase information for each subcarrier.

#### How It Works

```
┌─────────────────────────────────────────────────────────────────────┐
│                    WiFi CSI Detection Pipeline                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│   WiFi Access Point ──► Radio Waves ──► Human Body (reflects) ──►  │
│                                                                      │
│   Phone Receiver ──► CSI Extraction ──► Signal Processing ──►      │
│                                                                      │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  CSI Matrix (per packet):                                    │   │
│   │  [Subcarrier 1: amplitude, phase]                           │   │
│   │  [Subcarrier 2: amplitude, phase]                           │   │
│   │  [...]                                                       │   │
│   │  [Subcarrier N: amplitude, phase]  (N = 52-256 depending)   │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
│   Variance Analysis ──► Movement Detection ──► Presence Alert       │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

#### Implementation Strategy

```kotlin
/**
 * WiFi CSI-based presence detection
 * Requires: Android 11+ with WiFi RTT support, or rooted device with Nexmon CSI
 */
class WifiCsiDetector(private val context: Context) {
    
    private val csiBuffer = CircularBuffer<CsiReading>(1000)
    private var baselineVariance: FloatArray? = null
    
    data class CsiReading(
        val timestamp: Long,
        val subcarrierAmplitudes: FloatArray,
        val subcarrierPhases: FloatArray,
        val rssi: Int
    )
    
    /**
     * Extract CSI-like features from available WiFi APIs
     * Uses WiFi RTT (Round Trip Time) as CSI proxy on standard Android
     */
    suspend fun collectCsiFeatures(): CsiFeatures {
        val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) 
            as? WifiRttManager
        
        return if (wifiRttManager?.isAvailable == true) {
            collectWifiRttFeatures(wifiRttManager)
        } else {
            collectRssiBasedFeatures()
        }
    }
    
    /**
     * WiFi RTT-based ranging (Android 9+, WiFi RTT capable APs)
     * Provides distance estimates with ~1-2m accuracy
     */
    private suspend fun collectWifiRttFeatures(manager: WifiRttManager): CsiFeatures {
        val scanResults = getWifiScanResults()
        val rttCapableAps = scanResults.filter { it.is80211mcResponder }
        
        if (rttCapableAps.isEmpty()) {
            return collectRssiBasedFeatures()
        }
        
        val rangingRequest = RangingRequest.Builder()
            .addAccessPoints(rttCapableAps)
            .build()
        
        return suspendCancellableCoroutine { continuation ->
            manager.startRanging(rangingRequest, context.mainExecutor,
                object : RangingResultCallback() {
                    override fun onRangingResults(results: List<RangingResult>) {
                        val features = processRangingResults(results)
                        continuation.resume(features)
                    }
                    override fun onRangingFailure(code: Int) {
                        continuation.resume(CsiFeatures.empty())
                    }
                })
        }
    }
    
    /**
     * Detect presence through signal variance analysis
     * Human movement causes predictable CSI/RSSI pattern changes
     */
    fun detectPresence(currentReading: CsiReading): PresenceResult {
        csiBuffer.add(currentReading)
        
        if (csiBuffer.size < 100) {
            return PresenceResult(detected = false, calibrating = true)
        }
        
        val recentVariance = calculateVariance(csiBuffer.takeLast(50))
        val historicalVariance = baselineVariance ?: calculateVariance(csiBuffer.take(50))
        
        // Breathing detection: 0.1-0.5 Hz oscillation
        val breathingComponent = extractFrequencyComponent(csiBuffer, 0.1f, 0.5f)
        
        // Movement detection: 0.5-5 Hz changes
        val movementComponent = extractFrequencyComponent(csiBuffer, 0.5f, 5.0f)
        
        // Walking detection: characteristic Doppler pattern
        val walkingPattern = detectWalkingPattern(csiBuffer)
        
        val presenceScore = calculatePresenceScore(
            varianceRatio = recentVariance / (historicalVariance + 0.001f),
            breathingEnergy = breathingComponent,
            movementEnergy = movementComponent,
            walkingConfidence = walkingPattern
        )
        
        return PresenceResult(
            detected = presenceScore > PRESENCE_THRESHOLD,
            confidence = presenceScore,
            estimatedDistance = estimateDistanceFromCsi(currentReading),
            isMoving = movementComponent > MOVEMENT_THRESHOLD,
            isBreathing = breathingComponent > BREATHING_THRESHOLD
        )
    }
    
    /**
     * Through-wall detection using multipath analysis
     * Walls attenuate but don't block WiFi - we can detect presence behind them
     */
    fun detectThroughWall(readings: List<CsiReading>): ThroughWallResult {
        // Analyze multipath propagation
        val multipathProfile = analyzeMultipath(readings)
        
        // Look for human-caused reflections
        val humanReflections = multipathProfile.filter { path ->
            path.delay in HUMAN_REFLECTION_DELAY_RANGE &&
            path.dopplerShift in HUMAN_DOPPLER_RANGE
        }
        
        // Estimate position from multiple reflection paths
        val estimatedPosition = triangulateFromMultipath(humanReflections)
        
        return ThroughWallResult(
            detected = humanReflections.isNotEmpty(),
            confidence = calculateThroughWallConfidence(humanReflections),
            estimatedDistance = estimatedPosition?.distance,
            estimatedAngle = estimatedPosition?.angle,
            wallAttenuation = calculateWallAttenuation(readings)
        )
    }
    
    companion object {
        const val PRESENCE_THRESHOLD = 0.6f
        const val MOVEMENT_THRESHOLD = 0.3f
        const val BREATHING_THRESHOLD = 0.2f
        val HUMAN_REFLECTION_DELAY_RANGE = 10L..500L // nanoseconds
        val HUMAN_DOPPLER_RANGE = -5f..5f // Hz
    }
}

data class CsiFeatures(
    val amplitudeVariance: Float,
    val phaseVariance: Float,
    val rssiVariance: Float,
    val rttDistance: Float?,
    val rttVariance: Float?
) {
    companion object {
        fun empty() = CsiFeatures(0f, 0f, 0f, null, null)
    }
}

data class PresenceResult(
    val detected: Boolean,
    val confidence: Float = 0f,
    val estimatedDistance: Float? = null,
    val isMoving: Boolean = false,
    val isBreathing: Boolean = false,
    val calibrating: Boolean = false
)

data class ThroughWallResult(
    val detected: Boolean,
    val confidence: Float,
    val estimatedDistance: Float?,
    val estimatedAngle: Float?,
    val wallAttenuation: Float
)
```

#### Range Enhancement Techniques

1. **Multi-AP Triangulation**
   - Use signals from 3+ access points
   - Triangulate position using RTT or RSSI
   - Achievable range: 15-30m indoors

2. **Subcarrier Analysis**
   - Analyze individual WiFi subcarriers (requires root/custom firmware)
   - Detect micro-movements through phase changes
   - Can detect breathing at 5-8m through walls

3. **Machine Learning Classification**
   - Train models on CSI patterns for different activities
   - Distinguish human vs pet vs object movement
   - Improve accuracy from 70% to 90%+

---

### 17.3 Advanced Audio Sonar Techniques

#### Multi-Frequency FMCW Sonar

Traditional single-frequency sonar has limited range. Frequency-Modulated Continuous Wave (FMCW) sonar dramatically improves range and accuracy.

```kotlin
/**
 * FMCW (Frequency Modulated Continuous Wave) Sonar
 * Achieves 10-20m range vs 3-5m for single frequency
 */
class FmcwSonarProcessor(private val context: Context) {
    
    private val sampleRate = 48000
    private val chirpDuration = 0.1f // 100ms
    private val startFrequency = 17000f // 17 kHz
    private val endFrequency = 22000f // 22 kHz (sweep range)
    private val fftSize = 4096
    
    /**
     * Generate FMCW chirp signal
     * Frequency sweeps from startFreq to endFreq over chirpDuration
     */
    fun generateChirp(): ShortArray {
        val numSamples = (sampleRate * chirpDuration).toInt()
        val chirp = ShortArray(numSamples)
        
        val chirpRate = (endFrequency - startFrequency) / chirpDuration
        
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val instantFreq = startFrequency + chirpRate * t
            val phase = 2 * PI * (startFrequency * t + 0.5f * chirpRate * t * t)
            chirp[i] = (Short.MAX_VALUE * sin(phase)).toInt().toShort()
        }
        
        return chirp
    }
    
    /**
     * Process received echo using matched filter
     * Returns distance and velocity estimates
     */
    fun processEcho(transmitted: ShortArray, received: ShortArray): FmcwResult {
        // Cross-correlation (matched filter)
        val correlation = crossCorrelate(transmitted, received)
        
        // Find peaks in correlation - each peak is a reflection
        val peaks = findPeaks(correlation, threshold = 0.3f)
        
        // Convert peak positions to distances
        val targets = peaks.map { peak ->
            val delay = peak.position.toFloat() / sampleRate
            val distance = delay * SPEED_OF_SOUND / 2
            
            // Doppler shift for velocity
            val dopplerShift = calculateDopplerShift(received, peak)
            val velocity = dopplerShift * SPEED_OF_SOUND / 
                ((startFrequency + endFrequency) / 2)
            
            SonarTarget(
                distance = distance,
                velocity = velocity,
                amplitude = peak.amplitude,
                confidence = calculateConfidence(peak)
            )
        }
        
        return FmcwResult(
            targets = targets.filter { it.distance in 0.3f..20f },
            noiseFloor = calculateNoiseFloor(correlation),
            signalQuality = calculateSignalQuality(peaks, correlation)
        )
    }
    
    /**
     * Beamforming with multiple microphones (if available)
     * Some phones have 2-4 microphones - use for directional detection
     */
    fun beamformingProcess(micSignals: List<ShortArray>): BeamformResult {
        if (micSignals.size < 2) {
            return BeamformResult.singleMic()
        }
        
        // Phase difference between microphones gives direction
        val angles = mutableListOf<Float>()
        
        for (angle in 0 until 360 step 15) {
            val beamOutput = calculateBeamOutput(micSignals, angle.toFloat())
            angles.add(beamOutput)
        }
        
        val peakAngle = angles.indexOfMax() * 15
        val confidence = angles.max() / angles.average().toFloat()
        
        return BeamformResult(
            hasMultipleMics = true,
            estimatedAngle = peakAngle.toFloat(),
            angleConfidence = confidence,
            beamPattern = angles.toFloatArray()
        )
    }
    
    companion object {
        const val SPEED_OF_SOUND = 343f // m/s at 20°C
    }
}

data class FmcwResult(
    val targets: List<SonarTarget>,
    val noiseFloor: Float,
    val signalQuality: Float
)

data class SonarTarget(
    val distance: Float,
    val velocity: Float,
    val amplitude: Float,
    val confidence: Float
)

data class BeamformResult(
    val hasMultipleMics: Boolean,
    val estimatedAngle: Float?,
    val angleConfidence: Float,
    val beamPattern: FloatArray?
) {
    companion object {
        fun singleMic() = BeamformResult(false, null, 0f, null)
    }
}
```

#### Parametric Array (Theoretical)

Using ultrasonic carrier frequencies to create highly directional audio beams:

```kotlin
/**
 * Parametric Array Sonar (Theoretical/Experimental)
 * Uses nonlinear acoustic interaction for highly directional beam
 * 
 * Note: Requires specific hardware capabilities - may not work on all devices
 */
class ParametricArraySonar {
    
    /**
     * Generate parametric array signal
     * Two ultrasonic frequencies that interact to create audible difference frequency
     */
    fun generateParametricSignal(
        carrierFreq: Float = 40000f,  // 40 kHz carrier
        modulationFreq: Float = 1000f  // 1 kHz modulation
    ): FloatArray {
        // Most phone speakers can't reach 40kHz, but this shows the concept
        // Real implementation would use maximum speaker frequency
        
        val actualCarrier = minOf(carrierFreq, 22000f) // Limit to speaker capability
        
        val samples = FloatArray(48000) // 1 second
        for (i in samples.indices) {
            val t = i / 48000f
            // AM modulation creates sum and difference frequencies
            val carrier = sin(2 * PI * actualCarrier * t)
            val modulator = 1 + 0.5f * sin(2 * PI * modulationFreq * t)
            samples[i] = (carrier * modulator).toFloat()
        }
        
        return samples
    }
}
```

---

### 17.4 Bluetooth 5.0+ Extended Range

#### Bluetooth LE Coded PHY (Long Range)

Bluetooth 5.0 introduced Coded PHY which can achieve 4x the range of standard BLE.

```kotlin
/**
 * Bluetooth 5.0 Long Range detection
 * Uses Coded PHY (S=8) for extended range scanning
 */
class BluetoothLongRangeScanner(private val context: Context) {
    
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner = bluetoothAdapter?.bluetoothLeScanner
    
    /**
     * Check if device supports Bluetooth 5.0 Long Range
     */
    fun supportsLongRange(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bluetoothAdapter?.isLeCodedPhySupported == true
        } else {
            false
        }
    }
    
    /**
     * Start long-range BLE scanning
     * Coded PHY can reach 400m in open air, 50m+ indoors
     */
    fun startLongRangeScan(callback: LongRangeScanCallback) {
        if (!supportsLongRange()) {
            startStandardScan(callback)
            return
        }
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setLegacy(false) // Required for extended advertising
            .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED) // Scan all PHYs
            .build()
        
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val reading = BleLongRangeReading(
                    address = result.device.address,
                    rssi = result.rssi,
                    txPower = result.txPower,
                    primaryPhy = result.primaryPhy,
                    secondaryPhy = result.secondaryPhy,
                    isCodedPhy = result.primaryPhy == BluetoothDevice.PHY_LE_CODED,
                    estimatedDistance = calculateDistance(result.rssi, result.txPower),
                    timestamp = System.currentTimeMillis()
                )
                callback.onDeviceFound(reading)
            }
        }
        
        scanner?.startScan(null, settings, scanCallback)
    }
    
    /**
     * Calculate distance from RSSI using log-distance path loss model
     * More accurate with txPower from advertisement
     */
    private fun calculateDistance(rssi: Int, txPower: Int): Float {
        val actualTxPower = if (txPower != ScanResult.TX_POWER_NOT_PRESENT) {
            txPower
        } else {
            -59 // Default tx power at 1m
        }
        
        val ratio = rssi.toFloat() / actualTxPower
        return if (ratio < 1.0) {
            ratio.pow(10)
        } else {
            val accuracy = 0.89976f * ratio.pow(7.7095f) + 0.111f
            accuracy
        }
    }
    
    /**
     * Direction finding using multiple BLE advertisements
     * Requires movement or multiple receivers for triangulation
     */
    fun estimateDirection(readings: List<BleLongRangeReading>): DirectionEstimate? {
        if (readings.size < 3) return null
        
        // Use RSSI gradient to estimate direction
        val rssiGradient = calculateRssiGradient(readings)
        
        // Phone movement creates pseudo-antenna array
        val movementVector = getPhoneMovementVector()
        
        return if (movementVector.magnitude > 0.1f) {
            val angle = calculateAngleFromGradient(rssiGradient, movementVector)
            DirectionEstimate(angle, confidence = 0.6f)
        } else {
            null
        }
    }
}

data class BleLongRangeReading(
    val address: String,
    val rssi: Int,
    val txPower: Int,
    val primaryPhy: Int,
    val secondaryPhy: Int,
    val isCodedPhy: Boolean,
    val estimatedDistance: Float,
    val timestamp: Long
)

data class DirectionEstimate(
    val angleDegrees: Float,
    val confidence: Float
)

interface LongRangeScanCallback {
    fun onDeviceFound(reading: BleLongRangeReading)
}
```

#### Bluetooth Direction Finding (AoA/AoD)

Bluetooth 5.1 added Angle of Arrival (AoA) and Angle of Departure (AoD):

```kotlin
/**
 * Bluetooth 5.1 Direction Finding
 * Provides precise angle measurement (+/- 5°)
 * 
 * Note: Requires specific hardware support (antenna array)
 */
class BluetoothDirectionFinder(private val context: Context) {
    
    /**
     * Check if device supports direction finding
     * Very few phones currently support this (2024)
     */
    fun supportsDirectionFinding(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check for antenna switching capability
            BluetoothAdapter.getDefaultAdapter()
                ?.isLePeriodicAdvertisingSupported == true
        } else {
            false
        }
    }
    
    /**
     * Start direction-aware scanning
     * Falls back to RSSI-based estimation if hardware unsupported
     */
    fun startDirectionScan(callback: DirectionCallback) {
        if (supportsDirectionFinding()) {
            startHardwareDirectionScan(callback)
        } else {
            startSoftwareDirectionEstimation(callback)
        }
    }
    
    /**
     * Software-based direction estimation
     * Uses phone movement + RSSI changes to estimate direction
     */
    private fun startSoftwareDirectionEstimation(callback: DirectionCallback) {
        val rssiHistory = mutableMapOf<String, MutableList<Pair<Long, Int>>>()
        val orientationHistory = mutableListOf<Pair<Long, Float>>()
        
        // Collect orientation data
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                orientationHistory.add(System.currentTimeMillis() to event.values[0])
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(
            rotationListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        
        // When phone rotates, correlate RSSI changes with orientation
        // Direction with highest RSSI indicates target bearing
    }
}

interface DirectionCallback {
    fun onDirectionEstimate(address: String, angle: Float, confidence: Float, distance: Float)
}
```

---

### 17.5 Through-Wall Detection Techniques

#### WiFi-Based Through-Wall Sensing

```kotlin
/**
 * Through-Wall Human Detection using WiFi
 * Exploits WiFi signal reflection and absorption by human body
 */
class ThroughWallDetector(private val context: Context) {
    
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val signalHistory = mutableMapOf<String, CircularBuffer<WifiReading>>()
    
    data class WifiReading(
        val bssid: String,
        val rssi: Int,
        val frequency: Int,
        val timestamp: Long,
        val channelWidth: Int
    )
    
    /**
     * Detect human presence behind walls using WiFi signal analysis
     * 
     * Principle: Human body contains 60% water, which absorbs/reflects 2.4GHz WiFi
     * Movement causes measurable fluctuations in signal strength
     */
    suspend fun detectThroughWall(): ThroughWallDetection {
        // Collect readings from all visible access points
        val currentReadings = collectWifiReadings()
        
        // Update history
        currentReadings.forEach { reading ->
            signalHistory.getOrPut(reading.bssid) { 
                CircularBuffer(500) 
            }.add(reading)
        }
        
        // Analyze each AP for human-caused fluctuations
        val detections = signalHistory.mapNotNull { (bssid, history) ->
            analyzeForHumanPresence(bssid, history)
        }
        
        // Combine detections from multiple APs
        return combineDetections(detections)
    }
    
    /**
     * Analyze signal for human-caused fluctuations
     * Distinguishes human movement from environmental noise
     */
    private fun analyzeForHumanPresence(
        bssid: String, 
        history: CircularBuffer<WifiReading>
    ): SingleApDetection? {
        if (history.size < 100) return null
        
        val rssiValues = history.map { it.rssi.toFloat() }
        
        // Calculate variance (human movement increases variance)
        val variance = calculateVariance(rssiValues)
        val baseline = getBaseline(bssid)
        
        // Frequency analysis for breathing/movement patterns
        val fftResult = performFFT(rssiValues)
        
        // Breathing: 0.2-0.5 Hz (12-30 breaths/min)
        val breathingEnergy = fftResult.getEnergyInRange(0.2f, 0.5f)
        
        // Walking: 1-2 Hz (60-120 steps/min)
        val walkingEnergy = fftResult.getEnergyInRange(1f, 2f)
        
        // Arm movement: 2-5 Hz
        val movementEnergy = fftResult.getEnergyInRange(2f, 5f)
        
        val humanScore = calculateHumanScore(
            varianceRatio = variance / baseline.avgVariance,
            breathingEnergy = breathingEnergy,
            walkingEnergy = walkingEnergy,
            movementEnergy = movementEnergy
        )
        
        return if (humanScore > DETECTION_THRESHOLD) {
            SingleApDetection(
                bssid = bssid,
                confidence = humanScore,
                isMoving = walkingEnergy > WALKING_THRESHOLD,
                isBreathing = breathingEnergy > BREATHING_THRESHOLD,
                estimatedDistance = estimateDistanceFromRssi(history.last().rssi)
            )
        } else null
    }
    
    /**
     * Estimate distance to detected presence
     * Uses multipath analysis when possible
     */
    private fun estimateDistanceFromRssi(rssi: Int): Float {
        // Free-space path loss model adjusted for indoor/through-wall
        val txPower = -40 // Typical AP tx power at 1m
        val pathLossExponent = 3.5f // Higher for through-wall
        
        return 10f.pow((txPower - rssi) / (10 * pathLossExponent))
    }
    
    /**
     * Wall material affects detection capability
     */
    enum class WallType(val attenuation: Float) {
        DRYWALL(3f),        // ~3dB loss
        CONCRETE(10f),      // ~10dB loss
        BRICK(8f),          // ~8dB loss
        GLASS(2f),          // ~2dB loss
        METAL(20f),         // ~20dB loss (very difficult)
        WOOD(4f)            // ~4dB loss
    }
    
    companion object {
        const val DETECTION_THRESHOLD = 0.5f
        const val WALKING_THRESHOLD = 0.3f
        const val BREATHING_THRESHOLD = 0.2f
    }
}

data class SingleApDetection(
    val bssid: String,
    val confidence: Float,
    val isMoving: Boolean,
    val isBreathing: Boolean,
    val estimatedDistance: Float
)

data class ThroughWallDetection(
    val detected: Boolean,
    val confidence: Float,
    val numSupportingAps: Int,
    val estimatedDistance: Float?,
    val estimatedAngle: Float?,
    val isMoving: Boolean,
    val isStationary: Boolean,
    val breathingDetected: Boolean
)
```

#### Doppler Radar Principles via Phone

```kotlin
/**
 * Doppler Effect Detection
 * Uses WiFi/Bluetooth carrier frequency shifts to detect motion
 */
class DopplerDetector {
    
    /**
     * Detect Doppler shift in received signals
     * Moving targets cause frequency shift: Δf = 2 * v * f / c
     * 
     * For WiFi at 2.4GHz:
     * - Walking (1.4 m/s) causes ~22 Hz shift
     * - Running (5 m/s) causes ~80 Hz shift
     * - Breathing causes ~0.5 Hz shift
     */
    fun detectDopplerShift(
        samples: FloatArray, 
        sampleRate: Int,
        carrierFreq: Float
    ): DopplerResult {
        // Perform high-resolution FFT
        val fftSize = 8192
        val fftResult = performFFT(samples, fftSize)
        
        // Find peaks around carrier frequency
        val carrierBin = (carrierFreq * fftSize / sampleRate).toInt()
        val searchRange = 100 // bins around carrier
        
        val peaks = findPeaksInRange(fftResult, carrierBin - searchRange, carrierBin + searchRange)
        
        // Convert peak offsets to velocities
        val velocities = peaks.map { peak ->
            val freqShift = (peak.bin - carrierBin) * sampleRate.toFloat() / fftSize
            val velocity = freqShift * SPEED_OF_LIGHT / (2 * carrierFreq)
            DopplerTarget(velocity = velocity, strength = peak.amplitude)
        }
        
        // Classify detected motion
        val motionType = classifyMotion(velocities)
        
        return DopplerResult(
            targets = velocities,
            motionType = motionType,
            maxVelocity = velocities.maxOfOrNull { abs(it.velocity) } ?: 0f
        )
    }
    
    /**
     * Classify motion type from Doppler signature
     */
    private fun classifyMotion(targets: List<DopplerTarget>): MotionType {
        val maxVel = targets.maxOfOrNull { abs(it.velocity) } ?: 0f
        
        return when {
            maxVel < 0.1f -> MotionType.STATIONARY
            maxVel < 0.5f -> MotionType.BREATHING
            maxVel < 2f -> MotionType.WALKING
            maxVel < 6f -> MotionType.RUNNING
            else -> MotionType.VEHICLE
        }
    }
    
    companion object {
        const val SPEED_OF_LIGHT = 3e8f // m/s
    }
}

data class DopplerTarget(
    val velocity: Float,
    val strength: Float
)

data class DopplerResult(
    val targets: List<DopplerTarget>,
    val motionType: MotionType,
    val maxVelocity: Float
)

enum class MotionType {
    STATIONARY,
    BREATHING,
    WALKING,
    RUNNING,
    VEHICLE,
    UNKNOWN
}
```

---

### 17.6 UAV/Drone Detection

#### RF Signature Detection

```kotlin
/**
 * UAV/Drone Detection System
 * Detects drones through RF emissions, acoustic signature, and visual detection
 */
class UavDetector(private val context: Context) {
    
    /**
     * Drone RF Detection
     * Most consumer drones use 2.4GHz or 5.8GHz for control/video
     */
    fun detectDroneRf(): DroneRfDetection {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val scanResults = wifiManager.scanResults
        
        // Look for drone-specific WiFi patterns
        val droneSignatures = scanResults.filter { result ->
            isDroneWifiSignature(result)
        }
        
        // Analyze signal patterns
        val rfDetections = droneSignatures.map { signature ->
            DroneRfSignature(
                ssid = signature.SSID,
                bssid = signature.BSSID,
                rssi = signature.level,
                frequency = signature.frequency,
                droneType = identifyDroneType(signature),
                estimatedDistance = estimateDroneDistance(signature.level),
                confidence = calculateDroneConfidence(signature)
            )
        }
        
        return DroneRfDetection(
            detected = rfDetections.isNotEmpty(),
            signatures = rfDetections,
            strongestSignal = rfDetections.maxByOrNull { it.rssi }
        )
    }
    
    /**
     * Check if WiFi signature matches known drone patterns
     */
    private fun isDroneWifiSignature(result: ScanResult): Boolean {
        val ssid = result.SSID?.uppercase() ?: ""
        val bssid = result.BSSID?.uppercase() ?: ""
        
        // Known drone manufacturer prefixes
        val dronePatterns = listOf(
            "DJI", "PHANTOM", "MAVIC", "SPARK", "TELLO",    // DJI
            "SKYDIO",                                         // Skydio
            "PARROT", "ANAFI", "BEBOP",                      // Parrot
            "AUTEL", "EVO",                                   // Autel
            "YUNEEC", "TYPHOON",                              // Yuneec
            "3DR", "SOLO",                                    // 3DR
            "GOPRO", "KARMA",                                 // GoPro
            "DRONE", "QUAD", "UAV", "COPTER"                 // Generic
        )
        
        // Check SSID
        if (dronePatterns.any { ssid.contains(it) }) {
            return true
        }
        
        // Check OUI (first 6 chars of MAC) for known drone manufacturers
        val knownDroneOuis = listOf(
            "60:60:1F", // DJI
            "34:D2:62", // DJI
            "48:01:C5", // DJI
            "A0:14:3D", // Parrot
            "90:03:B7", // Parrot
            "00:12:1C", // Parrot
        )
        
        return knownDroneOuis.any { bssid.startsWith(it.replace(":", "")) }
    }
    
    /**
     * Identify drone type from signature
     */
    private fun identifyDroneType(result: ScanResult): DroneType {
        val ssid = result.SSID?.uppercase() ?: ""
        
        return when {
            ssid.contains("MAVIC") -> DroneType.DJI_MAVIC
            ssid.contains("PHANTOM") -> DroneType.DJI_PHANTOM
            ssid.contains("MINI") -> DroneType.DJI_MINI
            ssid.contains("TELLO") -> DroneType.DJI_TELLO
            ssid.contains("ANAFI") -> DroneType.PARROT_ANAFI
            ssid.contains("SKYDIO") -> DroneType.SKYDIO
            else -> DroneType.UNKNOWN
        }
    }
    
    /**
     * Acoustic drone detection
     * Drones produce characteristic sound at 100-1000 Hz from motors/props
     */
    suspend fun detectDroneAcoustic(audioSamples: ShortArray): DroneAcousticDetection {
        val fftResult = performFFT(audioSamples.map { it.toFloat() }.toFloatArray())
        
        // Drone motor frequencies (typically 100-500 Hz for props)
        val motorFreqEnergy = fftResult.getEnergyInRange(100f, 500f)
        
        // Propeller harmonics
        val propHarmonics = listOf(200f, 400f, 600f, 800f).map { freq ->
            fftResult.getEnergyAt(freq)
        }
        
        // Characteristic drone sound pattern
        val droneScore = calculateDroneAcousticScore(motorFreqEnergy, propHarmonics)
        
        // Estimate distance from sound intensity
        val estimatedDistance = estimateDistanceFromAudio(motorFreqEnergy)
        
        // Direction (requires stereo mics)
        val direction = estimateAudioDirection()
        
        return DroneAcousticDetection(
            detected = droneScore > DRONE_ACOUSTIC_THRESHOLD,
            confidence = droneScore,
            estimatedDistance = estimatedDistance,
            estimatedDirection = direction,
            frequencyProfile = extractFrequencyProfile(fftResult)
        )
    }
    
    /**
     * Visual drone detection using camera
     * Uses ML model trained on drone silhouettes
     */
    suspend fun detectDroneVisual(frame: Bitmap): DroneVisualDetection {
        // Preprocess image
        val processed = preprocessForDroneDetection(frame)
        
        // Run TFLite model
        val detections = runDroneDetectionModel(processed)
        
        return DroneVisualDetection(
            detected = detections.isNotEmpty(),
            boundingBoxes = detections.map { it.boundingBox },
            confidences = detections.map { it.confidence },
            droneTypes = detections.map { classifyDroneVisual(it) }
        )
    }
    
    /**
     * Fuse all detection methods for best accuracy
     */
    fun fuseDroneDetections(
        rf: DroneRfDetection?,
        acoustic: DroneAcousticDetection?,
        visual: DroneVisualDetection?
    ): FusedDroneDetection {
        val rfScore = rf?.let { if (it.detected) 0.8f else 0f } ?: 0f
        val acousticScore = acoustic?.confidence ?: 0f
        val visualScore = visual?.let { 
            if (it.detected) it.confidences.maxOrNull() ?: 0f else 0f 
        } ?: 0f
        
        // Weighted fusion
        val fusedScore = rfScore * 0.4f + acousticScore * 0.3f + visualScore * 0.3f
        
        // Distance estimation (prefer visual > acoustic > RF)
        val distance = visual?.estimatedDistance 
            ?: acoustic?.estimatedDistance 
            ?: rf?.strongestSignal?.estimatedDistance
        
        return FusedDroneDetection(
            detected = fusedScore > FUSED_DETECTION_THRESHOLD,
            confidence = fusedScore,
            estimatedDistance = distance,
            droneType = determineMostLikelyType(rf, visual),
            detectionSources = listOfNotNull(
                if (rfScore > 0) "RF" else null,
                if (acousticScore > 0) "ACOUSTIC" else null,
                if (visualScore > 0) "VISUAL" else null
            )
        )
    }
    
    companion object {
        const val DRONE_ACOUSTIC_THRESHOLD = 0.6f
        const val FUSED_DETECTION_THRESHOLD = 0.5f
    }
}

enum class DroneType {
    DJI_MAVIC,
    DJI_PHANTOM,
    DJI_MINI,
    DJI_TELLO,
    DJI_OTHER,
    PARROT_ANAFI,
    PARROT_OTHER,
    SKYDIO,
    AUTEL,
    UNKNOWN
}

data class DroneRfSignature(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val droneType: DroneType,
    val estimatedDistance: Float,
    val confidence: Float
)

data class DroneRfDetection(
    val detected: Boolean,
    val signatures: List<DroneRfSignature>,
    val strongestSignal: DroneRfSignature?
)

data class DroneAcousticDetection(
    val detected: Boolean,
    val confidence: Float,
    val estimatedDistance: Float?,
    val estimatedDirection: Float?,
    val frequencyProfile: FloatArray
)

data class DroneVisualDetection(
    val detected: Boolean,
    val boundingBoxes: List<RectF>,
    val confidences: List<Float>,
    val droneTypes: List<DroneType>,
    val estimatedDistance: Float? = null
)

data class FusedDroneDetection(
    val detected: Boolean,
    val confidence: Float,
    val estimatedDistance: Float?,
    val droneType: DroneType?,
    val detectionSources: List<String>
)
```

---

### 17.7 Auto-Maximize Device Capabilities

The system automatically detects and enables maximum capabilities for each device.

```kotlin
/**
 * Auto-Maximize System
 * Automatically detects and enables all available capabilities
 * Adapts to device hardware for optimal performance
 */
class AutoMaximizer(private val context: Context) {
    
    /**
     * Comprehensive capability detection
     * Runs at app startup and caches results
     */
    fun detectAllCapabilities(): DeviceCapabilityProfile {
        val profile = DeviceCapabilityProfile(
            // Basic sensors
            hasWifi = checkWifiCapability(),
            hasBluetooth = checkBluetoothCapability(),
            hasMicrophone = checkMicrophoneCapability(),
            hasCamera = checkCameraCapability(),
            
            // Advanced capabilities
            wifiRttSupported = checkWifiRttCapability(),
            wifiAwareSupported = checkWifiAwareCapability(),
            bluetooth5Supported = checkBluetooth5Capability(),
            bleCodedPhySupported = checkBleCodedPhyCapability(),
            bleDirectionFinding = checkBleDirectionFinding(),
            uwbSupported = checkUwbCapability(),
            
            // Audio capabilities
            ultrasonicSupported = checkUltrasonicCapability(),
            multiMicrophoneSupported = checkMultiMicCapability(),
            noiseSuppression = checkNoiseSuppressionCapability(),
            
            // Camera capabilities
            cameraResolution = getCameraMaxResolution(),
            camera60fpsSupported = check60fpsCapability(),
            infraredCamera = checkInfraredCamera(),
            depthCamera = checkDepthCamera(),
            
            // Processing power
            cpuCores = Runtime.getRuntime().availableProcessors(),
            ramMb = getAvailableRam(),
            gpuAcceleration = checkGpuAcceleration(),
            nnApiSupported = checkNnApiSupport(),
            
            // Android version features
            androidVersion = Build.VERSION.SDK_INT,
            hasBackgroundLocationAccess = checkBackgroundLocation()
        )
        
        return profile
    }
    
    /**
     * Create optimal configuration based on device capabilities
     */
    fun createOptimalConfiguration(profile: DeviceCapabilityProfile): OptimalConfig {
        val sensors = mutableSetOf<SensorConfig>()
        
        // WiFi configuration
        if (profile.hasWifi) {
            sensors.add(SensorConfig.Wifi(
                useRtt = profile.wifiRttSupported,
                useAware = profile.wifiAwareSupported,
                scanInterval = if (profile.cpuCores >= 4) 500L else 1000L
            ))
        }
        
        // Bluetooth configuration
        if (profile.hasBluetooth) {
            sensors.add(SensorConfig.Bluetooth(
                useLongRange = profile.bleCodedPhySupported,
                useDirectionFinding = profile.bleDirectionFinding,
                scanMode = if (profile.cpuCores >= 4) 
                    BleScanMode.LOW_LATENCY else BleScanMode.BALANCED
            ))
        }
        
        // Audio configuration
        if (profile.hasMicrophone) {
            sensors.add(SensorConfig.Audio(
                useUltrasonic = profile.ultrasonicSupported,
                useFmcw = profile.cpuCores >= 4 && profile.ultrasonicSupported,
                useBeamforming = profile.multiMicrophoneSupported,
                sampleRate = if (profile.ultrasonicSupported) 48000 else 44100,
                fftSize = if (profile.cpuCores >= 4) 4096 else 2048
            ))
        }
        
        // Camera configuration
        if (profile.hasCamera) {
            sensors.add(SensorConfig.Camera(
                resolution = calculateOptimalResolution(profile),
                frameRate = if (profile.camera60fpsSupported && profile.cpuCores >= 6) 30 else 15,
                useDepth = profile.depthCamera,
                useInfrared = profile.infraredCamera,
                useOpticalFlow = profile.cpuCores >= 4
            ))
        }
        
        // UWB configuration
        if (profile.uwbSupported) {
            sensors.add(SensorConfig.Uwb(
                enabled = true,
                maxRange = 50f,
                updateRate = 10
            ))
        }
        
        // ML configuration
        val mlConfig = MlConfig(
            useGpuAcceleration = profile.gpuAcceleration,
            useNnApi = profile.nnApiSupported,
            modelComplexity = when {
                profile.cpuCores >= 8 && profile.ramMb >= 6000 -> ModelComplexity.HIGH
                profile.cpuCores >= 4 && profile.ramMb >= 4000 -> ModelComplexity.MEDIUM
                else -> ModelComplexity.LOW
            }
        )
        
        // Fusion configuration
        val fusionConfig = FusionConfig(
            updateRateHz = when {
                profile.cpuCores >= 8 -> 20
                profile.cpuCores >= 4 -> 10
                else -> 5
            },
            maxTargets = when {
                profile.ramMb >= 6000 -> 20
                profile.ramMb >= 4000 -> 12
                else -> 6
            },
            useKalmanFilter = profile.cpuCores >= 4,
            useParticleFilter = profile.cpuCores >= 6
        )
        
        return OptimalConfig(
            sensors = sensors,
            ml = mlConfig,
            fusion = fusionConfig,
            tier = calculateDeviceTier(profile),
            estimatedRange = estimateMaxRange(profile),
            batteryImpact = estimateBatteryImpact(profile, sensors)
        )
    }
    
    /**
     * Calculate device tier based on capabilities
     */
    private fun calculateDeviceTier(profile: DeviceCapabilityProfile): DeviceTier {
        var score = 0
        
        // Basic features (Tier 1 baseline)
        if (profile.hasWifi) score += 10
        if (profile.hasBluetooth) score += 10
        if (profile.hasMicrophone) score += 10
        if (profile.hasCamera) score += 10
        
        // Advanced features (Tier 2)
        if (profile.uwbSupported) score += 30
        if (profile.wifiRttSupported) score += 15
        if (profile.bleCodedPhySupported) score += 10
        if (profile.depthCamera) score += 10
        
        // High-end features (Tier 3)
        if (profile.bleDirectionFinding) score += 20
        if (profile.infraredCamera) score += 15
        if (profile.multiMicrophoneSupported) score += 10
        
        // Processing power bonus
        if (profile.cpuCores >= 8) score += 10
        if (profile.gpuAcceleration) score += 10
        if (profile.nnApiSupported) score += 5
        
        return when {
            score >= 100 -> DeviceTier.TIER_3_ADVANCED
            score >= 60 -> DeviceTier.TIER_2_UWB
            score >= 30 -> DeviceTier.TIER_1_STANDARD
            else -> DeviceTier.TIER_0_BASIC
        }
    }
    
    /**
     * Estimate maximum detection range based on capabilities
     */
    private fun estimateMaxRange(profile: DeviceCapabilityProfile): RangeEstimate {
        var maxRange = 5f // Base range
        var throughWallRange = 0f
        
        if (profile.uwbSupported) {
            maxRange = maxOf(maxRange, 50f)
            throughWallRange = maxOf(throughWallRange, 10f)
        }
        
        if (profile.wifiRttSupported) {
            maxRange = maxOf(maxRange, 20f)
            throughWallRange = maxOf(throughWallRange, 15f)
        }
        
        if (profile.bleCodedPhySupported) {
            maxRange = maxOf(maxRange, 30f)
        }
        
        if (profile.hasWifi) {
            throughWallRange = maxOf(throughWallRange, 8f) // WiFi CSI
        }
        
        if (profile.ultrasonicSupported) {
            maxRange = maxOf(maxRange, 12f) // FMCW sonar
        }
        
        return RangeEstimate(
            lineOfSight = maxRange,
            throughWall = throughWallRange,
            optimalConditions = maxRange * 1.5f
        )
    }
    
    /**
     * Runtime capability monitoring and adjustment
     */
    fun monitorAndAdjust(
        currentConfig: OptimalConfig,
        metrics: PerformanceMetrics
    ): ConfigAdjustment {
        val adjustments = mutableListOf<Adjustment>()
        
        // CPU overload - reduce update rates
        if (metrics.cpuUsage > 80) {
            adjustments.add(Adjustment.ReduceUpdateRate(0.7f))
        }
        
        // Memory pressure - reduce max targets
        if (metrics.memoryUsage > 85) {
            adjustments.add(Adjustment.ReduceMaxTargets(0.5f))
        }
        
        // Battery critical - switch to low power sensors only
        if (metrics.batteryLevel < 15) {
            adjustments.add(Adjustment.LowPowerMode)
        }
        
        // Thermal throttling - reduce sensor activity
        if (metrics.thermalState == ThermalState.CRITICAL) {
            adjustments.add(Adjustment.ThermalThrottle)
        }
        
        return ConfigAdjustment(
            shouldAdjust = adjustments.isNotEmpty(),
            adjustments = adjustments
        )
    }
}

data class DeviceCapabilityProfile(
    // Basic sensors
    val hasWifi: Boolean,
    val hasBluetooth: Boolean,
    val hasMicrophone: Boolean,
    val hasCamera: Boolean,
    
    // WiFi advanced
    val wifiRttSupported: Boolean,
    val wifiAwareSupported: Boolean,
    
    // Bluetooth advanced
    val bluetooth5Supported: Boolean,
    val bleCodedPhySupported: Boolean,
    val bleDirectionFinding: Boolean,
    
    // UWB
    val uwbSupported: Boolean,
    
    // Audio
    val ultrasonicSupported: Boolean,
    val multiMicrophoneSupported: Boolean,
    val noiseSuppression: Boolean,
    
    // Camera
    val cameraResolution: Size,
    val camera60fpsSupported: Boolean,
    val infraredCamera: Boolean,
    val depthCamera: Boolean,
    
    // Processing
    val cpuCores: Int,
    val ramMb: Int,
    val gpuAcceleration: Boolean,
    val nnApiSupported: Boolean,
    
    // Android
    val androidVersion: Int,
    val hasBackgroundLocationAccess: Boolean
)

enum class DeviceTier {
    TIER_0_BASIC,      // Minimal features
    TIER_1_STANDARD,   // All basic sensors
    TIER_2_UWB,        // UWB + advanced features
    TIER_3_ADVANCED    // All features including direction finding
}

sealed class SensorConfig {
    data class Wifi(
        val useRtt: Boolean,
        val useAware: Boolean,
        val scanInterval: Long
    ) : SensorConfig()
    
    data class Bluetooth(
        val useLongRange: Boolean,
        val useDirectionFinding: Boolean,
        val scanMode: BleScanMode
    ) : SensorConfig()
    
    data class Audio(
        val useUltrasonic: Boolean,
        val useFmcw: Boolean,
        val useBeamforming: Boolean,
        val sampleRate: Int,
        val fftSize: Int
    ) : SensorConfig()
    
    data class Camera(
        val resolution: Size,
        val frameRate: Int,
        val useDepth: Boolean,
        val useInfrared: Boolean,
        val useOpticalFlow: Boolean
    ) : SensorConfig()
    
    data class Uwb(
        val enabled: Boolean,
        val maxRange: Float,
        val updateRate: Int
    ) : SensorConfig()
}

enum class BleScanMode {
    LOW_POWER,
    BALANCED,
    LOW_LATENCY
}

enum class ModelComplexity {
    LOW,
    MEDIUM,
    HIGH
}

data class MlConfig(
    val useGpuAcceleration: Boolean,
    val useNnApi: Boolean,
    val modelComplexity: ModelComplexity
)

data class FusionConfig(
    val updateRateHz: Int,
    val maxTargets: Int,
    val useKalmanFilter: Boolean,
    val useParticleFilter: Boolean
)

data class OptimalConfig(
    val sensors: Set<SensorConfig>,
    val ml: MlConfig,
    val fusion: FusionConfig,
    val tier: DeviceTier,
    val estimatedRange: RangeEstimate,
    val batteryImpact: BatteryImpact
)

data class RangeEstimate(
    val lineOfSight: Float,
    val throughWall: Float,
    val optimalConditions: Float
)

data class BatteryImpact(
    val estimatedDrainPerHour: Float,
    val estimatedRuntime: Float
)

sealed class Adjustment {
    data class ReduceUpdateRate(val factor: Float) : Adjustment()
    data class ReduceMaxTargets(val factor: Float) : Adjustment()
    object LowPowerMode : Adjustment()
    object ThermalThrottle : Adjustment()
}

data class ConfigAdjustment(
    val shouldAdjust: Boolean,
    val adjustments: List<Adjustment>
)
```

---

### 17.8 Extended Development Roadmap for Range Improvement

#### Phase 9: Extended Range Implementation (Weeks 23-28)

##### Week 23-24: WiFi CSI/RTT
- [ ] Implement WiFi RTT ranging (Android 9+)
- [ ] Add RSSI variance analysis for through-wall detection
- [ ] Implement multipath analysis for distance estimation
- [ ] Add breathing detection via WiFi signal analysis
- [ ] Test range: Target 10-15m through walls

##### Week 25-26: Advanced Audio Sonar
- [ ] Implement FMCW chirp generation
- [ ] Add matched filter processing for echoes
- [ ] Implement Doppler shift detection
- [ ] Add multi-microphone beamforming (where available)
- [ ] Test range: Target 10-15m line of sight

##### Week 27-28: Bluetooth 5.0+ Long Range
- [ ] Implement Coded PHY scanning
- [ ] Add extended advertising support
- [ ] Implement RSSI gradient direction finding
- [ ] Add device movement correlation for angles
- [ ] Test range: Target 30-50m

#### Phase 10: Through-Wall Detection (Weeks 29-32)

##### Week 29-30: WiFi Through-Wall
- [ ] Implement CSI-based presence detection
- [ ] Add wall attenuation compensation
- [ ] Implement multi-AP triangulation
- [ ] Add movement pattern classification
- [ ] Test: Detect presence through drywall at 5-10m

##### Week 31-32: Advanced Through-Wall
- [ ] Implement breathing detection through walls
- [ ] Add walking gait recognition
- [ ] Implement room occupancy counting
- [ ] Add position estimation using multiple APs
- [ ] Test: Detect presence through concrete at 3-5m

#### Phase 11: UAV/Drone Detection (Weeks 33-36)

##### Week 33-34: RF-Based Drone Detection
- [ ] Implement drone WiFi signature database
- [ ] Add drone controller detection
- [ ] Implement frequency hopping detection
- [ ] Add drone type classification
- [ ] Test: Detect consumer drones at 100m+

##### Week 35-36: Multi-Modal Drone Detection
- [ ] Implement acoustic drone detection
- [ ] Add propeller frequency analysis
- [ ] Implement visual drone detection ML model
- [ ] Add sensor fusion for drone tracking
- [ ] Test: Fused detection with 90%+ accuracy

#### Phase 12: Auto-Maximize & Optimization (Weeks 37-40)

##### Week 37-38: Capability Detection
- [ ] Implement comprehensive hardware detection
- [ ] Add runtime capability monitoring
- [ ] Implement adaptive configuration
- [ ] Add performance profiling
- [ ] Test: Correct tier assignment for 20+ device models

##### Week 39-40: Optimization & Polish
- [ ] Implement battery-aware range optimization
- [ ] Add thermal management
- [ ] Implement background scanning optimization
- [ ] Add range calibration wizard
- [ ] Test: Optimal performance across all tiers

---

*Nova BioRadar - All-in-One Autonomous Development Guide v1.1*
