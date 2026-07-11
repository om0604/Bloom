package com.bloom.app.data.local.dao

import androidx.room.*
import com.bloom.app.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {

    /**
     * All mood entries, newest first — for Insights history view.
     */
    @Query("SELECT * FROM mood_entries ORDER BY recorded_at DESC")
    fun observeAllMoodEntries(): Flow<List<MoodEntryEntity>>

    /**
     * Today's mood check-in — used on HomeScreen to show if user has
     * already checked in today.
     * [startOfDayMillis] is epoch millis for midnight local time today.
     */
    @Query("SELECT * FROM mood_entries WHERE recorded_at >= :startOfDayMillis ORDER BY recorded_at DESC LIMIT 1")
    fun observeTodaysMood(startOfDayMillis: Long): Flow<MoodEntryEntity?>

    /**
     * Mood entries for the past 30 days — used in Insights.
     */
    @Query("SELECT * FROM mood_entries WHERE recorded_at >= :sinceMillis ORDER BY recorded_at DESC")
    fun observeMoodsSince(sinceMillis: Long): Flow<List<MoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntryEntity): Long

    @Delete
    suspend fun deleteMoodEntry(entry: MoodEntryEntity)
}
