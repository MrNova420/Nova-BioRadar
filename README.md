# BioRadar

> **Transform your Android phone into a futuristic life-form detection radar**

[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-purple.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🎯 Overview

BioRadar is an open-source Android application that uses advanced sensor fusion technology to detect and visualize the presence of living beings in your vicinity. Using a combination of WiFi signal analysis, Bluetooth scanning, ultrasonic sonar, and camera motion detection, BioRadar creates a real-time radar display showing detected targets.

### Key Features

- 📡 **Real-Time Radar Display** - Circular polar radar visualization with sweep animation
- 🔊 **Audio Sonar System** - 18kHz ultrasonic ping emission with echo analysis
- 📶 **Radio Signal Analysis** - WiFi and Bluetooth RSSI variance tracking
- 📷 **Camera Motion Detection** - Optical flow analysis for direction detection
- 📱 **UWB Support** - Centimeter-accurate ranging on supported devices
- 🛡️ **Perimeter Guard** - Automated zone monitoring with alerts
- 🌐 **Mesh Networking** - Multi-device distributed sensor network
- 🔒 **Security Features** - AES-256 encryption and panic wipe

## 📸 Screenshots

```
┌────────────────────────────────────────┐
│  ≡  BIORADAR              🔋 85%  ⚙️   │
├────────────────────────────────────────┤
│                                        │
│              ╭─────────╮               │
│           ╭──│    ◉    │──╮            │
│          ╱   │  ●      │   ╲           │
│         │    │    ◉    │    │          │
│         │    │         │    │          │
│          ╲   │         │   ╱           │
│           ╰──│         │──╯            │
│              ╰─────────╯               │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │ TARGETS: 2    CONFIDENCE: 78%   │  │
│  │ MODE: Normal  RANGE: 10m        │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [SCAN] [GUARD] [MESH] [SETTINGS]      │
│                                        │
└────────────────────────────────────────┘
```

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Hilt DI
- **Sensors**: WiFi, Bluetooth LE, Audio, Camera, UWB
- **Security**: AES-256-GCM encryption
- **Networking**: WiFi Direct, Bluetooth Mesh

## 📱 System Requirements

### Minimum (Tier 1)
| Component | Requirement |
|-----------|-------------|
| OS | Android 8.0 (API 26) |
| RAM | 2 GB |
| Storage | 50 MB |
| Sensors | Microphone, WiFi |

### Recommended (Tier 2)
| Component | Requirement |
|-----------|-------------|
| OS | Android 12.0+ (API 31) |
| RAM | 4 GB+ |
| UWB | UWB chip for precision ranging |

## 🚀 Building

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build Steps

```bash
# Clone the repository
git clone https://github.com/MrNova420/Nova-BioRadar.git
cd Nova-BioRadar

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## 📦 Project Structure

```
app/
├── src/main/
│   ├── java/com/bioradar/
│   │   ├── core/           # Core models and utilities
│   │   │   ├── models/     # Data classes
│   │   │   ├── utils/      # Capability detection
│   │   │   └── di/         # Hilt modules
│   │   ├── sensor/         # Sensor drivers
│   │   │   ├── drivers/    # WiFi, BT, Sonar, Camera
│   │   │   ├── fusion/     # Sensor fusion engine
│   │   │   └── processors/ # Signal processing
│   │   ├── ui/             # Jetpack Compose UI
│   │   │   ├── screens/    # Main screens
│   │   │   ├── components/ # Reusable components
│   │   │   ├── viewmodels/ # ViewModels
│   │   │   └── theme/      # Material 3 theme
│   │   ├── network/        # Mesh networking
│   │   └── security/       # Encryption & security
│   └── res/                # Android resources
└── build.gradle.kts
```

## 🎮 Operating Modes

| Mode | Description | Battery |
|------|-------------|---------|
| **Normal** | Full features, standard scanning | 6-8 hours |
| **Emergency** | Minimal power, extended runtime | 24-48 hours |
| **Guard** | Fixed position monitoring | 8-12 hours |
| **Stealth** | Silent operation, no emissions | 12-20 hours |
| **Search** | Maximum sensitivity | 3-4 hours |
| **Lab** | Debug mode with raw data | Variable |

## 🔌 Sensor Capabilities

### WiFi Signal Analysis
- Through-wall detection using RSSI variance
- Range: Up to 30m
- CSI-like breathing detection

### Bluetooth LE Scanning
- Device proximity tracking
- RSSI variance for motion detection
- Bluetooth 5.0 Long Range support

### Audio Sonar
- 18kHz ultrasonic ping
- FFT echo analysis
- Doppler shift motion detection
- Range: Up to 8m

### Camera Motion Detection
- Optical flow analysis
- 8-sector directional detection
- Walking pattern recognition

## 🤝 Contributing

Contributions are welcome! Please read the [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) for detailed implementation guidelines.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔒 Privacy

BioRadar is designed with privacy first:
- **No cloud uploads** - All data stays on device
- **No personal data collection** - No identifying information stored
- **Encrypted storage** - AES-256-GCM for sensitive data
- **Panic wipe** - Instant secure data destruction

## 📚 Documentation

- [Full Documentation](DOCUMENTATION.md)
- [Development Guide](DEVELOPMENT_GUIDE.md)

---

*"Detect the invisible. Protect what matters."*

© 2024 BioRadar Project
