package com.bloom.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bloom.app.BloomApplication
import com.bloom.app.data.model.GardenStage
import com.bloom.app.data.model.JournalEntry
import com.bloom.app.data.model.Mood
import com.bloom.app.data.model.MoodEntry
import com.bloom.app.util.Constants
import com.bloom.app.util.DateUtils
import com.bloom.app.util.ThemePreference
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate

data class HomeUiState(
    val greeting        : String           = "",
    val userName        : String           = "",
    val quote           : Pair<String, String> = Pair("", ""),
    val streakDays      : Int              = 0,
    val gardenStage     : GardenStage      = GardenStage.SEED,
    val todaysMood      : MoodEntry?       = null,
    val latestEntry     : JournalEntry?    = null,
    val hasEntryToday   : Boolean          = false,
    val isLoading       : Boolean          = true,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container   = (application as BloomApplication).container
    private val journalRepo = container.journalRepository
    private val moodRepo    = container.moodRepository
    private val prefs       = container.userPreferences

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val themePreference: StateFlow<ThemePreference> = prefs.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.SYSTEM
        )


    fun toggleTheme() {
        viewModelScope.launch {
            val current = themePreference.value
            val newTheme = if (current == ThemePreference.DARK) ThemePreference.LIGHT else ThemePreference.DARK
            prefs.setThemePreference(newTheme)
        }
    }

    fun setTodaysMood(mood: Mood) {
        viewModelScope.launch {
            moodRepo.recordMood(mood = mood)
        }
    }

    private val currentQuoteFlow: Flow<Pair<String, String>> = flow {
        while(true) {
            val currentHour = System.currentTimeMillis() / (1000 * 60 * 60)
            val quoteIndex = (currentHour / 2).toInt() % Constants.DAILY_QUOTES.size
            emit(Constants.DAILY_QUOTES[quoteIndex])
            delay(60_000L) // Check every minute
        }
    }

    init {
        observeHomeData()
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            // Combine all reactive sources into a single UI state
            combine(
                prefs.userName,
                moodRepo.todaysMood,
                journalRepo.latestEntry,
                journalRepo.entryCount,
                currentQuoteFlow,
            ) { name, todaysMood, latestEntry, entryCount, quote ->

                val streak      = journalRepo.computeStreak()
                val gardenStage = GardenStage.fromEntryCount(entryCount)

                // Determine if the latest entry was created today
                val hasEntryToday = latestEntry?.let {
                    DateUtils.millisToLocalDate(it.createdAt) == LocalDate.now()
                } ?: false

                HomeUiState(
                    greeting      = DateUtils.getGreeting(),
                    userName      = name.ifEmpty { "there" },
                    quote         = quote,
                    streakDays    = streak,
                    gardenStage   = gardenStage,
                    todaysMood    = todaysMood,
                    latestEntry   = latestEntry,
                    hasEntryToday = hasEntryToday,
                    isLoading     = false,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
