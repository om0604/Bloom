# Bloom — Architecture

> Source of truth: the current codebase at `app/src/main/java/com/bloom/app/`

---

## Overview

Bloom follows **MVVM + Repository pattern** with lightweight clean architecture layering, manual dependency injection, and a unidirectional data flow. The app is entirely offline-first; network is used only for optional AI reflections.

---

## High-Level Layer Map

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│  Composable Screens  ←  ViewModel (StateFlow/UiState)   │
└──────────────────────────┬──────────────────────────────┘
                           │ observes / calls
┌──────────────────────────▼──────────────────────────────┐
│                    Domain / Data Layer                   │
│  Repository  →  Room DAO  |  DataStore  |  GroqService  │
└──────────────────────────┬──────────────────────────────┘
                           │ created by
┌──────────────────────────▼──────────────────────────────┐
│                   Dependency Container                   │
│  AppContainer (held by BloomApplication)                │
└─────────────────────────────────────────────────────────┘
```

---

## Layer Responsibilities

### UI Layer (`ui/`)
- Pure Compose — no business logic
- Each screen reads from exactly **one ViewModel** via `StateFlow`
- UI never calls repositories directly
- All side effects (save, delete, AI call) triggered via ViewModel functions

### ViewModel Layer
- Owns screen-specific `UiState` data class
- Exposes `StateFlow<UiState>` — never `MutableStateFlow` to UI
- Calls repository functions, maps results into UiState
- Emits `AppEvent` via `AppEventBus` for cross-screen signals

### Repository Layer (`data/repository/`)
- Single source of truth for its domain
- Wraps Room DAO calls, maps entities ↔ domain models
- Exposes reactive `Flow<T>` — UI always sees latest data
- Pure data — no UI knowledge, no event bus usage

### Data Layer (`data/local/`)
- **Room**: `BloomDatabase` with `JournalEntryEntity` and `MoodEntryEntity`
- **DataStore**: `UserPreferences` wrapping onboarding flag, user name, theme, reminder toggle
- **EntityMappers**: `toEntity()` / `toDomain()` extension functions keep entity/domain separation clean

### AI Layer (`ai/`)
- `GroqService` class (internally Groq-backed)
- Uses OkHttp for HTTP + SSE streaming
- Returns `Flow<ReflectionState>` — UI observes state transitions
- No coupling to ViewModels; called only from `JournalViewModel`

### DI Layer (`di/`)
- `AppContainer`: manual service locator with `lazy {}` initialization
- Held by `BloomApplication` — single instance per process
- ViewModels access it via `(context.applicationContext as BloomApplication).container`

---

## Data Flow

### Journal Save Flow
```
JournalEditorScreen
    ↓ (user navigates back)
JournalViewModel.saveEntry()
    ↓
JournalRepository.saveEntry() / updateEntry()
    ↓
Room (JournalEntryDao.insertEntry / updateEntry)
    ↓ (if content changed)
AppEventBus.emit(JournalEntrySaved)
    ↓
HomeScreen (LaunchedEffect observes events)
    ↓
Garden bounce animation + "Reflection Saved" Snackbar
```

### Mood Flow
```
HomeScreen MoodSlider (user drags/snaps)
    ↓
HomeViewModel.setTodaysMood(mood)
    ↓
MoodRepository.recordMood(mood)
    ↓
Room (MoodEntryDao.insertOrReplace)
    ↓
MoodRepository.todaysMood (Flow)
    ↓
HomeViewModel.uiState (StateFlow)
    ↓
HomeScreen recomposition
```

### AI Reflection Flow
```
JournalEditorScreen → "Get Reflection" tap
    ↓
JournalViewModel.requestReflection()
    ↓
GroqService.generateReflection(content, mood, prompt)
    ↓
OkHttp → POST https://api.groq.com/openai/v1/chat/completions
         (model: llama-3.3-70b-versatile, stream: true)
    ↓
SSE streaming response line-by-line
    ↓
emit ReflectionState.Streaming(accumulatedText)
    ↓
JournalEditorScreen renders text progressively
    ↓
Flow completes → emit ReflectionState.Complete
    ↓
JournalRepository.saveReflection(entryId, text)
```

### App Startup Flow
```
SplashScreen (Lottie animation)
    ↓
DataStore.isOnboardingComplete
    ├─ false → OnboardingScreen → Home
    └─ true  → HomeScreen
    ↓
DataStore.themePreference → Theme applied in MainActivity
```

---

## State Management

- **`StateFlow<UiState>`**: One per ViewModel. UI collects with `collectAsStateWithLifecycle()`.
- **`SharedFlow<AppEvent>`**: `AppEventBus` — cross-ViewModel signals with `extraBufferCapacity = 1` (fire-and-forget, non-blocking).
- **Room Flows**: DAOs return `Flow<T>`. Room automatically re-emits on table changes.
- **DataStore Flows**: `UserPreferences` exposes Kotlin `Flow<T>` properties backed by `DataStore<Preferences>`.

---

## Navigation

Single-activity, fully Compose-based with `NavHostController`.

### Screens
| Route | Class | Type |
|---|---|---|
| `splash` | `SplashScreen` | Full screen, no bottom nav |
| `onboarding` | `OnboardingScreen` | Full screen, no bottom nav |
| `home` | `HomeScreen` | Bottom nav tab |
| `journal` | `JournalListScreen` | Bottom nav tab |
| `garden` | `GardenScreen` | Bottom nav tab |
| `insights` | `InsightsScreen` | Bottom nav tab |
| `settings` | `SettingsScreen` | Sub-screen (slides in from right) |
| `journal_editor/{entryId}` | `JournalEditorScreen` | Sub-screen (slides up from bottom) |

### Transitions
- Bottom nav switching → **Fade** (calm, no direction confusion)
- Journal Editor open → **Slide up** (opening a page feel)
- Journal Editor close → **Slide down** (natural back gesture)
- Settings open/close → **Slide horizontal**
- All transitions: **300ms**

---

## Persistence

### Room Database (`bloom_database`, version 1)
- `JournalEntryEntity`: `id`, `content`, `mood`, `prompt`, `aiReflection`, `wordCount`, `createdAt`, `updatedAt`
- `MoodEntryEntity`: `id`, `mood`, `timestamp`
- Schema exported to `app/schemas/` for migration safety
- **No `fallbackToDestructiveMigration`** — user journal data is never wiped silently

### DataStore (Preferences)
| Key | Type | Purpose |
|---|---|---|
| `onboarding_complete` | Boolean | Routes splash to onboarding vs. home |
| `user_name` | String | Shown in greeting and profile |
| `theme_preference` | String | SYSTEM / LIGHT / DARK |
| `daily_reminder_enabled` | Boolean | Controls notification scheduling |

---

## Theme System

- `ThemePreference` enum: `SYSTEM`, `LIGHT`, `DARK`
- Persisted in DataStore, read as `StateFlow<ThemePreference>` in `HomeViewModel`
- Applied at `MainActivity` root before `BloomNavGraph`
- Material 3 `ColorScheme` with custom warm palette (amber primary, deep brown dark surface)

---

## Event System

`AppEventBus` is an `object` singleton using `MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)`.

**Current events:**
- `AppEvent.JournalEntrySaved` — emitted by `JournalViewModel.saveEntry()` only when content or mood actually changed compared to original snapshot.

**Consumers:**
- `HomeScreen` — collects in `LaunchedEffect(Unit)` to trigger snackbar and garden animation.

**Design rule:** Repositories never emit to `AppEventBus`. Only ViewModels do.

---

## Dependency Graph

```
BloomApplication
    └── AppContainer
            ├── UserPreferences (DataStore)
            ├── BloomDatabase
            │       ├── JournalEntryDao
            │       │       └── JournalRepository ← JournalViewModel
            │       └── MoodEntryDao
            │               └── MoodRepository ← HomeViewModel
            └── GroqService ← JournalViewModel
```
