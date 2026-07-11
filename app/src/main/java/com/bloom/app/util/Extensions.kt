package com.bloom.app.util

import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Extensions — Kotlin extension functions used across the app.
// Kept small — only add extensions here when they are genuinely reusable
// across multiple sites. One-off helpers stay in the call site.
// ─────────────────────────────────────────────────────────────────────────────

/** Capitalize only the first character, preserving the rest of the string. */
fun String.capitalizeFirst(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1)

/**
 * Returns true if this Long (epoch millis) represents a timestamp
 * that falls on today in the device's local timezone.
 */
fun Long.isToday(): Boolean =
    DateUtils.millisToLocalDate(this) == LocalDate.now()

/**
 * Returns true if this Long (epoch millis) represents a timestamp
 * that falls on yesterday in the device's local timezone.
 */
fun Long.isYesterday(): Boolean =
    DateUtils.millisToLocalDate(this) == LocalDate.now().minusDays(1)

/**
 * Clamps an Int to [min, max] range.
 * Not in stdlib until Kotlin 1.7 — explicit for clarity.
 */
fun Int.coerceInRange(min: Int, max: Int): Int = coerceIn(min, max)

/**
 * Safe substring that won't throw on out-of-bounds indices.
 * Used for safe preview truncation.
 */
fun String.safeTake(n: Int): String =
    if (length <= n) this else take(n).trimEnd() + "…"
