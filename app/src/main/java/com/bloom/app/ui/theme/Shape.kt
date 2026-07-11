package com.bloom.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Bloom Shape System
//
// Shapes in Bloom are soft and rounded — never sharp or angular.
// Sharp corners create tension; rounded corners communicate safety and calm.
// ─────────────────────────────────────────────────────────────────────────────

val BloomShapes = Shapes(
    // ExtraSmall — small chips, badges
    extraSmall  = RoundedCornerShape(8.dp),

    // Small — text fields, compact elements
    small       = RoundedCornerShape(12.dp),

    // Medium — standard cards, mood cards
    medium      = RoundedCornerShape(20.dp),

    // Large — bottom sheets, feature cards
    large       = RoundedCornerShape(28.dp),

    // ExtraLarge — full-width panels, dialogs
    extraLarge  = RoundedCornerShape(36.dp),
)

// ── Custom Shape Tokens ───────────────────────────────────────────────────────
// Used directly in composables for specific needs

/** Pill shape — CTAs, primary buttons */
val PillShape = RoundedCornerShape(50)

/** Mood card — slightly more rounded than standard cards */
val MoodCardShape = RoundedCornerShape(24.dp)

/** Garden card — soft, organic feel */
val GardenCardShape = RoundedCornerShape(32.dp)

/** Bottom bar shape — rounded top corners only */
val BottomBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
