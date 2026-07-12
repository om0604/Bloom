package com.bloom.app.util

// ─────────────────────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────────────────────

object Constants {

    // ── DataStore Keys ────────────────────────────────────────────────────────
    const val DATASTORE_NAME            = "bloom_preferences"
    const val KEY_ONBOARDING_COMPLETE   = "onboarding_complete"
    const val KEY_USER_NAME             = "user_name"
    const val KEY_THEME_PREFERENCE      = "theme_preference"
    const val KEY_DAILY_REMINDER_ENABLED= "daily_reminder_enabled"

    // ── Journal Prompts ───────────────────────────────────────────────────────
    // Shown as optional gentle prompts in the journal editor.
    // Not a form — user can ignore these entirely.
    val JOURNAL_PROMPTS = listOf(
        "How was today?",
        "What made you smile today?",
        "What are you grateful for right now?",
        "What challenged you today?",
        "What's something you want to remember?",
        "How are you really feeling?",
        "What did you learn today?",
        "What would make tomorrow better?",
        "What brought you peace today?",
        "What are you looking forward to?",
    )

    // ── Daily Quotes ──────────────────────────────────────────────────────────
    // Warm, reflective quotes — not motivational posters.
    // Rotated daily by index (dayOfYear % quotes.size).
    val DAILY_QUOTES = listOf(
        Pair("The quieter you become, the more you are able to hear.", "Rumi"),
        Pair("Almost everything will work again if you unplug it for a few minutes, including you.", "Anne Lamott"),
        Pair("You don't have to see the whole staircase, just take the first step.", "Martin Luther King Jr."),
        Pair("In the middle of difficulty lies opportunity.", "Albert Einstein"),
        Pair("This too shall pass.", "Persian proverb"),
        Pair("You are enough, just as you are.", "Megan Markle"),
        Pair("Be gentle with yourself. You are a child of the universe.", "Max Ehrmann"),
        Pair("Not all those who wander are lost.", "J.R.R. Tolkien"),
        Pair("The present moment always will have been.", "Unknown"),
        Pair("Do what you can, with what you have, where you are.", "Theodore Roosevelt"),
        Pair("Breathe. You're going to be okay.", "Unknown"),
        Pair("Wherever you are, be all there.", "Jim Elliot"),
        Pair("What you seek is seeking you.", "Rumi"),
        Pair("Rest is not idleness.", "John Lubbock"),
        Pair("Small steps still move you forward.", "Unknown"),
        Pair("Peace comes from within. Do not seek it without.", "Buddha"),
        Pair("Your calm mind is the ultimate weapon against your challenges.", "Bryant McGill"),
        Pair("Tension is who you think you should be. Relaxation is who you are.", "Chinese Proverb"),
        Pair("Happiness is not something ready made. It comes from your own actions.", "Dalai Lama"),
        Pair("Every moment is a fresh beginning.", "T.S. Eliot"),
        Pair("Turn your face to the sun and shadows fall behind you.", "Maori Proverb"),
        Pair("The best way out is always through.", "Robert Frost"),
        Pair("Doubt whom you will, but never yourself.", "Christian Nestell Bovee"),
        Pair("Fall seven times, stand up eight.", "Japanese Proverb"),
        Pair("Act as if what you do makes a difference. It does.", "William James")
    )
}
