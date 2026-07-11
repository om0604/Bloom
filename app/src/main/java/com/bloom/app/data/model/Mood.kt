package com.bloom.app.data.model

// ─────────────────────────────────────────────────────────────────────────────
// Mood
//
// Five moods — not ten, not a slider.
// Research shows too many options cause decision paralysis.
// Five named emotions are specific enough to be meaningful,
// broad enough to always feel accurate.
//
// Each mood carries:
//   - displayName: Human-readable label shown in UI
//   - emoji: The visual anchor — quick, universal, emotional
//   - description: A gentle one-liner shown below the mood card
// ─────────────────────────────────────────────────────────────────────────────

enum class Mood(
    val displayName: String,
    val emoji: String,
    val description: String,
) {
    GREAT(
        displayName  = "Great",
        emoji        = "✨",
        description  = "Feeling bright and energized",
    ),
    GOOD(
        displayName  = "Good",
        emoji        = "🌿",
        description  = "Calm and content",
    ),
    OKAY(
        displayName  = "Okay",
        emoji        = "☁️",
        description  = "Getting through the day",
    ),
    LOW(
        displayName  = "Low",
        emoji        = "🌙",
        description  = "A quieter kind of day",
    ),
    ROUGH(
        displayName  = "Rough",
        emoji        = "🌧️",
        description  = "It's okay — some days are harder",
    );

    companion object {
        /** Safe deserialization — never crashes on unknown stored values */
        fun fromName(name: String): Mood =
            entries.firstOrNull { it.name == name } ?: OKAY
    }
}
