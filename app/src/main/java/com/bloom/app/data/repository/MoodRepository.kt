package com.bloom.app.data.repository

import com.bloom.app.data.local.dao.MoodEntryDao
import com.bloom.app.data.local.entity.toDomain
import com.bloom.app.data.local.entity.toEntity
import com.bloom.app.data.model.Mood
import com.bloom.app.data.model.MoodEntry
import com.bloom.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoodRepository(private val dao: MoodEntryDao) {

    /** Today's mood check-in, if any. */
    val todaysMood: Flow<MoodEntry?> =
        dao.observeTodaysMood(DateUtils.startOfToday()).map { it?.toDomain() }

    /** All mood entries for the Insights history. */
    val allMoodEntries: Flow<List<MoodEntry>> =
        dao.observeAllMoodEntries().map { list -> list.map { it.toDomain() } }

    /** Last 30 days of moods — for the Insights mood breakdown. */
    val last30DaysMoods: Flow<List<MoodEntry>> =
        dao.observeMoodsSince(DateUtils.startOfToday() - 30L * 24 * 60 * 60 * 1000).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun recordMood(mood: Mood, note: String? = null): Long {
        val entry = MoodEntry(
            mood       = mood,
            note       = note?.trim()?.takeIf { it.isNotEmpty() },
            recordedAt = System.currentTimeMillis(),
        )
        return dao.insertMoodEntry(entry.toEntity())
    }

    suspend fun deleteMoodEntry(entry: MoodEntry) {
        dao.deleteMoodEntry(entry.toEntity())
    }

    /**
     * Returns the most common mood from a list of entries.
     * Used in Insights: "Hope appeared often in your reflections."
     */
    fun mostCommonMood(entries: List<MoodEntry>): Mood? =
        entries.groupingBy { it.mood }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
}
