package com.bloom.app.data.local.dao

import androidx.room.*
import com.bloom.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// JournalEntryDao
//
// All reactive queries return Flow — the ViewModel stays up-to-date
// automatically without manual refresh calls.
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface JournalEntryDao {

    /**
     * Observe all entries ordered by creation date, newest first.
     * Used by: JournalListScreen, HomeScreen (recent entry).
     */
    @Query("SELECT * FROM journal_entries ORDER BY created_at DESC")
    fun observeAllEntries(): Flow<List<JournalEntryEntity>>

    /**
     * Observe a single entry by id — used by the journal editor to
     * reactively update when AI reflection is saved.
     */
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    fun observeEntryById(id: Long): Flow<JournalEntryEntity?>

    /**
     * Total count of entries — drives GardenStage progression.
     */
    @Query("SELECT COUNT(*) FROM journal_entries")
    fun observeEntryCount(): Flow<Int>

    /**
     * Count of distinct calendar days with at least one entry.
     * Used for streak calculation in Insights.
     */
    @Query("""
        SELECT COUNT(DISTINCT date(created_at / 1000, 'unixepoch')) 
        FROM journal_entries
    """)
    suspend fun countDistinctDays(): Int

    /**
     * Entries in the past N days — used for streak computation.
     * [sinceMillis] is the epoch millis of midnight N days ago.
     */
    @Query("SELECT * FROM journal_entries WHERE created_at >= :sinceMillis ORDER BY created_at DESC")
    suspend fun getEntriesSince(sinceMillis: Long): List<JournalEntryEntity>

    /**
     * Most recent entry — displayed on HomeScreen.
     */
    @Query("SELECT * FROM journal_entries ORDER BY created_at DESC LIMIT 1")
    fun observeLatestEntry(): Flow<JournalEntryEntity?>

    /** Save a new entry, returns the generated id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    /** Update an existing entry (e.g., when AI reflection is added). */
    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    /** Delete — soft deletes not needed for this app's scope. */
    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)

    /**
     * Update only the AI reflection field.
     * Used by GeminiService — avoids overwriting the full entry.
     */
    @Query("UPDATE journal_entries SET ai_reflection = :reflection, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateReflection(id: Long, reflection: String, updatedAt: Long)
}
