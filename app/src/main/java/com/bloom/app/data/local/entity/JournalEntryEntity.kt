package com.bloom.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────────────────────
// JournalEntryEntity — Room Database Entity
//
// Deliberately separated from the domain model.
// Room concerns (annotations, naming) never leak into the domain.
// ─────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id           : Long   = 0,

    @ColumnInfo(name = "content")
    val content      : String,

    /** Stored as enum name string — safe and future-proof */
    @ColumnInfo(name = "mood")
    val mood         : String,

    /** The journal prompt shown to the user, if any */
    @ColumnInfo(name = "prompt")
    val prompt       : String? = null,

    /** Gemini AI reflection — null until user requests it */
    @ColumnInfo(name = "ai_reflection")
    val aiReflection : String? = null,

    /** Computed at save time — avoids recomputing in queries */
    @ColumnInfo(name = "word_count")
    val wordCount    : Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt    : Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt    : Long,
)
