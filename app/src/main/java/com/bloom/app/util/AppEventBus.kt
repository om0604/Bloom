package com.bloom.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global UI events that need to cross ViewModel boundaries without coupling them.
 */
sealed class AppEvent {
    /**
     * Fired when a journal entry is successfully created or meaningfully edited.
     * Use this to trigger celebrations or animations on the Home Screen.
     */
    object JournalEntrySaved : AppEvent()
}

/**
 * A lightweight, decoupled event bus for cross-ViewModel communication.
 */
object AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    fun emit(event: AppEvent) {
        _events.tryEmit(event)
    }
}
