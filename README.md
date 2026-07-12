# 🌱 Bloom — Grow a little every day.

A **premium Android journaling application** focused on mindful reflection and emotional wellbeing.

Bloom is offline-first, powered by a local Room database, and enriched with Groq AI-assisted reflections, a living Digital Garden, and a carefully crafted Material 3 Jetpack Compose UI with beautiful animations.

---

## Screenshots

| Splash | Onboarding | Home | Journal | Garden | Insights |
|---|---|---|---|---|---|
| _(run app to see)_ | _(run app to see)_ | _(run app to see)_ | _(run app to see)_ | _(run app to see)_ | _(run app to see)_ |

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
Room DAO / DataStore / GeminiService (Groq REST)
       ↓
AppContainer (Manual Dependency Injection)
```

### Pattern: MVVM + Repository + Lightweight Clean Architecture

```
com.bloom.app
├── BloomApplication.kt         # Application class — owns AppContainer
├── MainActivity.kt             # Single Activity — hosts NavGraph + BottomNav
├── ai/
│   └── GeminiService.kt        # Groq REST API client (retains class name for stability)
├── data/
│   ├── local/
│   │   ├── BloomDatabase.kt    # Room database definition (v1)
│   │   ├── dao/
│   │   │   ├── JournalEntryDao.kt
│   │   │   └── MoodEntryDao.kt
│   │   └── entity/
│   │       ├── JournalEntryEntity.kt
│   │       ├── MoodEntryEntity.kt
│   │       └── EntityMappers.kt
│   ├── model/
│   │   ├── GardenStage.kt      # Enum: SEED → SPROUT → LEAF → FLOWER → TREE
│   │   ├── JournalEntry.kt     # Domain model
│   │   ├── Mood.kt             # Enum: GREAT / GOOD / OKAY / LOW / ROUGH
│   │   └── MoodEntry.kt        # Domain model
│   └── repository/
│       ├── JournalRepository.kt
│       └── MoodRepository.kt
├── di/
│   └── AppContainer.kt         # Manual DI — lazy service locator
├── ui/
│   ├── components/
│   │   ├── BloomComponents.kt  # BloomCard, BloomPrimaryButton, QuoteCard, StreakBadge, etc.
│   │   └── MoodSlider.kt       # Reusable 5-position mood drag slider
│   ├── garden/
│   │   ├── GardenScreen.kt
│   │   └── GardenViewModel.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── insights/
│   │   └── InsightsScreen.kt
│   ├── journal/
│   │   ├── JournalEditorScreen.kt
│   │   ├── JournalListScreen.kt
│   │   └── JournalViewModel.kt
│   ├── navigation/
│   │   ├── BloomNavGraph.kt
│   │   ├── BottomNavigation.kt
│   │   └── Screen.kt
│   ├── onboarding/
│   │   └── OnboardingScreen.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   ├── splash/
│   │   └── SplashScreen.kt
│   └── theme/
│       └── (Theme.kt, Color.kt, Type.kt, Shape.kt)
└── util/
    ├── AppEventBus.kt          # Cross-ViewModel event bus (SharedFlow)
    ├── Constants.kt            # Journal prompts, daily quotes (25)
    ├── DateUtils.kt
    ├── Extensions.kt
    └── UserPreferences.kt      # DataStore wrapper
```

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
| Async | Kotlin Coroutines + Flow |
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
- Android Studio Ladybug (2024.2.x) or newer
- JDK 17
- Android SDK 35

> **Without a GROQ_API_KEY**, all app features except AI Reflections will work normally. AI Reflections will show an "Invalid API key" error message inline.

---

## Design Decisions

| Decision | Rationale |
|---|---|
| **Persistent Home Mood Slider** | Removes an unnecessary screen (Mood Check-in) — the slider is now always accessible on the Home screen |
| **Groq instead of Gemini** | Gemini SDK model availability issues in this project's API configuration; Groq's OpenAI-compatible REST API is simpler, more reliable, and supports true SSE streaming |
| **AppEventBus** | Decouples `JournalViewModel` from `HomeViewModel` — the save snackbar and garden bounce animation trigger only on genuine data mutations, not navigation events |
| **Manual DI** | 5 screens + 2 repositories = Hilt would add unnecessary annotation processing complexity. AppContainer is transparent and migrateable to Hilt in ~30 minutes if needed |
| **StateFlow everywhere** | Single source of truth per screen. UI only reads, never writes state directly |
| **Room over SQLite** | Type-safe queries, coroutine support, schema migration safety, no manual cursor management |
| **No network-first** | Journal data is personal and precious — offline-first ensures zero data loss on connectivity changes |
| **GeminiService class name preserved** | The class was renamed internally (Groq-backed) but retains its name to minimize Git diff noise and architectural churn |

---

## Roadmap

- [ ] Scheduled daily reminder notifications
- [ ] iCloud / Google Drive journal backup
- [ ] Streak freezes / grace days
- [ ] Weekly Insights report
- [ ] Multiple journal themes / fonts
- [ ] Search across entries
- [ ] AI provider abstraction layer (swap Groq → any OpenAI-compatible endpoint)
