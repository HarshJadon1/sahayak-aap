# Sahayak (सहायक) - Personal & Women Safety SOS App

<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_foreground.xml" alt="Sahayak Logo" width="120" />
</p>

<p align="center">
  <b>An automated, multi-trigger emergency assistance and personal safety system for Android.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android" alt="Android Platform" />
  <img src="https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue.svg" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-brightgreen.svg" alt="Target SDK" />
  <img src="https://img.shields.io/badge/Language-Java%20%2F%20Android%20SDK-orange.svg" alt="Language" />
  <img src="https://img.shields.io/badge/UI-Material%20Design%203-6750A4.svg" alt="Material 3" />
</p>

---

## 📌 Overview

**Sahayak (सहायक)** is an intelligent, reactive personal safety application designed to protect users during life-threatening emergencies, distress situations, and accidents. It combines background sensor monitoring, speech recognition, real-time GPS location sharing, automated video evidence capture, and lock-screen emergency accessibility into a unified, reliable safety ecosystem.

---

## 🚀 Key Features

### 1. 🚨 Multi-Vector SOS Triggers
* **Pulse Animated SOS Button**: High-visibility one-tap trigger on the main dashboard.
* **Shake Gesture Detection**: Accelerometer-driven gesture monitoring with calibrated force thresholds (`2.5g`) to eliminate false positives while ensuring reliable activation during physical assault or distress.
* **Fall & Accident Detection**: Advanced dual-phase physics detection:
  * Phase 1: Free-fall detection (`acceleration < 2.0 m/s²`).
  * Phase 2: High-impact collision check (`acceleration > 18.0 m/s²` within a 1-second window).
  * Automatically launches a 10-second countdown alert screen (`AccidentAlertActivity`) with haptic and audio sirens before dispatching SOS.
* **Continuous Voice SOS Detection**: Background speech recognition actively listening for critical phrases such as *"help"*, *"emergency"*, *"bachao"*, and *"save me"*, equipped with smart backoff delay mechanisms.

### 2. 📹 Automated Evidence Capture
* **Silent Video & Audio Recording**: On SOS trigger, `EmergencyVideoService` boots a CameraX lifecycle instance in an Android Foreground Service to record ambient video and audio directly to private app storage (`Movies/Sahayak_Recordings`).
* **Emergency Evidence Vault**: `EmergencyHistoryActivity` allows users to review saved emergency recordings with video thumbnails, integrated playback, and secure export via Android `FileProvider`.

### 3. 📍 Real-Time Location & Emergency Dispatch
* **Instant GPS Coordinates**: Utilizes `FusedLocationProviderClient` for rapid, high-accuracy latitude and longitude extraction.
* **Multipart SMS Delivery**: Automatically divides and transmits Google Maps navigation links via multi-part SMS to all designated guardians.
* **Helpline 112 Fallback**: If no emergency contacts are configured, `FallbackCallActivity` provides a 5-second countdown window and dials emergency helpline `112`.
* **"I Am Safe" False Alarm Handler**: A persistent foreground notification action allows one-tap cancellation of alerts and sends confirmation messages to guardians that the user is secure.

### 4. 👥 Guardian Management & OTP Verification
* **SMS OTP Verification**: Requires newly added emergency contacts to be verified with an automated 6-digit OTP to guarantee guardian phone number validity.
* **Edit & Update Flow**: Smooth updating of guardian names and phone numbers with real-time list synchronization.

### 5. 🔒 Lock-Screen & Medical Card Overlay
* **Accessible on Locked Devices**: `LockScreenActivity` and `EmergencyActivity` appear above the Android keyguard and lock screen (`FLAG_SHOW_WHEN_LOCKED` / `setShowWhenLocked`), displaying:
  * Blood group
  * Allergies
  * Pre-existing medical conditions
  * Emergency notes & direct-dial guardian cards
* **Live Interactive Map**: Built using **OSMDroid (OpenStreetMap)** to display user location and surrounding areas without strict dependencies on proprietary map keys.

---

## 🛠 Tech Stack & Architecture

| Component | Technology |
|---|---|
| **Core Architecture** | Android Services, Foreground Services, Sensor Listeners, Broadcast Receivers |
| **Language** | Java (Java 11 compatible) |
| **Camera & Video** | Android Jetpack **CameraX** (`camera-core`, `camera-camera2`, `camera-video`, `camera-lifecycle`) |
| **Location Services** | Google Play Services (`play-services-location`, `play-services-maps`) |
| **Mapping Engine** | **OSMDroid** (`osmdroid-android`) for open-source map rendering |
| **UI Components** | **Material Design 3** (`com.google.android.material`), ConstraintLayout, CoordinatorLayout |
| **Media & Animation** | **Glide** (Image/Thumbnail Caching & Rendering), **Lottie** (Vector Animations) |
| **Storage & Sharing** | SharedPreferences (Prefs), Android App-Specific Sandbox, `FileProvider` |

---

## 📂 Project Structure

```
Sahayak/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml           # Permissions, activities, and foreground service declarations
│   │   │   ├── java/com/sahayak/app/
│   │   │   │   ├── MainActivity.java         # Main dashboard with SOS trigger & navigation cards
│   │   │   │   ├── EmergencyService.java     # Central foreground background service (Sensors + Voice + SOS)
│   │   │   │   ├── EmergencyVideoService.java# CameraX video recording foreground service
│   │   │   │   ├── EvidenceRecordingService.java # Background audio recording fallback
│   │   │   │   ├── AccidentAlertActivity.java# 10s accident warning & siren activity
│   │   │   │   ├── FallbackCallActivity.java # 5s fallback 112 autodialer
│   │   │   │   ├── ContactsActivity.java     # Guardian list management
│   │   │   │   ├── AddContactActivity.java   # Add/Edit guardian & OTP generation
│   │   │   │   ├── VerifyOtpActivity.java    # 6-digit SMS OTP verification screen
│   │   │   │   ├── ContactManager.java       # SharedPreferences guardian storage manager
│   │   │   │   ├── LockScreenActivity.java   # Lock screen overlay with emergency cards
│   │   │   │   ├── EmergencyActivity.java    # Quick emergency info viewer
│   │   │   │   ├── EmergencyHistoryActivity.java # Video evidence library with playback/sharing
│   │   │   │   ├── MedicalInfoActivity.java  # Medical profile editor
│   │   │   │   ├── ShakeDetector.java        # Accelerometer shake gesture processor
│   │   │   │   ├── FallDetector.java         # Accelerometer fall & collision algorithm
│   │   │   │   ├── VoiceSOSManager.java      # Continuous speech recognition listener
│   │   │   │   ├── SMSHelper.java            # Multi-part emergency SMS dispatcher
│   │   │   │   ├── LocationHelper.java       # Fused location provider wrapper
│   │   │   │   ├── SplashActivity.java       # App launch splash screen
│   │   │   │   └── location/
│   │   │   │       ├── MapsActivity.java     # OSMDroid live GPS map activity
│   │   │   │       ├── LocationHelper.java   # Location callback updates helper
│   │   │   │       └── LocationService.java  # Periodic location tracker
│   │   │   └── res/
│   │   │       ├── layout/                   # Material 3 XML layouts
│   │   │       ├── xml/file_paths.xml        # FileProvider private directory paths
│   │   │       └── values/                   # Colors, themes, styles, and strings
│   └── build.gradle.kts                      # Module build configuration & dependencies
├── gradle/
│   └── libs.versions.toml                    # Version catalog (AGP, Kotlin, Libraries)
├── build.gradle.kts                          # Top-level build configuration
└── README.md                                 # Project documentation
```

---

## 🔒 Permissions & Security

| Permission | Purpose |
|---|---|
| `ACCESS_FINE_LOCATION` / `COARSE` | Fetches high-precision GPS coordinates for SOS SMS dispatch. |
| `SEND_SMS`, `READ_SMS`, `RECEIVE_SMS` | Dispatches emergency location alerts and verifies guardian OTPs. |
| `CALL_PHONE` | Executes emergency calls to 112 or guardians directly from lock screen. |
| `CAMERA` & `RECORD_AUDIO` | Captures video and audio evidence silently during an emergency. |
| `FOREGROUND_SERVICE` (Location, Camera, Microphone) | Ensures emergency monitoring remains active when the screen is locked. |
| `POST_NOTIFICATIONS` | Displays continuous safety status and "I Am Safe" action notification. |
| `VIBRATE` | Provides physical alerts and haptic feedback during countdowns. |

---

## ⚙️ Setup & Installation

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer
* **Java Development Kit**: JDK 17, 21, or JetBrains Runtime (JBR)
* **Physical Device**: Recommended for testing sensors (accelerometer, shake, fall), CameraX, and SMS dispatch.

### Steps to Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/HarshJadon1/sahayak-aap.git
   cd sahayak-aap
   ```
2. **Open in Android Studio**:
   * Open Android Studio and select `Open Project` -> choose the project root folder.
   * Allow Gradle to sync dependencies.
3. **Run on Device**:
   * Connect an Android device with **USB Debugging** enabled.
   * Click **Run 'app'** (`Shift + F10`).
   * Grant the requested runtime permissions (Location, SMS, Camera, Microphone) upon first launch.

---

## 🛡️ License & Contributions
Developed for personal safety and open-source assistance. Contributions and feature improvements are welcome via pull requests.
