# 🌱 Bloom — Grow a little every day.

A premium Android journaling application focused on **emotional wellbeing**, not productivity.

Bloom helps people develop a habit of mindful reflection through a calm, beautiful, and distraction-free experience.

---

## Screenshots

> _Run the app on an emulator to see the full experience._

| Onboarding | Home | Mood Check-in | Journal | Garden | Insights |
|---|---|---|---|---|---|
| _(placeholder)_ | _(placeholder)_ | _(placeholder)_ | _(placeholder)_ | _(placeholder)_ | _(placeholder)_ |

---

## Features

| Feature | Description |
|---|---|
| 💧 **Splash Screen** | Branded animated launch sequence (Logo -> Title -> Tagline) |
| ✨ **Onboarding** | 3-page warm pager with personal name capture |
| 🏠 **Home** | Greeting, mood check-in, today's entry, garden preview, daily quote |
| 🌤️ **Mood Check-in** | 5 animated mood cards with spring physics and haptic feedback |
| ✍️ **Journal Editor** | Distraction-free writing with AI loading states & success haptics |
| ✨ **AI Reflection** | Gemini-powered warm reflection (rotating loading phrases, inline) |
| 🌱 **Digital Garden** | Canvas-drawn plant with breathing animation & empty state |
| 📊 **Insights** | Locked state (<3 entries), animated numbers, mood breakdown |
| ⚙️ **Settings** | Theme preferences (Dynamic colors support), Notifications, Privacy |
| 🌙 **Dark Mode & Theming** | Full warm dark theme (deep brown) with optional Material You support |
| 📱 **UI Polish** | Edge-to-edge UI, Shimmer loading placeholders, Staggered animations |

---

## Architecture

```
UI Layer          →   ViewModel (StateFlow/UiState)
ViewModel         →   Repository
Repository        →   Room DAO / GeminiService
Application       →   AppContainer (Manual DI)
```

### Pattern: MVVM + Repository + Lightweight Clean Architecture

- **One module** — no unnecessary module graph for this scope
- **Manual DI** — AppContainer in BloomApplication, no Hilt overhead
- **Immutable UI state** — every screen has a data-class UiState
- **StateFlow** — reactive, lifecycle-aware, no LiveData
- **Offline-first** — Room for all data, Gemini only on explicit request
- **Domain/Entity separation** — Room entities never leak into ViewModel or UI

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  HomeScreen  MoodScreen  JournalEditor  Garden  Insights │
└──────────────────────┬───────────────────────────────────┘
                       │ StateFlow<UiState>
┌──────────────────────▼───────────────────────────────────┐
│                    ViewModel Layer                        │
│  HomeVM  MoodVM  JournalVM  GardenVM  InsightsVM         │
└──────────────────────┬───────────────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────────────┐
│                  Repository Layer                         │
│       JournalRepository    MoodRepository                │
└────────────┬────────────────────────┬────────────────────┘
             │                        │
┌────────────▼──────────┐  ┌──────────▼──────────┐
│      Room DB          │  │   GeminiService      │
│  JournalEntryDao      │  │  (streaming Flow)    │
│  MoodEntryDao         │  └─────────────────────-┘
└───────────────────────┘
             │
┌────────────▼──────────┐
│    AppContainer       │  ← BloomApplication owns this
│   (Manual DI)         │
└───────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| State | StateFlow + UiState data classes |
| Database | Room 2.6 |
| Preferences | DataStore |
| Async | Coroutines + Flow |
| DI | Manual (AppContainer) |
| AI | Gemini 1.5 Flash (generative-ai SDK) |
| Animations | Compose built-in + Canvas |
| Fonts | Lora (serif) + DM Sans (sans-serif) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 |

---

## Folder Structure

```
com.bloom.app/
├── BloomApplication.kt       # App entry point, owns AppContainer
├── MainActivity.kt           # Single activity, edge-to-edge, splash
│
├── ai/
│   └── GeminiService.kt      # Streaming Gemini integration
│
├── data/
│   ├── local/
│   │   ├── BloomDatabase.kt  # Room database singleton
│   │   ├── dao/              # JournalEntryDao, MoodEntryDao
│   │   └── entity/           # Room entities + mappers
│   ├── model/                # Domain models (Mood, JournalEntry, etc.)
│   └── repository/           # JournalRepository, MoodRepository
│
├── di/
│   └── AppContainer.kt       # Manual DI — all dependencies
│
├── ui/
│   ├── navigation/           # Screen.kt, BloomNavGraph, BottomNavigation
│   ├── theme/                # Color, Type, Shape, BloomTheme
│   ├── components/           # Reusable: BloomCard, StreakBadge, etc.
│   ├── onboarding/           # OnboardingScreen + ViewModel
│   ├── home/                 # HomeScreen + ViewModel
│   ├── mood/                 # MoodCheckInScreen + ViewModel
│   ├── journal/              # JournalEditorScreen + ListScreen + ViewModel
│   ├── garden/               # GardenScreen + ViewModel + GardenPlant
│   ├── splash/               # SplashScreen
│   ├── settings/             # SettingsScreen + ViewModel
│   └── insights/             # InsightsScreen + ViewModel
│
└── util/
    ├── Constants.kt          # Prompts, quotes, DataStore keys
    ├── DateUtils.kt          # java.time helpers, greeting, formatting
    └── UserPreferences.kt    # DataStore wrapper
```

---

## Setup Guide

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK API 35
- Java 17
- A Gemini API key

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/bloom-android.git
cd bloom-android
```

### 2. Add your Gemini API key
Open (or create) `local.properties` in the root directory:
```properties
GEMINI_API_KEY=your_gemini_api_key_here
```
Get a key at: https://aistudio.google.com/app/apikey

> WARNING: `local.properties` is in `.gitignore` — never commit your API key.

### 3. Download fonts
Download from Google Fonts and place in `app/src/main/res/font/`:

**Lora** (https://fonts.google.com/specimen/Lora):
- `lora_regular.ttf`, `lora_medium.ttf`, `lora_semibold.ttf`
- `lora_bold.ttf`, `lora_italic.ttf`, `lora_bold_italic.ttf`

**DM Sans** (https://fonts.google.com/specimen/DM+Sans):
- `dmsans_regular.ttf`, `dmsans_medium.ttf`
- `dmsans_semibold.ttf`, `dmsans_bold.ttf`

### 4. Build and run
```bash
./gradlew assembleDebug
```
Or press Run in Android Studio.

---

## Design Decisions

| Decision | Choice | Why |
|---|---|---|
| No title in journal | Intentional | People don't title private journals |
| 5 moods, not a slider | Fixed options | Sliders cause decision paralysis |
| Manual DI, not Hilt | AppContainer | No overhead for single-module scope |
| Dynamic color off | Brand palette | Bloom's warmth is intentional identity |
| Canvas for garden | Custom | Full control over growth animations |
| Lora + DM Sans | Two fonts | Warmth (editorial) + Clarity (UI) |
| No tabs for mood | Contextual | 30-second action, not a destination |

---

## Future Roadmap

- [ ] Reminders — gentle, optional daily notification
- [ ] Export — PDF/text export of journal entries  
- [ ] Tags — free-form emotional tags on entries
- [ ] Weekly wrap — AI-generated weekly reflection summary
- [ ] Themes — additional seasonal color palettes
- [ ] Widget — home screen mood check-in widget
- [ ] Accessibility — full TalkBack audit
- [ ] Localization — Spanish, French, German
- [ ] Backup — encrypted local backup/restore

---

## Philosophy

Bloom is not a CRUD app. It is not a Gemini demo. It is not a Compose showcase.

It is a premium Android experience that encourages calmness, reflection, and consistency.
Every screen should communicate one thing: *you can do this, and it's worth doing.*

---

*Built with care. No backend. No ads. No social features. Just you and your thoughts.*
