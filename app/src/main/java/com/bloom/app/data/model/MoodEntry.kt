package com.bloom.app.data.model

// ─────────────────────────────────────────────────────────────────────────────
// MoodEntry — Domain Model
//
// Represents a standalone mood check-in (separate from journal entries).
// Users may log mood without writing — low-friction check-in is a feature.
// ─────────────────────────────────────────────────────────────────────────────

data class MoodEntry(
    val id          : Long   = 0,
    val mood        : Mood,
    val note        : String? = null,   // Optional one-liner (max ~60 chars)
    val recordedAt  : Long,             // Epoch millis
)
