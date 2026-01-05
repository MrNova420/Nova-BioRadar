# Nova BioRadar - Quick Start Guide

**Get started in 5 minutes!** This guide will help you install and use Nova BioRadar.

---

## 📲 Installation

### Method 1: Install Pre-built APK (Easiest)

1. **Download the APK**:
   - Go to the [Releases page](https://github.com/MrNova420/Nova-BioRadar/releases)
   - Download the latest `nova-bioradar-release.apk`

2. **Install**:
   - Open the downloaded APK file on your Android device
   - If prompted, enable "Install from Unknown Sources"
   - Tap "Install"
   - Tap "Open" when installation completes

3. **Grant Permissions**:
   - The app will request several permissions
   - Tap "Allow" for each permission (required for sensors)
   - Permissions needed:
     - Location (for WiFi/Bluetooth scanning)
     - Camera (for motion detection)
     - Microphone (for audio sonar)

### Method 2: Build from Source

```bash
# 1. Clone repository
git clone https://github.com/MrNova420/Nova-BioRadar.git
cd Nova-BioRadar

# 2. Build APK
./gradlew assembleDebug

# 3. Install to device
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🚀 First Launch

### Step 1: Open the App
- Tap the Nova BioRadar icon
- The app opens on the **Radar** screen

### Step 2: Grant Permissions
- Tap "Allow" for all permission requests
- These are required for the sensors to work

### Step 3: Start Scanning
- Tap the **[SCAN]** button at the bottom
- The radar will start sweeping
- Green dots appear when presence is detected

**That's it!** You're now detecting nearby presence.

---

## 🎯 Basic Usage

### Understanding the Radar Screen

```
┌────────────────────────────────────┐
│  MODE: Normal        🔋 85%    ⚙️   │ ← Mode & Battery
├────────────────────────────────────┤
│                                    │
│          ┌──────────┐              │
│        ┌─│    ●     │─┐            │ ← Radar Display
│       │  │  ●    ●  │  │           │   (Green dots = targets)
│        └─│          │─┘            │
│          └──────────┘              │
│                                    │
│  TARGETS: 3    CONFIDENCE: 82%     │ ← Detection Info
│  RANGE: 15m    SENSORS: WiFi, BT   │
│                                    │
│  [SCAN] [GUARD] [MESH] [SETTINGS]  │ ← Navigation
└────────────────────────────────────┘
```

**What each element means**:
- **Green dots**: Detected targets (people, animals, etc.)
- **Radar rings**: Distance from center (5m, 10m, 15m, etc.)
- **Confidence**: How sure the app is about detections (0-100%)
- **Active sensors**: Which detection methods are currently running

### Navigation Tabs

| Tab | What it does |
|-----|--------------|
| **⚡ Radar** | Main detection display - see targets in real-time |
| **🛡️ Guard** | Perimeter monitoring - alerts when something enters zones |
| **📡 Mesh** | Multi-device network - coordinate with other phones |
| **⚙️ Settings** | Configure app - adjust sensors, range, display |

---

## ⚡ Quick Actions

### 1. Start Detection (Easiest Way)
```
1. Open app
2. Tap [SCAN] button
3. Wait 2-3 seconds for calibration
4. Green dots appear when presence detected
```

### 2. Use Ultimate Mode (One-Button Optimization)
```
1. Tap Settings tab
2. Tap "Advanced Modes" card at top
3. Tap "Activate Ultimate Mode"
4. Done! App automatically optimizes everything
```

**What Ultimate Mode does**:
- ✅ Detects ALL available sensors on your device
- ✅ Classifies your device tier (High-End/Mid-Range/Budget)
- ✅ Optimizes all settings automatically
- ✅ Maximizes detection range and accuracy
- ✅ Shows real-time performance metrics

### 3. Use Blackout Mode (Zero Infrastructure)
```
1. Tap Settings → Advanced Modes
2. Select a profile:
   - Maximum Range (200m+, 6-8hrs)
   - Balanced (100m, 12-16hrs)
   - Maximum Endurance (50m, 24+hrs)
   - Stealth (30m, 18-24hrs, silent)
   - Mesh Hub (200m+, coordinator)
3. Tap "Activate Blackout Mode"
4. Works with NO WiFi, cellular, or internet!
```

**When to use Blackout Mode**:
- 🔥 Power outage / disaster scenario
- 🔥 Remote location with no infrastructure
- 🔥 Emergency situation
- 🔥 Want maximum range
- 🔥 Need complete independence

---

## 🎮 Operating Modes

### Quick Mode Selection
Tap the **mode name** at the top of Radar screen to see all modes:

| Mode | Best For | Battery Life |
|------|----------|--------------|
| **Normal** ⭕ | Everyday use | 6-8 hours |
| **Emergency** ⚠️ | Low battery situations | 24-48 hours |
| **Guard** 🛡️ | Monitoring a fixed location | 8-12 hours |
| **Stealth** 👁️ | Silent, invisible operation | 12-20 hours |
| **Search** 🔍 | Maximum sensitivity | 3-4 hours |
| **Ultimate** ⚡ | Auto-optimize everything | 6-10 hours |
| **Blackout** 🔌 | Zero infrastructure | 6-24+ hours |

---

## 🛡️ Perimeter Guard Mode

**Set up zone monitoring in 3 steps:**

### Step 1: Switch to Guard Tab
- Tap the **🛡️ Guard** tab at bottom

### Step 2: Calibrate Baseline
```
1. Stand in empty area (no people nearby)
2. Tap [CALIBRATE] button
3. Wait 30 seconds
4. Baseline established!
```

### Step 3: Activate Guard
```
1. Tap [START GUARD]
2. Place phone in monitoring position
3. App alerts when presence enters zone
4. Alerts: sound + vibration + visual
```

**Use cases**:
- Monitor doorway while sleeping
- Detect anyone entering room
- Perimeter security
- Alert when kids leave safe zone

---

## 📡 Multi-Device Mesh Network

**Connect multiple phones for extended coverage:**

### Step 1: Enable Mesh on All Devices
```
1. Open app on all devices
2. Tap Mesh tab
3. Tap [START MESH]
```

### Step 2: Devices Auto-Connect
- Devices find each other automatically
- Shows connected devices on screen
- Each device contributes to detection

### Step 3: View Combined Radar
- All devices share target data
- See detections from all phones
- Extended range and accuracy

**Benefits**:
- 🌐 Cover larger area (200m+)
- 🌐 More accurate detection
- 🌐 Multi-angle perspective
- 🌐 Works via WiFi Direct (no router needed)

---

## ⚙️ Settings & Customization

### Essential Settings

**In Settings tab → scroll to find:**

1. **Max Range** (slider): How far to scan (5-50m)
2. **Sensors** (toggles): Which sensors to use
   - WiFi Scanning
   - Bluetooth Scanning
   - Audio Sonar
   - Camera Motion
3. **Display** (options): UI customization
   - Grid Lines (on/off)
   - Sweep Speed (1-5x)
   - Animation

### Performance Tuning

**For better battery life**:
- Reduce max range to 15m
- Disable Audio Sonar
- Disable Camera Motion
- Use Emergency mode

**For better detection**:
- Increase max range to 30m+
- Enable all sensors
- Use Ultimate Mode
- Increase sweep speed

---

## 🔋 Battery Optimization

### Battery Life Guide

| Configuration | Expected Battery Life |
|---------------|----------------------|
| All sensors enabled | 4-6 hours |
| WiFi + Bluetooth only | 8-12 hours |
| WiFi only | 12-18 hours |
| Emergency mode | 24-48 hours |
| Blackout Endurance | 24+ hours |

### Tips for Longer Battery Life
1. ✅ Reduce max range (use 15m instead of 30m)
2. ✅ Disable audio sonar when not needed
3. ✅ Disable camera motion detection
4. ✅ Use Emergency mode
5. ✅ Lower screen brightness
6. ✅ Use Blackout Endurance profile

---

## 🆘 Troubleshooting

### "No Targets Detected"

**Check:**
1. ✅ Scan is running (button shows "STOP")
2. ✅ Sensors are enabled (Settings → Sensors)
3. ✅ Permissions granted (Location, Camera, Mic)
4. ✅ Someone is actually nearby (try walking)
5. ✅ Max range is reasonable (15-30m)

**Try:**
- Stand still for 10 seconds, then walk
- Increase max range in settings
- Enable more sensors
- Switch to Ultimate Mode

### "Permission Denied" Error

**Solution:**
1. Open Android Settings
2. Apps → Nova BioRadar → Permissions
3. Enable all permissions:
   - Location: "Allow all the time"
   - Camera: "Allow"
   - Microphone: "Allow"
4. Restart app

### App Crashes or Freezes

**Solution:**
1. Force close app
2. Clear cache: Settings → Apps → Nova BioRadar → Storage → Clear Cache
3. Restart app
4. If problem persists, reinstall

### Poor Detection Accuracy

**Try:**
1. Calibrate in empty room: Stand alone, tap [CALIBRATE]
2. Reduce max range to 15m
3. Enable Ultimate Mode (auto-optimizes)
4. Ensure good WiFi/Bluetooth coverage
5. Keep phone still (motion affects accuracy)

---

## 📱 Device Compatibility

### Fully Supported Devices
- ✅ **High-End**: Pixel 6+, Galaxy S21+, OnePlus 9+ (with UWB)
- ✅ **Mid-Range**: Most Android 12+ devices
- ✅ **Budget**: Android 8.0+ devices

### Sensor Availability

| Sensor | Required Android Version | Devices |
|--------|-------------------------|---------|
| WiFi | 8.0+ | All devices |
| Bluetooth | 8.0+ | All devices |
| Audio Sonar | 8.0+ | All devices with mic |
| Camera | 8.0+ | All devices with camera |
| **UWB** | 12.0+ | Pixel 6+, Galaxy S21+, etc. |

**Note**: UWB provides centimeter accuracy but is only on high-end devices. The app works great without it!

---

## 🎯 Use Cases & Scenarios

### 1. Home Security
```
Mode: Guard
Setup: Place phone near doorway
Result: Alert when anyone enters
```

### 2. Camping / Outdoor
```
Mode: Blackout (Maximum Range)
Setup: Place phone at campsite edge
Result: 200m+ detection with no infrastructure
```

### 3. Finding People in Dark
```
Mode: Search
Setup: Hold phone and walk slowly
Result: Detect people without seeing them
```

### 4. Emergency / Disaster
```
Mode: Blackout (Balanced)
Setup: No power/internet? No problem!
Result: Works completely off-grid
```

### 5. Building Security
```
Mode: Mesh Network
Setup: Multiple phones in different rooms
Result: Cover entire building
```

---

## 🔐 Privacy & Security

### What Data is Collected?
- ✅ **None sent to internet** - everything stays on device
- ✅ **No cloud, no servers** - fully offline
- ✅ **Encrypted local storage** - AES-256-GCM
- ✅ **Panic wipe available** - delete all data instantly

### Security Features
1. **AES-256-GCM Encryption**: All logs encrypted
2. **Panic Wipe**: Triple-tap to delete everything
3. **No Network Required**: Works completely offline
4. **No Tracking**: No analytics, no telemetry

---

## 💡 Pro Tips

### Tip 1: Calibration is Key
Always calibrate in an empty room for best accuracy. The app learns what "empty" looks like, then detects deviations.

### Tip 2: Use Ultimate Mode First
Don't tweak settings manually. Just use Ultimate Mode - it auto-optimizes everything based on your device.

### Tip 3: Blackout Mode for Emergencies
In disaster scenarios, Blackout Mode is your friend. It works with literally ZERO infrastructure.

### Tip 4: Multiple Sensors = Better Accuracy
Enable at least 3 sensors (WiFi + Bluetooth + one more) for reliable detection.

### Tip 5: Guard Mode for Night
Set up Guard Mode before sleeping. It'll wake you if anyone enters.

### Tip 6: Mesh for Large Areas
Use 3+ devices in mesh mode to cover an entire house or large outdoor area.

---

## 📞 Support & Help

### Getting Help
- 📖 **Documentation**: See [DOCUMENTATION.md](DOCUMENTATION.md)
- 🐛 **Issues**: Report bugs on [GitHub Issues](https://github.com/MrNova420/Nova-BioRadar/issues)
- 💬 **Discussions**: Ask questions in [GitHub Discussions](https://github.com/MrNova420/Nova-BioRadar/discussions)

### Useful Links
- [Full README](README.md) - Complete feature documentation
- [Development Guide](DEVELOPMENT_GUIDE.md) - For developers
- [Build Status](BUILD_STATUS.md) - Build information

---

## ✅ Quick Checklist

**Before first use:**
- [ ] Install app
- [ ] Grant all permissions
- [ ] Test scan on Radar screen
- [ ] Try Ultimate Mode

**For daily use:**
- [ ] Choose appropriate mode
- [ ] Set desired range (15-30m)
- [ ] Enable needed sensors
- [ ] Start scanning

**For emergencies:**
- [ ] Activate Blackout Mode
- [ ] Select appropriate profile
- [ ] Works with zero infrastructure

---

**You're all set!** Enjoy using Nova BioRadar. 🎉

Need more details? Check the [full README](README.md) or [documentation](DOCUMENTATION.md).
