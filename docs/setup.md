# Bloom — Developer Setup

## Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Ladybug (2024.2.x) or newer recommended |
| JDK | 17 (bundled with Android Studio is fine) |
| Android SDK | API 35 (Android 15) |
| Android emulator or physical device | API 26+ (Android 8.0+) |

---

## Clone

```bash
git clone https://github.com/om0604/Bloom.git
cd Bloom
```

---

## API Key Setup

Bloom uses **Groq** for AI reflections. Get a free key at [console.groq.com](https://console.groq.com).

Add it to `local.properties` in the project root (this file is git-ignored):

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
GROQ_API_KEY=gsk_your_key_here
```

The key is injected at build time as `BuildConfig.GROQ_API_KEY` via:
```kotlin
// app/build.gradle.kts
buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
```

> **Without a key:** All features work normally. AI Reflections shows an inline error message instead.

---

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Install directly to connected device
./gradlew installDebug

# Release APK (requires signing config)
./gradlew assembleRelease
```

---

## Run in Android Studio

1. Open the project root in Android Studio
2. Wait for Gradle sync to complete
3. Select a device (emulator or physical)
4. Press **Run ▶** or `Shift+F10`

---

## Project Structure Quick Reference

```
Bloom/
├── app/
│   ├── build.gradle.kts          # App-level build config
│   ├── schemas/                  # Room schema exports (committed to VCS)
│   └── src/main/java/com/bloom/app/
│       ├── BloomApplication.kt
│       ├── MainActivity.kt
│       ├── ai/GeminiService.kt   # Groq REST client
│       ├── data/                 # Room, DataStore, repositories
│       ├── di/AppContainer.kt    # Manual dependency injection
│       ├── ui/                   # All Compose screens & components
│       └── util/                 # AppEventBus, DateUtils, Constants, etc.
├── docs/                         # This documentation
├── gradle/libs.versions.toml     # Centralized dependency versions
├── local.properties              # SDK path + GROQ_API_KEY (git-ignored)
└── README.md
```

---

## Key Files for Onboarding a New Developer

| File | Why Read It First |
|---|---|
| `di/AppContainer.kt` | Understand all dependencies and their lifetimes |
| `ui/navigation/Screen.kt` | All navigation routes in one place |
| `ui/navigation/BloomNavGraph.kt` | Full navigation graph with transitions |
| `data/model/Mood.kt` | Core domain concept used everywhere |
| `util/AppEventBus.kt` | Cross-ViewModel event system |
| `ai/GeminiService.kt` | AI integration and streaming protocol |

---

## Debugging Tips

### AI Reflection not working
1. Check `local.properties` has `GROQ_API_KEY=...`
2. Check Logcat for tag `GeminiService`
3. Verify the key at [console.groq.com](https://console.groq.com)

### Database issues
- Room schema at `app/schemas/com.bloom.app.data.local.BloomDatabase/1.json`
- Use Android Studio's **Database Inspector** (View → Tool Windows → App Inspection)

### Theme not persisting
- DataStore data is per-user, per-package
- Clear app data in Settings to reset all DataStore preferences

### Crash on startup
- Check Logcat for `BloomApplication` or `AppContainer` tags
- Most likely a `NullPointerException` from initialization order in ViewModels

---

## Gradle Notes

- **AGP 8.5.2** triggers a `compileSdk = 35` compatibility warning — this is suppressed and non-blocking
- **KSP** (Kotlin Symbol Processing) is used instead of kapt for Room code generation — faster builds
- Version catalog at `gradle/libs.versions.toml` — add all new dependencies there
