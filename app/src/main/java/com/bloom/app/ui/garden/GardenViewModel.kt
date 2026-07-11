package com.bloom.app.ui.garden

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloom.app.BloomApplication
import com.bloom.app.data.model.GardenStage
import kotlinx.coroutines.flow.*

data class GardenUiState(
    val stage       : GardenStage  = GardenStage.SEED,
    val entryCount  : Int          = 0,
    val nextStage   : GardenStage? = null,
    val entriesUntilNextStage : Int = 0,
    val isLoading   : Boolean      = true,
)

class GardenViewModel(application: Application) : AndroidViewModel(application) {

    private val journalRepo = (application as BloomApplication).container.journalRepository

    val uiState: StateFlow<GardenUiState> =
        journalRepo.entryCount
            .map { count ->
                val currentStage = GardenStage.fromEntryCount(count)
                val nextStage = GardenStage.entries
                    .firstOrNull { it.minEntries > count }

                GardenUiState(
                    stage      = currentStage,
                    entryCount = count,
                    nextStage  = nextStage,
                    entriesUntilNextStage = nextStage?.let { it.minEntries - count } ?: 0,
                    isLoading  = false,
                )
            }
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = GardenUiState(),
            )
}
