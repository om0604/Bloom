package com.bloom.app.data.local.entity

import com.bloom.app.data.model.JournalEntry
import com.bloom.app.data.model.Mood
import com.bloom.app.data.model.MoodEntry

// ─────────────────────────────────────────────────────────────────────────────
// Entity Mappers
//
// Pure functions — no state, no dependencies.
// The only place where Room entities become domain models and vice versa.
// This keeps the domain model free of Room annotations forever.
// ─────────────────────────────────────────────────────────────────────────────

// ── JournalEntry ─────────────────────────────────────────────────────────────

fun JournalEntryEntity.toDomain(): JournalEntry = JournalEntry(
    id           = id,
    content      = content,
    mood         = Mood.fromName(mood),
    prompt       = prompt,
    aiReflection = aiReflection,
    wordCount    = wordCount,
    createdAt    = createdAt,
    updatedAt    = updatedAt,
)

fun JournalEntry.toEntity(): JournalEntryEntity = JournalEntryEntity(
    id           = id,
    content      = content,
    mood         = mood.name,
    prompt       = prompt,
    aiReflection = aiReflection,
    wordCount    = wordCount,
    createdAt    = createdAt,
    updatedAt    = updatedAt,
)

// ── MoodEntry ─────────────────────────────────────────────────────────────────

fun MoodEntryEntity.toDomain(): MoodEntry = MoodEntry(
    id         = id,
    mood       = Mood.fromName(mood),
    note       = note,
    recordedAt = recordedAt,
)

fun MoodEntry.toEntity(): MoodEntryEntity = MoodEntryEntity(
    id         = id,
    mood       = mood.name,
    note       = note,
    recordedAt = recordedAt,
)
