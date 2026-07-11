package com.bloom.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloom.app.BloomApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentPage : Int    = 0,
    val userName    : String = "",
    val isComplete  : Boolean = false,
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as BloomApplication).container
    private val prefs     = container.userPreferences

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    fun onNextPage() {
        val current = _uiState.value.currentPage
        if (current < OnboardingPage.entries.size - 1) {
            _uiState.value = _uiState.value.copy(currentPage = current + 1)
        }
    }

    fun onPreviousPage() {
        val current = _uiState.value.currentPage
        if (current > 0) {
            _uiState.value = _uiState.value.copy(currentPage = current - 1)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val name = _uiState.value.userName.trim()
            if (name.isNotEmpty()) {
                prefs.setUserName(name)
            }
            prefs.setOnboardingComplete()
            _uiState.value = _uiState.value.copy(isComplete = true)
        }
    }
}

// ── Onboarding Page Definitions ───────────────────────────────────────────────

enum class OnboardingPage(
    val emoji       : String,
    val headline    : String,
    val body        : String,
) {
    WELCOME(
        emoji    = "🌱",
        headline = "A place to\nbe honest.",
        body     = "Bloom is your quiet space to reflect, feel, and grow — a little every day.",
    ),
    GARDEN(
        emoji    = "🌸",
        headline = "Grow your\ngarden.",
        body     = "Every time you reflect, your garden grows. Watch something beautiful take shape.",
    ),
    NAME(
        emoji    = "✨",
        headline = "What should\nwe call you?",
        body     = "We'll use your name in your daily greeting. You can always change it later.",
    );
}
