# Bloom — Data Flow

> Detailed diagrams for every major data flow in the application.

---

## Overall Architecture

```mermaid
graph TD
    subgraph UI["UI Layer (Compose)"]
        HS[HomeScreen]
        JLS[JournalListScreen]
        JES[JournalEditorScreen]
        GS[GardenScreen]
        IS[InsightsScreen]
        SS[SettingsScreen]
    end

    subgraph VM["ViewModel Layer"]
        HVM[HomeViewModel]
        JVM[JournalViewModel]
        GVM[GardenViewModel]
    end

    subgraph REPO["Repository Layer"]
        JR[JournalRepository]
        MR[MoodRepository]
    end

    subgraph PERSIST["Persistence Layer"]
        ROOM[(Room Database)]
        DS[(DataStore)]
    end

    subgraph AI["AI Layer"]
        GROQ[GeminiService\nGroq REST]
    end

    subgraph BUS["Event Bus"]
        AEB[AppEventBus\nSharedFlow]
    end

    HS --> HVM
    JLS --> JVM
    JES --> JVM
    GS --> GVM

    HVM --> JR
    HVM --> MR
    HVM --> DS
    JVM --> JR
    JVM --> GROQ
    JVM --> AEB
    GVM --> JR

    JR --> ROOM
    MR --> ROOM
    DS --> DS

    AEB --> HS
```

---

## Navigation Flow

```mermaid
flowchart TD
    APP[App Launch] --> SPLASH[SplashScreen\nLottie animation]
    SPLASH --> DS{DataStore\nonboardingComplete?}
    DS -- false --> OB[OnboardingScreen\n3-page pager + name]
    DS -- true --> HOME
    OB --> HOME[HomeScreen\nbottom nav root]

    HOME --> JE[JournalEditorScreen\nslide up]
    HOME --> SET[SettingsScreen\nslide right]

    JOURNAL[JournalListScreen] --> JE
    JE --> JOURNAL

    HOME <--> JOURNAL
    HOME <--> GARDEN[GardenScreen]
    HOME <--> INSIGHTS[InsightsScreen]
```

---

## Journal Save Flow

```mermaid
sequenceDiagram
    participant U as User
    participant JES as JournalEditorScreen
    participant JVM as JournalViewModel
    participant JR as JournalRepository
    participant ROOM as Room DB
    participant BUS as AppEventBus
    participant HS as HomeScreen

    U->>JES: navigates back (auto-save)
    JES->>JVM: saveEntry()
    JVM->>JVM: compare content vs originalContent
    JVM->>JR: saveEntry() or updateEntry()
    JR->>ROOM: insertEntry() or updateEntry()
    ROOM-->>JR: success
    JR-->>JVM: done
    alt content changed
        JVM->>BUS: emit(JournalEntrySaved)
        BUS-->>HS: LaunchedEffect receives event
        HS->>HS: gardenBounce = true
        HS->>HS: showSnackbar("🌱 Reflection Saved")
    end
```

---

## AI Reflection Flow

```mermaid
sequenceDiagram
    participant U as User
    participant JES as JournalEditorScreen
    participant JVM as JournalViewModel
    participant SVC as GeminiService (Groq)
    participant API as api.groq.com
    participant ROOM as Room DB

    U->>JES: taps "Get Reflection"
    JES->>JVM: requestReflection()
    JVM->>SVC: generateReflection(content, mood, prompt)
    SVC->>API: POST /openai/v1/chat/completions\n(stream: true)
    loop SSE streaming
        API-->>SVC: data: {"delta":{"content":"..."}}
        SVC->>JVM: emit ReflectionState.Streaming(text)
        JVM->>JES: editorState updated
        JES->>JES: renders text progressively
    end
    API-->>SVC: data: [DONE]
    SVC-->>JVM: flow completes
    JVM->>ROOM: saveReflection(entryId, finalText)
```

---

## Mood Flow

```mermaid
sequenceDiagram
    participant U as User
    participant MS as MoodSlider (HomeScreen)
    participant HVM as HomeViewModel
    participant MR as MoodRepository
    participant ROOM as Room DB

    U->>MS: drag or tap mood position
    MS->>MS: haptic feedback on snap
    MS->>HVM: onMoodSelect(mood) [on release]
    HVM->>MR: recordMood(mood)
    MR->>ROOM: insertOrReplaceMood(entity)
    ROOM-->>MR: Flow emits updated mood
    MR-->>HVM: todaysMood Flow update
    HVM-->>MS: uiState.todaysMood updated
    MS->>MS: recompose — thumb jumps to saved position
```

---

## App Startup / Theme Flow

```mermaid
sequenceDiagram
    participant OS as Android OS
    participant MA as MainActivity
    participant DS as DataStore
    participant HVM as HomeViewModel

    OS->>MA: onCreate()
    MA->>DS: collect themePreference
    DS-->>MA: ThemePreference (SYSTEM/LIGHT/DARK)
    MA->>MA: apply BloomTheme(darkTheme=...)
    MA->>DS: collect isOnboardingComplete
    DS-->>MA: true/false → startDestination
    MA->>MA: render BloomNavGraph
    Note over MA,HVM: HomeViewModel initializes independently
    MA->>HVM: (created by Compose on first navigation)
    HVM->>DS: collect userName + themePreference
    HVM->>MA: StateFlow drives theme toggle button
```

---

## Quote Rotation

```mermaid
flowchart LR
    TICK["Every 60 seconds\ncurrentQuoteFlow emits"] --> CALC["currentHour / 2 → quoteIndex\nmod 25"]
    CALC --> Q["Constants.DAILY_QUOTES[quoteIndex]"]
    Q --> COMBINE["combine(..., currentQuoteFlow) in HomeViewModel"]
    COMBINE --> STATE["HomeUiState.quote updates"]
    STATE --> QC["QuoteCard recomposes\n(animated crossfade)"]
```

---

## Event Bus

```mermaid
flowchart LR
    JVM[JournalViewModel\nsaveEntry] -->|emit JournalEntrySaved| BUS[AppEventBus\nMutableSharedFlow]
    BUS -->|collect in LaunchedEffect| HS[HomeScreen]
    HS -->|gardenBounce = true| GA[Garden Animation]
    HS -->|showSnackbar| SB["🌱 Reflection Saved"]
```
