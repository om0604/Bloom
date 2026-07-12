package com.bloom.app.ui.navigation

// ─────────────────────────────────────────────────────────────────────────────
// Screen — Type-safe navigation routes
//
// Using a sealed class with object and data class variants:
//   - Objects for screens with no parameters
//   - Data classes for screens requiring arguments
//
// Route strings are defined once here — no magic strings scattered
// across the codebase.
// ─────────────────────────────────────────────────────────────────────────────

sealed class Screen(val route: String) {

    // ── Top-level destination (shown once) ───────────────────────────────────
    data object Splash     : Screen("splash")
    data object Onboarding : Screen("onboarding")

    // ── Main bottom nav destinations ─────────────────────────────────────────
    data object Home     : Screen("home")
    data object Journal  : Screen("journal")
    data object Garden   : Screen("garden")
    data object Insights : Screen("insights")

    // ── Sub-destinations ──────────────────────────────────────────────────────

    data object Settings    : Screen("settings")

    /** Journal editor — entryId is optional (null = new entry) */
    data object JournalEditor : Screen("journal_editor/{entryId}") {
        const val ARG_ENTRY_ID = "entryId"
        const val NEW_ENTRY    = -1L

        /** Build the route for navigation call sites. */
        fun createRoute(entryId: Long = NEW_ENTRY): String =
            "journal_editor/$entryId"
    }
}
