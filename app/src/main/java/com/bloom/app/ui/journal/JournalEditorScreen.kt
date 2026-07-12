package com.bloom.app.ui.journal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.bloom.app.BuildConfig
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.ai.ReflectionState
import com.bloom.app.data.model.Mood
import com.bloom.app.ui.components.BloomDivider
import com.bloom.app.ui.components.BloomPrimaryButton
import com.bloom.app.ui.theme.*
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// JournalEditorScreen
//
// Design philosophy:
//   - Maximum focus: nearly nothing visible except the writing area
//   - No title field — people don't title private journals
//   - Prompt shown subtly above the text field — gentle, not mandatory
//   - Mood selector at bottom — small, unobtrusive, horizontal chips
//   - "✨ Reflect" button appears only after content is entered
//   - Reflection appears inline below the writing — never a separate screen
//   - Word count shown — gives a quiet sense of progress
//   - Back saves automatically — no "Save" anxiety
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun JournalEditorScreen(
    entryId     : Long,
    onBack      : () -> Unit,
    viewModel   : JournalViewModel = viewModel(),
) {
    val state by viewModel.editorState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    // Auto-save on back — the user should never worry about losing their words
    val onBackWithSave: () -> Unit = {
        if (state.content.isNotBlank() && !state.isSaved) {
            viewModel.saveEntry()
        }
        onBack()
    }

    LaunchedEffect(Unit) {
        // Small delay to let the slide-up animation complete before focusing
        if (entryId < 0) {
            delay(350)
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ───────────────────────────────────────────────────────
            EditorTopBar(
                wordCount = state.wordCount,
                isSaved   = state.isSaved,
                onBack    = onBackWithSave,
                onSave    = viewModel::saveEntry,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // ── Date header ───────────────────────────────────────────────
                Text(
                    text      = com.bloom.app.util.DateUtils.formatEntryDate(System.currentTimeMillis()),
                    style     = MaterialTheme.typography.labelMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier  = Modifier.padding(horizontal = 24.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Prompt (soft, optional) ───────────────────────────────────
                state.activePrompt?.let { prompt ->
                    if (state.content.isEmpty()) {
                        Text(
                            text     = prompt,
                            style    = MaterialTheme.typography.headlineSmall,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── Writing area ──────────────────────────────────────────────
                BasicTextField(
                    value         = state.content,
                    onValueChange = viewModel::onContentChanged,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 240.dp)
                        .padding(horizontal = 24.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            // Placeholder shown when completely empty
                            if (state.content.isEmpty() && state.activePrompt == null) {
                                Text(
                                    text  = "What's on your mind?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── AI Reflection area ────────────────────────────────────────
                AnimatedVisibility(
                    visible = state.content.length >= 20,
                    enter   = fadeIn(tween(400)) + expandVertically(tween(400)),
                ) {
                    ReflectionSection(
                        reflectionState = state.reflectionState,
                        aiReflection    = state.aiReflection,
                        onReflect       = viewModel::requestReflection,
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

            // ── Mood selector (bottom, above keyboard) ────────────────────────
            MoodSelectorBar(
                selectedMood = state.selectedMood,
                onMoodSelect = viewModel::onMoodSelected,
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun EditorTopBar(
    wordCount   : Int,
    isSaved     : Boolean,
    onBack      : () -> Unit,
    onSave      : () -> Unit,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onSurface,
            )
        }

        AnimatedVisibility(visible = wordCount > 0) {
            Text(
                text  = "$wordCount ${if (wordCount == 1) "word" else "words"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Saved indicator fades in when saved
        AnimatedVisibility(
            visible = isSaved,
            enter   = fadeIn(),
            exit    = fadeOut(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text  = "Saved",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (!isSaved) {
            TextButton(onClick = onSave) {
                Text(
                    text  = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// ── Reflection Section ────────────────────────────────────────────────────────

@Composable
private fun ReflectionSection(
    reflectionState : ReflectionState?,
    aiReflection    : String?,
    onReflect       : () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        BloomDivider()
        Spacer(modifier = Modifier.height(24.dp))

        val haptic = LocalHapticFeedback.current
        val hasApiKey = BuildConfig.GROQ_API_KEY.isNotEmpty()

        LaunchedEffect(aiReflection, reflectionState) {
            if (aiReflection != null && reflectionState !is ReflectionState.Loading && reflectionState !is ReflectionState.Streaming) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        when {
            // Reflection exists — show it
            aiReflection != null && reflectionState !is ReflectionState.Loading -> {
                Text(
                    text  = "✨ A gentle reflection",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text  = if (reflectionState is ReflectionState.Streaming)
                                reflectionState.text
                            else
                                aiReflection,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Show loading indicator during streaming
                if (reflectionState is ReflectionState.Streaming) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color    = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }

            // Error state
            reflectionState is ReflectionState.Error -> {
                Text(
                    text  = reflectionState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                BloomPrimaryButton(
                    text     = "✨ Try again",
                    onClick  = onReflect,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Loading
            reflectionState is ReflectionState.Loading -> {
                RotatingLoadingText()
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth(),
                    color      = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )
            }

            // No reflection yet — show button
            else -> {
                if (hasApiKey) {
                    BloomPrimaryButton(
                        text     = "✨  Reflect",
                        onClick  = onReflect,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text      = "Get a gentle reflection on what you've written.",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text      = "⚠ Gemini API key not configured. Add it in local.properties to enable AI reflections.",
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RotatingLoadingText() {
    val loadingPhrases = listOf(
        "✨ Reading your reflection...",
        "🌱 Finding emotional patterns...",
        "💭 Creating insight...",
        "🌼 Almost done..."
    )
    var currentIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            currentIndex = (currentIndex + 1) % loadingPhrases.size
        }
    }
    
    AnimatedContent(
        targetState = currentIndex,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "loading_text_rotation"
    ) { index ->
        Text(
            text  = loadingPhrases[index],
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Mood Selector Bar ─────────────────────────────────────────────────────────

@Composable
private fun MoodSelectorBar(
    selectedMood : Mood,
    onMoodSelect : (Mood) -> Unit,
) {
    Surface(
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column {
            BloomDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Feeling:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Mood.entries.forEach { mood ->
                        val isSelected = mood == selectedMood
                        val scale by animateFloatAsState(
                            targetValue   = if (isSelected) 1.2f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label         = "mood_chip_scale_${mood.name}",
                        )

                        FilterChip(
                            selected  = isSelected,
                            onClick   = { onMoodSelect(mood) },
                            modifier  = Modifier.scale(scale),
                            label = {
                                Text(
                                    text  = "${mood.emoji} ${mood.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor  = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor      = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
        }
    }
}
