package com.bloom.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bloom.app.R

// ─────────────────────────────────────────────────────────────────────────────
// Bloom Typography System
//
// Two typefaces, two distinct purposes:
//
// LORA (Serif) — Used for display text, headings, quotes, journal prompts.
//   Warm, editorial, and humanistic. Creates the feeling of a real journal.
//   Inspired by Apple Journal and Readwise Reader.
//
// DM SANS (Sans-serif) — Used for all UI chrome: labels, captions, buttons,
//   navigation, metadata. Clean and modern without feeling cold.
//
// This combination balances emotional warmth (Lora) with functional
// clarity (DM Sans). Neither fights the other.
// ─────────────────────────────────────────────────────────────────────────────

val LoraFontFamily = FontFamily.Serif

val DmSansFontFamily = FontFamily.SansSerif

// ── Bloom Typography Scale ────────────────────────────────────────────────────
val BloomTypography = Typography(

    // ── Display — Reserved for hero moments (garden, onboarding) ─────────────
    displayLarge = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 52.sp,
        lineHeight  = 60.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 42.sp,
        lineHeight  = 50.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 34.sp,
        lineHeight  = 42.sp,
        letterSpacing = 0.sp,
    ),

    // ── Headline — Screen titles, section headings ────────────────────────────
    headlineLarge = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 28.sp,
        lineHeight  = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 24.sp,
        lineHeight  = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 20.sp,
        lineHeight  = 28.sp,
        letterSpacing = 0.sp,
    ),

    // ── Title — Card headers, prompts, key labels ─────────────────────────────
    titleLarge = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 18.sp,
        lineHeight  = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 15.sp,
        lineHeight  = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 13.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body — Journal text, readable content ─────────────────────────────────
    // Body uses Lora so that the act of reading journal entries feels warm
    bodyLarge = TextStyle(
        fontFamily  = LoraFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 17.sp,
        lineHeight  = 28.sp,           // Generous line-height for readability
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 22.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 12.sp,
        lineHeight  = 18.sp,
        letterSpacing = 0.4.sp,
    ),

    // ── Label — UI chrome, navigation, metadata ───────────────────────────────
    labelLarge = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily  = DmSansFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 11.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
