# 🌱 Bloom — Grow a little every day.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Android](https://img.shields.io/badge/Android-API%2026%2B-green)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen)
![Room](https://img.shields.io/badge/Room-Offline--First-orange)
![Groq](https://img.shields.io/badge/AI-Groq-red)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

A **premium Android journaling application** focused on mindful reflection and emotional wellbeing.

Bloom is offline-first, powered by a local Room database, and enriched with Groq AI-assisted reflections, a living Digital Garden, and a carefully crafted Material 3 Jetpack Compose UI with beautiful animations.

---

## Highlights

- Offline-first journaling powered by Room
- AI-assisted reflections using the Groq API
- Material 3 Jetpack Compose UI
- Custom Digital Garden with growth stages
- Reusable Mood Slider component
- Event-driven architecture using SharedFlow
- Manual dependency injection with AppContainer

---

## Screenshots

| Splash | Onboarding | Home | Journal | Garden | Insights |
|---|---|---|---|---|---|
| ![Splash](docs/images/splash.png) | ![Onboarding](docs/images/onboarding.png) | ![Home](docs/images/home.png) | ![Journal](docs/images/journal.png) | ![Garden](docs/images/garden.png) | ![Insights](docs/images/insights.png) |

---

## Features

| Feature | Description |
|---|---|
| 💧 **Animated Splash Screen** | Branded launch sequence with Lottie animation |
| ✨ **Multi-step Onboarding** | 3-page warm pager with personal name capture |
| 🏠 **Dynamic Greeting** | Time-aware greeting ("Good Morning / Afternoon / Evening") with user's name |
| 🎚️ **Home Mood Slider** | Persistent interactive 5-position slider embedded directly in the Home screen |
| 📝 **Live Mood Description** | Dynamically updates description text as the user drags the slider |
| ✍️ **Journal Editor** | Distraction-free writing with word count, mood selector, and AI loading states |
| 🤖 **AI Reflection (Groq)** | Real-time streaming reflections powered by `llama-3.3-70b-versatile` via Groq REST API |
| 🌱 **Digital Garden** | Canvas-drawn plant that grows through 5 stages (Seed → Sprout → Leaf → Flower → Tree) |
| 📊 **Insights** | Mood breakdown and journaling statistics (locked state under 3 entries) |
| ⚙️ **Settings** | Theme switching, profile name editing, daily reminder toggle |
| 🌙 **Dynamic Light/Dark Mode** | Full warm dark theme (deep brown) with in-app toggle |
| 💾 **Offline Room Database** | All data stored locally — works without internet |
| 📳 **Haptic Feedback** | Haptic response on mood slider drag and save |
| 🔔 **Save Event Architecture** | AppEventBus fires only on genuine journal saves, triggering garden animation + snackbar |
| 📜 **Rotating Affirmations** | 25 curated quotes, auto-rotate every 2 hours |
| 🗑️ **Long-Press to Delete** | Long-press any journal entry for a confirmation delete dialog |

---

## Architecture

```
UI Layer (Compose)
       ↓
ViewModel (StateFlow / UiState)
       ↓
Repository (single source of truth)
       ↓
Room DAO / DataStore / Groq REST API
       ↓
AppContainer (Manual Dependency Injection)
```

### Pattern: MVVM + Repository + Lightweight Clean Architecture

```
com.bloom.app
├── BloomApplication.kt
├── MainActivity.kt
├── ai/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── BloomDatabase.kt
│   ├── repository/
│   └── model/
├── di/
├── ui/
└── util/
```

---

## Demo APK

A demonstration APK is available for reviewers.

The demo APK is intended for UI/UX evaluation. AI reflections require a locally supplied Groq API key.

To protect API credentials, AI Reflections are disabled in the demo build.

To enable AI functionality, clone the repository and add your own:

`GROQ_API_KEY=YOUR_KEY`

in `local.properties`, then rebuild.

[Download Demo APK](https://drive.google.com/file/d/1R5jQ2oaFhK9JnpokJ612mzZ5IHwucwN9/view?usp=sharing)

---

## Tech Stack

| Category | Library / Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Navigation | Jetpack Navigation Compose |
| Database | Room 2.6.1 |
| Preferences | DataStore Preferences 1.1.1 |
| AI | Groq REST API (`llama-3.3-70b-versatile`) |
| Networking | OkHttp 4.12.0 + Gson 2.10.1 |
| Async | Kotlin Coroutines + Flow (StateFlow / SharedFlow) |
| Animations | Compose Animation + Lottie 6.6.0 |
| DI | Manual (AppContainer) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |
| Java | 17 |

---

## Setup

### 1. Clone the project

```bash
git clone https://github.com/om0604/Bloom.git
cd Bloom
```

### 2. Add your Groq API key

Create or open `local.properties` in the project root (never commit this file):

```properties
GROQ_API_KEY=your_groq_api_key_here
```

Obtain a free key at [console.groq.com](https://console.groq.com).

### 3. Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Or open in Android Studio and Run
```

**Requirements:**
- Android Studio (latest stable)
- JDK 17
- Android SDK 35

> **Without a GROQ_API_KEY**, all app features except AI Reflections will work normally. AI Reflections will show an "Invalid API key" error message inline.

---

## Design Decisions

| Decision | Rationale |
|---|---|
| **Persistent Home Mood Slider** | Removes an unnecessary screen (Mood Check-in) — the slider is now always accessible on the Home screen |
| **Groq instead of Gemini** | Groq was selected for its OpenAI-compatible REST API, low latency, and straightforward streaming support. |
| **AppEventBus** | Decouples `JournalViewModel` from `HomeViewModel` — the save snackbar and garden bounce animation trigger only on genuine data mutations, not navigation events |
| **Manual DI** | 5 screens + 2 repositories = Hilt would add unnecessary annotation processing complexity. AppContainer is transparent and migrateable to Hilt in ~30 minutes if needed |
| **StateFlow everywhere** | Single source of truth per screen. UI only reads, never writes state directly |
| **Room over SQLite** | Type-safe queries, coroutine support, schema migration safety, no manual cursor management |
| **No network-first** | Journal data is personal and precious — offline-first ensures zero data loss on connectivity changes |
| **Event-driven save architecture** | UI emits save events, triggering cross-screen animations via `SharedFlow` |

---

## Acknowledgements

Bloom was built as a personal project to explore modern Android development, thoughtful UX design, and AI-assisted journaling while maintaining an offline-first architecture.
