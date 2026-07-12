# Bloom — Codebase Reference

> A file-by-file reference of every significant class, its responsibility, and key methods.

---

## `BloomApplication`
**Package:** `com.bloom.app`

Android `Application` subclass. Creates and holds `AppContainer` on `onCreate()`. Every ViewModel accesses dependencies via:
```kotlin
(context.applicationContext as BloomApplication).container
```

---

## `MainActivity`
**Package:** `com.bloom.app`

Single Activity. Responsibilities:
- Reads `ThemePreference` from `UserPreferences` via `StateFlow`
- Applies `BloomTheme(darkTheme = ...)` at root
- Determines `startDestination` from `isOnboardingComplete`
- Renders `BloomNavGraph` + `BottomNavigation`
- Controls bottom nav visibility (hidden on Splash, Onboarding, Settings, JournalEditor)

---

## AI — `ai/`

### `GeminiService`
**Class name preserved** — internally backed by Groq REST API (not Gemini SDK).

| Member | Description |
|---|---|
| `generateReflection(content, mood, prompt)` | Returns `Flow<ReflectionState>`. Streams SSE response from Groq |
| `buildUserMessage()` | Constructs the user turn for the chat prompt |
| `buildSystemInstruction()` | Defines the AI persona (warm journaling companion, 3–5 sentences) |
| OkHttp client | 15s connect / 30s read timeout |
| Model | `llama-3.3-70b-versatile` |
| Endpoint | `https://api.groq.com/openai/v1/chat/completions` |
| Streaming | Native SSE — reads line-by-line, accumulates delta content |

### `ReflectionState` (sealed class)
```kotlin
sealed class ReflectionState {
    data object Loading : ReflectionState()
    data class Streaming(val text: String) : ReflectionState()
    data class Complete(val text: String) : ReflectionState()
    data class Error(val message: String) : ReflectionState()
}
```

---

## Data — `data/`

### Models (`data/model/`)

#### `Mood` (enum)
Five states: `GREAT`, `GOOD`, `OKAY`, `LOW`, `ROUGH`. Each carries `displayName`, `emoji`, `description`. Safe deserialization via `Mood.fromName(name)`.

#### `GardenStage` (enum)
Five stages: `SEED` (0), `SPROUT` (1+), `LEAF` (4+), `FLOWER` (10+), `TREE` (20+). Derived from total journal entry count via `GardenStage.fromEntryCount(count)`.

#### `JournalEntry` (data class)
Domain model: `id`, `content`, `mood`, `prompt`, `aiReflection`, `wordCount`, `createdAt`, `updatedAt`.

#### `MoodEntry` (data class)
Domain model: `id`, `mood`, `timestamp`.

---

### Entities (`data/local/entity/`)

#### `JournalEntryEntity`
Room entity for `journal_entries` table. Mirrors `JournalEntry` with `@Entity`, `@PrimaryKey(autoGenerate = true)`, and `@ColumnInfo` annotations.

#### `MoodEntryEntity`
Room entity for `mood_entries` table.

#### `EntityMappers`
Extension functions:
- `JournalEntryEntity.toDomain() → JournalEntry`
- `JournalEntry.toEntity() → JournalEntryEntity`
- Same pair for `MoodEntry`

---

### DAOs (`data/local/dao/`)

#### `JournalEntryDao`
| Method | Return | Description |
|---|---|---|
| `observeAllEntries()` | `Flow<List<JournalEntryEntity>>` | All entries, newest first |
| `observeLatestEntry()` | `Flow<JournalEntryEntity?>` | Most recent single entry |
| `observeEntryCount()` | `Flow<Int>` | Total count |
| `observeEntryById(id)` | `Flow<JournalEntryEntity?>` | Single entry for editor |
| `insertEntry(entity)` | `Long` | Returns generated rowId |
| `updateEntry(entity)` | `Unit` | |
| `deleteEntry(entity)` | `Unit` | |
| `updateReflection(id, text, updatedAt)` | `Unit` | Targeted column update |
| `getEntriesSince(startMs)` | `List<JournalEntryEntity>` | Used for streak calculation |

#### `MoodEntryDao`
| Method | Return | Description |
|---|---|---|
| `getTodaysMood(startOfDay)` | `Flow<MoodEntryEntity?>` | Reactive today's mood |
| `insertOrReplaceMood(entity)` | `Unit` | Upsert — only one mood per day |

---

### Repositories (`data/repository/`)

#### `JournalRepository`
| Member | Description |
|---|---|
| `allEntries: Flow<List<JournalEntry>>` | All entries, domain-mapped |
| `latestEntry: Flow<JournalEntry?>` | For Home screen preview |
| `entryCount: Flow<Int>` | Drives `GardenStage` |
| `observeEntry(id)` | For editor — live updates |
| `saveEntry(content, mood, prompt)` | Inserts, returns new id |
| `updateEntry(entry)` | Updates content/mood, recomputes wordCount |
| `saveReflection(entryId, text)` | Targeted reflection column update |
| `deleteEntry(entry)` | Deletes by entity |
| `computeStreak()` | Walks days backwards counting consecutive entries |

#### `MoodRepository`
| Member | Description |
|---|---|
| `todaysMood: Flow<MoodEntry?>` | Reactive today's mood |
| `recordMood(mood)` | Upsert — replaces any prior mood for today |

---

### `BloomDatabase`
- Room v1, two tables: `journal_entries`, `mood_entries`
- Singleton via `@Volatile` + `synchronized` pattern
- Schema exported to `app/schemas/` for migration tooling
- No `fallbackToDestructiveMigration`

---

## DI — `di/`

### `AppContainer`
Service locator. All dependencies created `by lazy {}`:

| Property | Type |
|---|---|
| `userPreferences` | `UserPreferences` |
| `database` (private) | `BloomDatabase` |
| `journalRepository` | `JournalRepository` |
| `moodRepository` | `MoodRepository` |
| `geminiService` | `GeminiService` |

---

## UI — `ui/`

### `ui/navigation/`

#### `Screen` (sealed class)
Type-safe route strings. Screens:
- `Splash`, `Onboarding` (one-time)
- `Home`, `Journal`, `Garden`, `Insights` (bottom nav)
- `Settings`, `JournalEditor` (sub-screens)
- `JournalEditor.createRoute(entryId)` builds parameterized route

#### `BloomNavGraph`
`NavHost` with all composable routes, transition animations, and argument handling.

#### `BottomNavigation`
4-tab bottom bar: Home, Journal, Garden, Insights. Hidden on Splash/Onboarding/Settings/JournalEditor.

---

### `ui/home/`

#### `HomeViewModel`
| State/Function | Description |
|---|---|
| `uiState: StateFlow<HomeUiState>` | Combined stream of name, mood, entry, count, quote |
| `themePreference: StateFlow<ThemePreference>` | From DataStore |
| `setTodaysMood(mood)` | Calls MoodRepository |
| `toggleTheme()` | Writes to DataStore |
| `currentQuoteFlow` (private) | Rotates quotes every 2 hours based on `currentHour / 2` |

#### `HomeScreen`
- Staggered `AnimatedVisibility` entrance for each section
- `MoodSection` renders `MoodSlider` component
- `AppEventBus.events` collected in `LaunchedEffect(Unit)` — triggers snackbar + animation on `JournalEntrySaved`

---

### `ui/journal/`

#### `JournalViewModel`
Shared ViewModel for both list and editor screens.

| State/Function | Description |
|---|---|
| `listState: StateFlow<JournalListUiState>` | All entries for list screen |
| `editorState: StateFlow<JournalEditorUiState>` | Editor form state |
| `loadEntry(entryId)` | Loads existing entry or initializes new |
| `onContentChanged(text)` | Updates content + wordCount |
| `onMoodSelected(mood)` | Updates selected mood |
| `saveEntry()` | Saves/updates; emits `JournalEntrySaved` only if content/mood changed |
| `deleteEntry(entry)` | Deletes via repository |
| `requestReflection()` | Triggers Groq AI reflection stream |

#### `JournalEditorUiState`
Snapshots `originalContent` and `originalMood` to detect genuine mutations before emitting save events.

#### `JournalListScreen`
- Groups entries by date label (Today, Yesterday, date)
- Long-press on any card → `AlertDialog` confirmation → delete
- FAB for new entry (pill shape)

---

### `ui/garden/`

#### `GardenViewModel`
Reads `entryCount` from `JournalRepository`, derives `GardenStage`, exposes `StateFlow<GardenUiState>`.

#### `GardenScreen`
- Canvas-drawn plant animation with breathing effect
- Stage progression display (Seed → Tree)
- Motivational copy per stage

---

### `ui/components/`

#### `BloomComponents.kt`
| Component | Purpose |
|---|---|
| `BloomCard` | Standard rounded card with press animation, supports `onClick` + `onLongClick` |
| `BloomPrimaryButton` | Pill-shaped amber CTA |
| `BloomTextButton` | Text-only secondary action |
| `QuoteCard` | Italic quote + author attribution |
| `StreakBadge` | 🌱 streak indicator pill |
| `SectionHeader` | Consistent section label |
| `BloomDivider` | 0.5dp hairline divider |
| `HomeShimmerLoading` / `BloomShimmerBlock` | Animated shimmer placeholders |

#### `MoodSlider.kt`
Reusable 5-position drag slider.
- Tracks pointer position via `detectHorizontalDragGestures` + `detectTapGestures`
- Animates thumb offset with `spring(stiffness = MediumLow)`
- Haptic feedback on each mood snap (`TextHandleMove`)
- Emits `onMoodHover(mood)` during drag, `onMoodSelect(mood)` on release/tap
- Label positions computed with exact pixel offsets (not weight-based) for perfect alignment

---

## Utilities — `util/`

### `AppEventBus`
Singleton `SharedFlow<AppEvent>` bus.
- `emit(event)` — non-suspending, uses `tryEmit` with `extraBufferCapacity = 1`
- Currently one event: `AppEvent.JournalEntrySaved`

### `UserPreferences`
DataStore wrapper with `Flow<T>` properties and `suspend` setters for:
- `isOnboardingComplete`, `userName`, `themePreference`, `dailyReminderEnabled`

### `Constants`
- `JOURNAL_PROMPTS` — 10 rotating writing prompts (day-of-year indexed)
- `DAILY_QUOTES` — 25 curated affirmations (index rotates every 2 hours via `currentHour / 2`)
- DataStore key constants

### `DateUtils`
- `getGreeting()` — returns "Good Morning/Afternoon/Evening" based on hour
- `formatEntryDate(millis)` — returns "Today", "Yesterday", or formatted date
- `formatTime(millis)` — 12h time string
- `millisToLocalDate(millis)` — converts epoch ms to `LocalDate`
- `startOfToday()` — midnight epoch ms for streak calculation

### `Extensions`
Kotlin extension functions for common UI / data operations.
