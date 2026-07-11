package com.bloom.app.ui.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.data.model.GardenStage
import com.bloom.app.data.model.Mood
import com.bloom.app.ui.components.BloomCard
import com.bloom.app.ui.components.SectionHeader
import com.bloom.app.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// InsightsScreen
//
// Design philosophy (from brief):
//   "Do not build complicated charts."
//   "Make Insights feel encouraging rather than analytical."
//
// Approach:
//   - Human-language summaries (not metric labels)
//   - Mood dots visualization — simple, warm, no axes
//   - 3 stat cards — streak, entries, words — minimal numbers
//   - Garden stage as a milestone statement
//   - Empty state that encourages, not guilts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = MaterialTheme.colorScheme.primary,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // ── Page title ────────────────────────────────────────────────
                Text(
                    text  = "Your journey",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                )

                if (state.totalEntries < 3) {
                    InsightsEmptyState(totalEntries = state.totalEntries)
                } else {
                    // ── Human summaries ───────────────────────────────────────
                    InsightsSummarySection(state = state)

                    // ── 3-column stat chips ───────────────────────────────────
                    InsightsStatsRow(state = state)

                    // ── Mood breakdown (last 30 days) ─────────────────────────
                    if (state.moodBreakdown.isNotEmpty()) {
                        MoodBreakdownSection(breakdown = state.moodBreakdown)
                    }

                    // ── Garden milestone ──────────────────────────────────────
                    GardenMilestoneCard(
                        stage      = state.gardenStage,
                        entryCount = state.totalEntries,
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ── Human-language summaries ──────────────────────────────────────────────────

@Composable
private fun InsightsSummarySection(state: InsightsUiState) {
    val sentences = buildList {
        if (state.streakDays > 0) {
            add("You've reflected ${state.streakDays} ${if (state.streakDays == 1) "day" else "days"} in a row. That's something.")
        }
        if (state.totalEntries > 0) {
            add("You've written ${state.totalEntries} ${if (state.totalEntries == 1) "journal entry" else "journal entries"} — ${state.totalWords} words total.")
        }
        if (state.mostCommonMood != null) {
            add("${state.mostCommonMood.displayName} ${state.mostCommonMood.emoji} has been your most frequent mood lately.")
        }
        if (state.reflectionCount > 0) {
            add("You've explored ${state.reflectionCount} gentle ${if (state.reflectionCount == 1) "reflection" else "reflections"}.")
        }
    }

    BloomCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier            = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            sentences.forEachIndexed { index, sentence ->
                Text(
                    text  = sentence,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (index < sentences.size - 1) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────

@Composable
private fun InsightsStatsRow(state: InsightsUiState) {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }

    val streak by animateIntAsState(
        targetValue = if (animated) state.streakDays else 0,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "streak_anim"
    )
    val entries by animateIntAsState(
        targetValue = if (animated) state.totalEntries else 0,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "entries_anim"
    )
    val words by animateIntAsState(
        targetValue = if (animated) state.totalWords else 0,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "words_anim"
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            emoji = "🔥",
            value = "$streak",
            label = "day streak",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            emoji = "📖",
            value = "$entries",
            label = "entries",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            emoji = "✍️",
            value = if (words > 999) "${words / 1000}k" else "$words",
            label = "words",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    emoji    : String,
    value    : String,
    label    : String,
    modifier : Modifier = Modifier,
) {
    BloomCard(modifier = modifier) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                text  = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Mood Breakdown ────────────────────────────────────────────────────────────
// Shown as emoji dots — warm, visual, not a bar chart

@Composable
private fun MoodBreakdownSection(breakdown: Map<Mood, Int>) {
    val total = breakdown.values.sum()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(text = "Mood in the last 30 days")

        BloomCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Mood.entries.forEach { mood ->
                    val count = breakdown[mood] ?: 0
                    if (count > 0) {
                        MoodBreakdownRow(
                            mood  = mood,
                            count = count,
                            total = total,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodBreakdownRow(
    mood  : Mood,
    count : Int,
    total : Int,
) {
    val fraction = count.toFloat() / total.toFloat()
    val animatedFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(800, easing = EaseOutCubic),
        label         = "mood_fraction_${mood.name}",
    )
    val moodColor = mood.toInsightsColor()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(text = mood.emoji, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text  = mood.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text  = "$count ${if (count == 1) "day" else "days"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LinearProgressIndicator(
            progress   = { animatedFraction },
            modifier   = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color      = moodColor,
            trackColor = moodColor.copy(alpha = 0.15f),
            strokeCap  = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

// ── Garden Milestone Card ─────────────────────────────────────────────────────

@Composable
private fun GardenMilestoneCard(
    stage      : GardenStage,
    entryCount : Int,
) {
    BloomCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            com.bloom.app.ui.garden.GardenPlant(
                stage    = stage,
                modifier = Modifier.size(64.dp),
                animated = false,
            )
            Column {
                Text(
                    text  = "Your garden is a ${stage.displayName.lowercase()}.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = stage.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun InsightsEmptyState(totalEntries: Int) {
    val remaining = 3 - totalEntries
    Box(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text      = "Insights are locked",
                style     = MaterialTheme.typography.headlineSmall,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = "Write $remaining more ${if (remaining == 1) "entry" else "entries"} to unlock your insights.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Mood color helpers ────────────────────────────────────────────────────────

private fun Mood.toInsightsColor() = when (this) {
    Mood.GREAT -> MoodGreat
    Mood.GOOD  -> MoodGood
    Mood.OKAY  -> MoodOkay
    Mood.LOW   -> MoodLow
    Mood.ROUGH -> MoodRough
}
