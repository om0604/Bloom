package com.bloom.app

import android.app.Application
import com.bloom.app.di.AppContainer

// ─────────────────────────────────────────────────────────────────────────────
// BloomApplication
//
// Entry point for the app process.
// Holds the AppContainer — single owner of all shared dependencies.
// ─────────────────────────────────────────────────────────────────────────────

class BloomApplication : Application() {

    /** Accessed by ViewModels via (context.applicationContext as BloomApplication).container */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
