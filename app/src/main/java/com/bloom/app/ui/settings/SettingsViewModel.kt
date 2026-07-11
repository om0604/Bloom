package com.bloom.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bloom.app.util.ThemePreference
import com.bloom.app.util.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = userPreferences.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.SYSTEM
        )

    val userName: StateFlow<String> = userPreferences.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val dailyReminderEnabled: StateFlow<Boolean> = userPreferences.dailyReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferences.setThemePreference(theme)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            userPreferences.setUserName(name)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDailyReminderEnabled(enabled)
        }
    }

    companion object {
        fun provideFactory(userPreferences: UserPreferences): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(userPreferences) as T
                }
            }
    }
}
