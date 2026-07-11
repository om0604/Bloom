package com.bloom.app.ui.mood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloom.app.BloomApplication
import com.bloom.app.data.model.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoodCheckInUiState(
    val selectedMood : Mood?   = null,
    val note         : String  = "",
    val isSaved      : Boolean = false,
)

class MoodViewModel(application: Application) : AndroidViewModel(application) {

    private val moodRepo = (application as BloomApplication).container.moodRepository

    private val _uiState = MutableStateFlow(MoodCheckInUiState())
    val uiState: StateFlow<MoodCheckInUiState> = _uiState.asStateFlow()

    fun selectMood(mood: Mood) {
        _uiState.value = _uiState.value.copy(selectedMood = mood)
    }

    fun onNoteChanged(note: String) {
        if (note.length <= 80) {    // Cap at 80 chars — keep it brief
            _uiState.value = _uiState.value.copy(note = note)
        }
    }

    fun saveMoodEntry() {
        val mood = _uiState.value.selectedMood ?: return
        viewModelScope.launch {
            moodRepo.recordMood(
                mood = mood,
                note = _uiState.value.note.takeIf { it.isNotBlank() },
            )
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
