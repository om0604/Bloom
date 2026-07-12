package com.bloom.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.data.model.GardenStage
import com.bloom.app.data.model.Mood
import com.bloom.app.data.model.MoodEntry
import com.bloom.app.ui.components.*
import com.bloom.app.ui.garden.GardenPlant
import com.bloom.app.ui.theme.MoodGood
import com.bloom.app.ui.theme.MoodGreat
import com.bloom.app.ui.theme.MoodLow
import com.bloom.app.ui.theme.MoodOkay
import com.bloom.app.ui.theme.MoodRough
import com.bloom.app.util.DateUtils
import com.bloom.app.util.ThemePreference

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen
//
// Design review checklist:
//   ✓ Does NOT feel like a dashboard
//   ✓ One clear action at a time
//   ✓ Generous whitespace
//   ✓ Large typography for greeting
//   ✓ Mood check-in is prominent and warm
//   ✓ Latest entry shown — feels like a journal, not a feed
//   ✓ Garden preview — emotional anchor
//   ✓ Quote is present but not dominant
//   ✓ No statistics, no charts, no numbers except streak
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNewEntry      : () -> Unit,
    onContinueEntry : (Long) -> Unit,
    onSettings      : () -> Unit,
    viewModel       : HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themePref by viewModel.themePreference.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (state.isLoading) {
            HomeShimmerLoading()
        } else {
            HomeContent(
                state           = state,
                themePref       = themePref,
                onMoodSelect    = viewModel::setTodaysMood,
                onNewEntry      = onNewEntry,
                onContinueEntry = onContinueEntry,
                onSettings      = onSettings,
                onToggleTheme   = viewModel::toggleTheme
            )
        }
    }
}

@Composable
private fun HomeContent(
    state           : HomeUiState,
    themePref       : ThemePreference,
    onMoodSelect    : (Mood) -> Unit,
    onNewEntry      : () -> Unit,
    onContinueEntry : (Long) -> Unit,
    onSettings      : () -> Unit,
    onToggleTheme   : () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    var gardenBounce by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.bloom.app.util.AppEventBus.events.collect { event ->
            if (event is com.bloom.app.util.AppEvent.JournalEntrySaved) {
                gardenBounce = true
                snackbarHostState.showSnackbar("🌱 Reflection Saved\nYour garden grew today.")
                gardenBounce = false
            }
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        delay(300) // Wait for transition to finish for butter-smooth UX
        visible = true 
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 24.dp),
        ) {
        // ── Header: Greeting + Streak ─────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = 100)) + fadeIn(animationSpec = tween(400, delayMillis = 100))
        ) {
            HomeHeader(
                greeting   = state.greeting,
                userName   = state.userName,
                streakDays = state.streakDays,
                themePref  = themePref,
                onSettings = onSettings,
                onToggleTheme = onToggleTheme,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Mood Check-in ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = 250)) + fadeIn(animationSpec = tween(400, delayMillis = 250))
        ) {
            MoodSection(
                todaysMood   = state.todaysMood,
                onMoodSelect = onMoodSelect,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Continue Writing / Start Entry ────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = 400)) + fadeIn(animationSpec = tween(400, delayMillis = 400))
        ) {
            WriteSection(
                latestEntry     = state.latestEntry,
                hasEntryToday   = state.hasEntryToday,
                onNewEntry      = onNewEntry,
                onContinueEntry = onContinueEntry,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Garden Preview ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = 550)) + fadeIn(animationSpec = tween(400, delayMillis = 550))
        ) {
            Box {
                GardenPreviewSection(
                    gardenStage = state.gardenStage,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Daily Quote ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400, delayMillis = 700)) + fadeIn(animationSpec = tween(400, delayMillis = 700))
        ) {
            QuoteCard(
                quote    = state.quote.first,
                author   = state.quote.second,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    greeting   : String,
    userName   : String,
    streakDays : Int,
    themePref  : ThemePreference,
    onSettings : () -> Unit,
    onToggleTheme: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            // Streak badge — top right would feel crowded; put it above the greeting
            if (streakDays > 0) {
                StreakBadge(streakDays = streakDays)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text  = "$greeting,",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Name on its own line — creates visual weight and warmth
            AnimatedContent(
                targetState = userName.replaceFirstChar { it.uppercase() },
                transitionSpec = { fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400)) },
                label = "userNameCrossfade"
            ) { targetName ->
                Text(
                    text  = targetName,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (themePref == ThemePreference.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Mood Section ──────────────────────────────────────────────────────────────

@Composable
private fun MoodSection(
    todaysMood    : MoodEntry?,
    onMoodSelect  : (Mood) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(text = "How are you feeling?")

        var currentlyActiveMood by remember(todaysMood) { mutableStateOf(todaysMood?.mood) }

        MoodSlider(
            selectedMood = todaysMood?.mood,
            onMoodSelect = { mood -> 
                currentlyActiveMood = mood
                onMoodSelect(mood) 
            },
            onMoodHover = { currentlyActiveMood = it }
        )

        AnimatedVisibility(
            visible = currentlyActiveMood != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentlyActiveMood?.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Write Section ─────────────────────────────────────────────────────────────

@Composable
private fun WriteSection(
    latestEntry     : com.bloom.app.data.model.JournalEntry?,
    hasEntryToday   : Boolean,
    onNewEntry      : () -> Unit,
    onContinueEntry : (Long) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(
            text = if (hasEntryToday) "Today's reflection" else "Ready to reflect?"
        )

        if (latestEntry != null && hasEntryToday) {
            // Show today's entry — tap to continue
            BloomCard(
                onClick  = { onContinueEntry(latestEntry.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text     = latestEntry.content,
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(
                            text  = "${latestEntry.wordCount} words",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text  = "Continue",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        } else {
            // New entry prompt
            BloomPrimaryButton(
                text     = "Write today's entry",
                onClick  = onNewEntry,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Text(text = "✍️", style = MaterialTheme.typography.bodyMedium)
                }
            )

            // If there's a previous entry (just not today), show subtle prompt
            if (latestEntry != null) {
                Text(
                    text  = "Last reflected ${DateUtils.formatEntryDate(latestEntry.createdAt).lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

// ── Garden Preview ────────────────────────────────────────────────────────────

@Composable
private fun GardenPreviewSection(gardenStage: GardenStage) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(text = "Your garden")

        BloomCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text  = gardenStage.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = gardenStage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Animated plant preview
                GardenPlant(
                    stage    = gardenStage,
                    modifier = Modifier.size(80.dp),
                    animated = true,
                )
            }
        }
    }
}
