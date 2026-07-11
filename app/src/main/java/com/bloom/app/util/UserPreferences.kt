package com.bloom.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────────────────────
// UserPreferences
//
// Wraps DataStore for lightweight user preferences:
//   - Whether onboarding has been completed (routing gate)
//   - User's name (shown in greeting)
//
// Not using SharedPreferences — DataStore is the modern, coroutine-safe
// replacement. Suspending writes prevent ANRs on main thread.
// ─────────────────────────────────────────────────────────────────────────────

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

class UserPreferences(private val context: Context) {

    private val onboardingKey = booleanPreferencesKey(Constants.KEY_ONBOARDING_COMPLETE)
    private val userNameKey   = stringPreferencesKey(Constants.KEY_USER_NAME)
    private val themePrefKey  = stringPreferencesKey(Constants.KEY_THEME_PREFERENCE)
    private val reminderKey   = booleanPreferencesKey(Constants.KEY_DAILY_REMINDER_ENABLED)

    val isOnboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[onboardingKey] ?: false
        }

    val userName: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[userNameKey] ?: ""
        }

    val themePreference: Flow<ThemePreference> =
        context.dataStore.data.map { prefs ->
            when (prefs[themePrefKey]) {
                ThemePreference.LIGHT.name -> ThemePreference.LIGHT
                ThemePreference.DARK.name  -> ThemePreference.DARK
                else                       -> ThemePreference.SYSTEM
            }
        }

    val dailyReminderEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[reminderKey] ?: false
        }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[onboardingKey] = true
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[userNameKey] = name.trim()
        }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[themePrefKey] = theme.name
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[reminderKey] = enabled
        }
    }
}
