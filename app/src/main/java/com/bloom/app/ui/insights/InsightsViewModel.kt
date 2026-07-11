package com.bloom.app.ui.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloom.app.BloomApplication
import com.bloom.app.data.model.GardenStage
import com.bloom.app.data.model.Mood
import kotlinx.coroutines.flow.*

data class InsightsUiState(
    val streakDays          : Int           = 0,
    val totalEntries        : Int           = 0,
    val totalWords          : Int           = 0,
    val mostCommonMood      : Mood?         = null,
    val gardenStage         : GardenStage   = GardenStage.SEED,
    val moodBreakdown       : Map<Mood, Int> = emptyMap(),
    val reflectionCount     : Int           = 0,
    val isLoading           : Boolean       = true,
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val container   = (application as BloomApplication).container
    private val journalRepo = container.journalRepository
    private val moodRepo    = container.moodRepository

    val uiState: StateFlow<InsightsUiState> =
        combine(
            journalRepo.allEntries,
            journalRepo.entryCount,
            moodRepo.last30DaysMoods,
        ) { entries, count, moods ->

            val streak      = journalRepo.computeStreak()
            val totalWords  = entries.sumOf { it.wordCount }
            val gardenStage = GardenStage.fromEntryCount(count)
            val mostCommon  = moodRepo.mostCommonMood(moods)
            val reflections = entries.count { it.aiReflection != null }

            val moodBreakdown = moods
                .groupBy { it.mood }
                .mapValues { (_, entries) -> entries.size }

            InsightsUiState(
                streakDays      = streak,
                totalEntries    = count,
                totalWords      = totalWords,
                mostCommonMood  = mostCommon,
                gardenStage     = gardenStage,
                moodBreakdown   = moodBreakdown,
                reflectionCount = reflections,
                isLoading       = false,
            )
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = InsightsUiState(),
        )
}
