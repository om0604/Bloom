package com.bloom.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// DateUtils
//
// All date/time logic lives here.
// Uses java.time (API 26+) — no Calendar, no SimpleDateFormat.
// ─────────────────────────────────────────────────────────────────────────────

object DateUtils {

    private val defaultZone get() = ZoneId.systemDefault()

    /** Epoch millis for midnight (start) of today in local time. */
    fun startOfToday(): Long =
        LocalDate.now()
            .atStartOfDay(defaultZone)
            .toInstant()
            .toEpochMilli()

    /** Epoch millis for midnight of a specific date. */
    fun startOfDay(date: LocalDate): Long =
        date.atStartOfDay(defaultZone).toInstant().toEpochMilli()

    /** Convert epoch millis to LocalDate in device's local timezone. */
    fun millisToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(defaultZone).toLocalDate()

    /**
     * Friendly date display for journal entries:
     * - Today     → "Today"
     * - Yesterday → "Yesterday"
     * - This year → "Monday, July 7"
     * - Older     → "July 7, 2024"
     */
    fun formatEntryDate(millis: Long): String {
        val date  = millisToLocalDate(millis)
        val today = LocalDate.now()
        return when {
            date == today               -> "Today"
            date == today.minusDays(1)  -> "Yesterday"
            date.year == today.year     -> date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
            else                        -> date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()))
        }
    }

    /** Short date for compact UI: "Jul 7" */
    fun formatShortDate(millis: Long): String =
        millisToLocalDate(millis).format(
            DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
        )

    /** Time string: "9:30 AM" */
    fun formatTime(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(defaultZone)
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

    /**
     * Greeting based on local time:
     * 05:00–11:59 → "Good morning"
     * 12:00–16:59 → "Good afternoon"
     * 17:00–20:59 → "Good evening"
     * 21:00–04:59 → "Good night"
     */
    fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11  -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤"
            in 17..20 -> "Good Evening 🌙"
            else      -> "Good Evening 🌙"
        }
    }
}
