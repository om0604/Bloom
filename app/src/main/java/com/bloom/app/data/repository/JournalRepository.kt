package com.bloom.app.data.repository

import com.bloom.app.data.local.dao.JournalEntryDao
import com.bloom.app.data.local.entity.toDomain
import com.bloom.app.data.local.entity.toEntity
import com.bloom.app.data.model.JournalEntry
import com.bloom.app.data.model.Mood
import com.bloom.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────────────────────
// JournalRepository
//
// Single source of truth for journal data.
// ViewModels interact only with this — never directly with the DAO.
//
// All Flow-returning functions emit immediately on collection and then
// whenever the underlying Room table changes. No manual refresh needed.
// ─────────────────────────────────────────────────────────────────────────────

class JournalRepository(private val dao: JournalEntryDao) {

    /** All entries as domain models, newest first. */
    val allEntries: Flow<List<JournalEntry>> =
        dao.observeAllEntries().map { entities -> entities.map { it.toDomain() } }

    /** The most recent entry — shown on HomeScreen. */
    val latestEntry: Flow<JournalEntry?> =
        dao.observeLatestEntry().map { it?.toDomain() }

    /** Total entry count — drives GardenStage. */
    val entryCount: Flow<Int> = dao.observeEntryCount()

    /** Observe a single entry by id (for editor screen). */
    fun observeEntry(id: Long): Flow<JournalEntry?> =
        dao.observeEntryById(id).map { it?.toDomain() }

    /**
     * Save a new journal entry.
     * Word count is computed here so it never leaks into the UI layer.
     * Returns the generated id for navigation purposes.
     */
    suspend fun saveEntry(
        content : String,
        mood    : Mood,
        prompt  : String? = null,
    ): Long {
        val now   = System.currentTimeMillis()
        val entry = JournalEntry(
            content   = content.trim(),
            mood      = mood,
            prompt    = prompt,
            wordCount = content.trim().split("\\s+".toRegex()).count { it.isNotEmpty() },
            createdAt = now,
            updatedAt = now,
        )
        return dao.insertEntry(entry.toEntity())
    }

    /**
     * Update an existing entry's content and mood.
     */
    suspend fun updateEntry(entry: JournalEntry) {
        dao.updateEntry(
            entry.copy(
                wordCount = entry.content.trim().split("\\s+".toRegex()).count { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis(),
            ).toEntity()
        )
    }

    /**
     * Attach a Gemini reflection to an existing entry.
     * Uses a targeted UPDATE — never touches content or mood.
     */
    suspend fun saveReflection(entryId: Long, reflection: String) {
        dao.updateReflection(
            id          = entryId,
            reflection  = reflection,
            updatedAt   = System.currentTimeMillis(),
        )
    }

    suspend fun deleteEntry(entry: JournalEntry) {
        dao.deleteEntry(entry.toEntity())
    }

    /**
     * Computes the current journaling streak in days.
     *
     * Algorithm: Walk backwards day by day from today.
     * If there's an entry for that day, increment streak.
     * Stop on the first day with no entry.
     *
     * This is an O(streak) operation — acceptable for streaks
     * that will realistically be under 365 days.
     */
    suspend fun computeStreak(): Int {
        var streak      = 0
        var checkDay    = DateUtils.startOfToday()
        val oneDayMs    = 24 * 60 * 60 * 1000L

        while (true) {
            val entriesOnDay = dao.getEntriesSince(checkDay)
                .filter { it.createdAt < checkDay + oneDayMs }

            if (entriesOnDay.isEmpty()) break
            streak++
            checkDay -= oneDayMs
        }

        return streak
    }
}
