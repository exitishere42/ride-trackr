# Ride Trackr 🎢

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A modern, high-performance native Android application for theme park enthusiasts and park visitors. **Ride Trackr** delivers real-time wait times, Wikipedia-powered technical ride specifications, dynamic seasonal crowd modeling, and a local on-site reporting database to calibrate wait times on the go.

---

## ✨ Features

- **Dual-Source Wait Times**: Aggregates and cross-verifies live wait times from independent APIs (*ThemeParks.wiki* and *Wartezeiten.APP*).
- **Wikipedia Media & Deep Dive**: Live integration with MediaWiki REST APIs fetching authentic high-res attraction photos, manufacturer details (Gerstlauer, Mack, Vekoma, B&M, Intamin), top speed, track length, height, inversions, and opening year.
- **Dynamic Seasonal Simulation (Tripsdrill)**: For parks without public live APIs (such as *Erlebnispark Tripsdrill*), Ride Trackr provides a time-of-day and seasonal calculation engine:
  - **Time of Day**: Light queues in the morning (09:00-10:00), lunch rush peak (12:00-14:00), evening drop (17:00-18:00).
  - **Seasonality & Months**: July/August summer surge, October Halloween peaks (*Schaurige Altweibernächte*), winter pause handling.
  - **Water Ride Heat Boost**: Automatic demand multipliers for flumes and raftings during hot summer months.
  - **Regional Online Crowd Index**: Ingests real-time state-level crowd data from Baden-Württemberg.
- **On-Site Wait Time Reporting Database**:
  - Offline-first **Room database** storing personal wait time reports submitted on site.
  - Immediately overrides and calibrates calculations with recent observations.
  - Displays badges such as *"Own Report (15 min ago): 25 min"*.
- **Modern Dark UI**:
  - Floating pill toolbar navigation with smooth crossfade transitions.
  - Customizable color accents.
  - Clean, informative specification cards without cluttered wording.
- **Bilingual**: Seamless switching between German and English.

---

## 🛠️ Architecture & Tech Stack

- **UI**: Jetpack Compose, Compose Navigation.
- **Architecture**: Modern Android Architecture (MVVM, UDF, Repository Pattern).
- **Dependency Injection**: Dagger Hilt.
- **Local Persistence**: Room SQLite Database & Jetpack DataStore (Preferences).
- **Networking**: Retrofit 2, OkHttp 3, Kotlinx Serialization.
- **Image Loading**: Coil Compose for asynchronous Wikimedia caching.
- **Asynchronous**: Kotlin Coroutines & StateFlow.

---

## 📱 Installation

Download the latest APK release from the [Releases](https://github.com/exitishere42/ride-trackr/releases) section or install via ADB:

```bash
adb install -r -d app-debug.apk
```

---

## 🏗️ Build from Source

```bash
# Clone the repository
git clone https://github.com/exitishere42/ride-trackr.git
cd ride-trackr

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

---

## 📄 License

This project is open-source under the MIT License.
