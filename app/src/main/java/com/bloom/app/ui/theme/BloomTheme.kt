package com.bloom.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.bloom.app.util.ThemePreference

// ─────────────────────────────────────────────────────────────────────────────
// Bloom Theme
//
// Light mode: Warm cream backgrounds with amber accents — feels like morning.
// Dark mode: Deep warm brown with muted amber — feels like candlelight.
//
// Material You (dynamic color) is supported on API 31+ but only used if the
// user's wallpaper-based palette doesn't clash with Bloom's warmth.
// We intentionally disable dynamic color by default to preserve the Bloom
// brand identity — unlike generic apps, Bloom's color is part of its soul.
// ─────────────────────────────────────────────────────────────────────────────

private val BloomLightColorScheme = lightColorScheme(
    // ── Primary — Amber gold ──────────────────────────────────────────────────
    primary             = Amber60,
    onPrimary           = Color.White,
    primaryContainer    = Amber95,
    onPrimaryContainer  = Amber10,

    // ── Secondary — Sage green ────────────────────────────────────────────────
    secondary           = Sage50,
    onSecondary         = Color.White,
    secondaryContainer  = Sage95,
    onSecondaryContainer= Sage10,

    // ── Tertiary — Dusty rose ─────────────────────────────────────────────────
    tertiary            = Rose60,
    onTertiary          = Color.White,
    tertiaryContainer   = Rose95,
    onTertiaryContainer = Rose10,

    // ── Error ─────────────────────────────────────────────────────────────────
    error               = ErrorRed,
    onError             = Color.White,

    // ── Backgrounds — warm cream, not clinical white ───────────────────────────
    background          = Warm99,
    onBackground        = Warm10,

    // ── Surface — slightly warmer than background ─────────────────────────────
    surface             = Color.White,
    onSurface           = Warm20,
    surfaceVariant      = Warm95,
    onSurfaceVariant    = Warm40,

    // ── Outlines ──────────────────────────────────────────────────────────────
    outline             = Warm80,
    outlineVariant      = Warm90,

    // ── Scrim — warm rather than neutral ──────────────────────────────────────
    scrim               = Warm20,
)

private val BloomDarkColorScheme = darkColorScheme(
    // ── Primary ───────────────────────────────────────────────────────────────
    primary             = Amber80,
    onPrimary           = Amber20,
    primaryContainer    = Amber30,
    onPrimaryContainer  = Amber90,

    // ── Secondary ─────────────────────────────────────────────────────────────
    secondary           = Sage80,
    onSecondary         = Sage20,
    secondaryContainer  = Sage30,
    onSecondaryContainer= Sage90,

    // ── Tertiary ──────────────────────────────────────────────────────────────
    tertiary            = Rose80,
    onTertiary          = Rose20,
    tertiaryContainer   = Rose30,
    onTertiaryContainer = Rose90,

    // ── Error ─────────────────────────────────────────────────────────────────
    error               = Color(0xFFFFB4AB),
    onError             = Color(0xFF690005),

    // ── Backgrounds — deep warm brown, not cold dark grey ─────────────────────
    background          = Warm10,
    onBackground        = Warm90,

    // ── Surface ───────────────────────────────────────────────────────────────
    surface             = Warm20,
    onSurface           = Warm80,
    surfaceVariant      = Warm30,
    onSurfaceVariant    = Warm70,

    // ── Outlines ──────────────────────────────────────────────────────────────
    outline             = Warm50,
    outlineVariant      = Warm40,

    scrim               = Color.Black,
)

@Composable
fun BloomTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    // Dynamic color optionally enabled — Bloom's warmth is brand identity but users can override
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        useDarkTheme -> BloomDarkColorScheme
        else         -> BloomLightColorScheme
    }

    // ── Configure system bars for edge-to-edge ────────────────────────────────
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make both status bar and nav bar transparent for edge-to-edge
            window.statusBarColor  = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            val insetsController = WindowCompat.getInsetsController(window, view)
            // Use dark icons on light backgrounds, light icons on dark
            insetsController.isAppearanceLightStatusBars     = !useDarkTheme
            insetsController.isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = BloomTypography,
        shapes      = BloomShapes,
        content     = content,
    )
}
