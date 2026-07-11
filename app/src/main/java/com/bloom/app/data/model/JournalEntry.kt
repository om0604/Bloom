package com.bloom.app.data.model

// ─────────────────────────────────────────────────────────────────────────────
// JournalEntry — Domain Model
//
// This is the app's primary domain object.
// It is NOT the Room entity — that lives in data/local/entity/.
// Keeping them separate means the UI and ViewModels never depend on Room.
// ─────────────────────────────────────────────────────────────────────────────

data class JournalEntry(
    val id           : Long    = 0,
    val content      : String,          // The journal text — primary field
    val mood         : Mood,            // Mood at time of writing
    val prompt       : String?  = null, // Which prompt the user chose (optional)
    val aiReflection : String?  = null, // Gemini reflection, if requested
    val wordCount    : Int      = 0,    // Computed from content
    val createdAt    : Long,            // Epoch millis — used for date grouping
    val updatedAt    : Long,            // Epoch millis — tracks edits
)
