package com.bloom.app.di

import android.content.Context
import com.bloom.app.ai.GroqService
import com.bloom.app.data.local.BloomDatabase
import com.bloom.app.data.repository.JournalRepository
import com.bloom.app.data.repository.MoodRepository
import com.bloom.app.util.UserPreferences

// ─────────────────────────────────────────────────────────────────────────────
// AppContainer — Manual Dependency Injection
//
// A simple service locator held by BloomApplication.
// All objects are created lazily — nothing is instantiated until first access.
//
// Why manual DI over Hilt?
//   This app has 5 screens and 2 repositories. Hilt would add:
//   - @HiltAndroidApp annotation processing
//   - @AndroidEntryPoint on every Activity/Fragment
//   - @HiltViewModel on every ViewModel
//   - Multiple generated modules
//   - Increased build time
//
//   For this scope, an AppContainer is transparent, readable, and
//   easier to reason about in code review. Migration to Hilt later
//   would take ~30 minutes if the app grows.
// ─────────────────────────────────────────────────────────────────────────────

class AppContainer(context: Context) {

    // ── Preferences ───────────────────────────────────────────────────────────
    val userPreferences by lazy { UserPreferences(context) }

    // ── Database + DAOs ───────────────────────────────────────────────────────
    private val database by lazy { BloomDatabase.getInstance(context) }

    // ── Repositories ──────────────────────────────────────────────────────────
    val journalRepository by lazy { JournalRepository(database.journalEntryDao()) }
    val moodRepository    by lazy { MoodRepository(database.moodEntryDao()) }

    // ── AI Service ────────────────────────────────────────────────────────────
    val groqService by lazy { GroqService() }
}
