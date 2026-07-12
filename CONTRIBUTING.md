# Contributing to Bloom

Thank you for your interest in contributing to Bloom.

---

## Architecture Principles

Before making changes, read `docs/architecture.md`. Key rules:

1. **ViewModels call repositories. Repositories do not call ViewModels.**
2. **Repositories do not use `AppEventBus`.** Only ViewModels emit events.
3. **Never expose `MutableStateFlow` from a ViewModel.** Always expose `.asStateFlow()`.
4. **No business logic in Composable functions.** Call ViewModel functions instead.
5. **Domain models (`data/model/`) are separate from Room entities (`data/local/entity/`).** Use `EntityMappers` to convert.

---

## Project Setup

See `docs/setup.md` for full instructions.

**Quick version:**
```bash
git clone https://github.com/om0604/Bloom.git
# Add GROQ_API_KEY to local.properties
./gradlew assembleDebug
```

---

## Code Style

- **Kotlin idiomatic**: Use `when`, `data class`, `sealed class`, extension functions
- **Compose**: Prefer `@Composable` functions under 100 lines. Extract to helper composables if longer
- **Comments**: Explain *why*, not *what*. Use the `──` separator style for major section dividers
- **Naming**: `UiState` suffix for state data classes, `Screen` suffix for composable screens, `ViewModel` suffix for ViewModels, `Repository` suffix for repositories

---

## Adding a New Screen

1. Add a `data object` to `Screen.kt`
2. Add a `composable(Screen.YourScreen.route)` block in `BloomNavGraph.kt`
3. Decide if it needs a bottom nav tab → update `BottomNavigation.kt` and `hideBottomNavRoutes` in `MainActivity`
4. Create the `ViewModel` (if needed) — access `AppContainer` via `(application as BloomApplication).container`
5. Create the `Screen.kt` Composable
6. Update `docs/codebase.md` and `docs/architecture.md`

---

## Adding a New Dependency

1. Add the version to `gradle/libs.versions.toml` `[versions]` section
2. Add the library to `[libraries]` section
3. Reference it in `app/build.gradle.kts`
4. Update `docs/setup.md` Tech Stack table if it's a significant addition

---

## Adding a New Room Entity

1. Create the entity in `data/local/entity/`
2. Add to `@Database(entities = [...])` in `BloomDatabase.kt`
3. **Increment the version number**
4. Create a `Migration` object — **never use `fallbackToDestructiveMigration`**
5. Create the DAO in `data/local/dao/`
6. Add mapper functions to `EntityMappers.kt`
7. Create the domain model in `data/model/`
8. Create the repository in `data/repository/`
9. Add the repository to `AppContainer`

---

## Testing

The project has test scaffolding but unit tests are not yet written for MVP. If adding tests:

- Unit tests: `app/src/test/` — use JUnit 4
- UI tests: `app/src/androidTest/` — use Compose testing APIs
- Run: `./gradlew test` (unit) or `./gradlew connectedAndroidTest` (instrumented)

---

## Pull Request Checklist

- [ ] App builds with `./gradlew assembleDebug`
- [ ] No new lint warnings introduced
- [ ] Architecture rules followed (see above)
- [ ] `docs/` updated if architecture or components changed
- [ ] No secrets committed (check `local.properties` is in `.gitignore`)
