# Bloom — UI Components

> Documentation for every reusable Compose component and screen-level composable.

---

## Reusable Components (`ui/components/`)

### `BloomCard`
**File:** `BloomComponents.kt`

The standard content card for Bloom. Used for journal entry previews, garden preview, quote card wrapper, and any grouped content.

```kotlin
BloomCard(
    modifier    = Modifier.fillMaxWidth(),
    onClick     = { /* optional tap handler */ },
    onLongClick = { /* optional long-press handler */ },
) {
    // Column content here
}
```

- Rounded corners (`MaterialTheme.shapes.large`)
- Animated elevation: 4dp → 1dp on press
- Uses `combinedClickable` — supports both `onClick` and `onLongClick`
- Warm ambient shadow using primary color tint

---

### `BloomPrimaryButton`
Pill-shaped primary CTA button.
```kotlin
BloomPrimaryButton(
    text        = "Write today's entry",
    onClick     = { ... },
    modifier    = Modifier.fillMaxWidth(),
    leadingIcon = { Text("✍️") }  // optional
)
```
- 52dp height, pill shape
- Amber fill, white text
- Zero elevation (flat design)

---

### `BloomTextButton`
Secondary text-only action.
```kotlin
BloomTextButton(text = "Skip", onClick = { ... })
```

---

### `QuoteCard`
Displays the daily rotating affirmation on the Home screen.
```kotlin
QuoteCard(quote = "Rest is not idleness.", author = "John Lubbock")
```
- Italic body text
- `— Author` attribution line below
- Wrapped in `BloomCard`

---

### `StreakBadge`
Compact pill showing the user's journaling streak.
```kotlin
StreakBadge(streakDays = 7)
// Renders: 🌱 7 Day Reflection Streak
```

---

### `SectionHeader`
Consistent small label for content sections.
```kotlin
SectionHeader(text = "How are you feeling?")
```

---

### `BloomShimmerBlock` / `HomeShimmerLoading`
Animated shimmer loading placeholders shown during initial data load.

```kotlin
BloomShimmerBlock(modifier = Modifier.height(100.dp).fillMaxWidth())
```

The shimmer effect is a `Modifier.shimmerEffect()` extension using `rememberInfiniteTransition`.

---

### `MoodSlider`
**File:** `MoodSlider.kt`

Reusable 5-position interactive mood slider. Used on the Home screen.

```kotlin
MoodSlider(
    selectedMood = todaysMood?.mood,
    onMoodSelect = { mood -> viewModel.setTodaysMood(mood) },
    onMoodHover  = { mood -> currentlyActiveMood = mood }
)
```

#### Internals
- `trackWidth`: measured via `onSizeChanged { }` — never hardcoded
- `segmentWidth = trackWidth / (moods.size - 1)` — 4 equal segments for 5 moods
- Thumb offset animated with `spring(stiffness = MediumLow)`
- Color animated with `animateColorAsState(tween(300))`
- Thumb scale: 1.0 (no selection) → 1.1 (selected) → 1.2 (dragging)
- Label positions: exact pixel offset `index * segmentWidth - 24.dp.toPx()` (not `weight()` — ensures perfect alignment with tick marks)
- Haptic: `HapticFeedbackType.TextHandleMove` on each mood snap during drag
- 24dp horizontal padding on track and labels for comfortable mobile proportions

#### Mood Color Mapping (`fun Mood.toColor(): Color`)
```
GREAT → MoodGreat (warm amber)
GOOD  → MoodGood  (soft green)
OKAY  → MoodOkay  (muted blue-grey)
LOW   → MoodLow   (deep blue)
ROUGH → MoodRough (muted red)
```

---

## Screen-Level Components

### `HomeScreen`
Sections (all animated with staggered `AnimatedVisibility`):
1. **Header** — greeting, username, streak badge, theme toggle, settings icon
2. **MoodSection** — "How are you feeling?" + `MoodSlider` + live description text
3. **WriteSection** — Today's entry preview (tap to continue) or "Write today's entry" CTA
4. **GardenPreviewSection** — Garden stage name, description, animated `GardenPlant`
5. **QuoteCard** — Rotating affirmation

**Event bus collection:**
```kotlin
LaunchedEffect(Unit) {
    AppEventBus.events.collect { event ->
        if (event is AppEvent.JournalEntrySaved) {
            gardenBounce = true
            snackbarHostState.showSnackbar("🌱 Reflection Saved\nYour garden grew today.")
            gardenBounce = false
        }
    }
}
```

---

### `JournalListScreen`
- Grouped by date label: "Today", "Yesterday", or full date
- Animated entry appearance with `slideInVertically` + `fadeIn`
- Long-press triggers `AlertDialog` with "Yes / No" delete confirmation
- FAB (pill shape): "+ New entry"

---

### `JournalEditorScreen`
- Full-screen distraction-free editor
- Back arrow triggers auto-save via `BackHandler`
- Word count displayed in top bar
- Mood selector row (5 mood buttons)
- Journal prompt shown as placeholder text
- "Get Reflection" button at bottom
- AI reflection section: shows `Loading` → `Streaming` → full text → `Error`

---

### `GardenScreen`
- Canvas-drawn plant animation (breathing / idle motion)
- Stage progression bars
- Descriptive copy per stage
- Encourages consistent journaling without gamification language

---

### `InsightsScreen`
- Locked behind 3-entry threshold (shows teaser if under)
- Mood distribution breakdown
- Entry count and streak statistics
- Animated number reveals

---

### `OnboardingScreen`
- 3-page `HorizontalPager`
- Page 3 captures user name via `TextField`
- On complete: saves name + `onboardingComplete = true` to DataStore, navigates to Home

---

### `SettingsScreen`
- Editable profile name field
- Theme toggle (System / Light / Dark)
- Daily reminder toggle
- Version info

---

## Design System Tokens

Defined in `ui/theme/`:
- **Colors:** Warm amber primary, deep brown dark background, off-white light background, 5 mood colors
- **Typography:** Custom font stack (or system default fallback), `DisplaySmall` for name greeting, `BodyLarge` for journal content
- **Shapes:** `small` = 8dp, `medium` = 12dp, `large` = 16dp, `extraLarge` = 24dp, `PillShape` = 50dp full-round
- **Motion:** All transitions 300ms, spring animations for interactive elements
