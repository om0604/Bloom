package com.bloom.app.ui.mood

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.data.model.Mood
import com.bloom.app.ui.components.BloomPrimaryButton
import com.bloom.app.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// MoodCheckInScreen
//
// Design philosophy:
//   - This is a 30-second interaction — it should feel effortless
//   - 5 mood cards in a row — tap once, done
//   - Selected mood: scale up, color fill, emoji enlarges
//   - Unselected moods: dim slightly — focus user attention
//   - Optional note below — short, personal, not a required field
//   - "Add to journal" shortcut — mood → journal in one flow
//   - No submit button until a mood is selected — avoids empty saves
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MoodCheckInScreen(
    preselectedMood: String? = null,
    onComplete    : () -> Unit,
    onWriteEntry  : () -> Unit,
    viewModel     : MoodViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onComplete()
    }

    LaunchedEffect(preselectedMood) {
        if (preselectedMood != null) {
            Mood.entries.find { it.name == preselectedMood }?.let {
                viewModel.selectMood(it)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Close button ──────────────────────────────────────────────────
            Row(
                modifier      = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onComplete) {
                    Icon(
                        imageVector        = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Headline ──────────────────────────────────────────────────────
            Text(
                text      = "How are you feeling right now?",
                style     = MaterialTheme.typography.displaySmall,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text      = "There's no wrong answer.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Mood Cards ────────────────────────────────────────────────────
            MoodCardRow(
                selectedMood = state.selectedMood,
                onMoodSelect = { mood ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.selectMood(mood)
                },
            )

            // ── Selected Mood Label ───────────────────────────────────────────
            AnimatedVisibility(
                visible = state.selectedMood != null,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text  = state.selectedMood?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Optional Note ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.selectedMood != null,
                enter   = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit    = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    OutlinedTextField(
                        value         = state.note,
                        onValueChange = viewModel::onNoteChanged,
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text  = "Add a note… (optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        },
                        textStyle       = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization  = KeyboardCapitalization.Sentences,
                            imeAction       = ImeAction.Done,
                        ),
                        shape  = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                        supportingText = {
                            Text(
                                text  = "${state.note.length}/80",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Actions ───────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.selectedMood != null,
                enter   = fadeIn() + slideInVertically { it / 2 },
                exit    = fadeOut(),
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BloomPrimaryButton(
                        text     = "Save",
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveMoodEntry()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    TextButton(
                        onClick = onWriteEntry,
                        colors  = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text(
                            text  = "✍️  Write about it",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Mood Card Row ─────────────────────────────────────────────────────────────

@Composable
private fun MoodCardRow(
    selectedMood : Mood?,
    onMoodSelect : (Mood) -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Mood.entries.forEach { mood ->
            MoodCard(
                mood         = mood,
                isSelected   = mood == selectedMood,
                anySelected  = selectedMood != null,
                onSelect     = { onMoodSelect(mood) },
                modifier     = Modifier.weight(1f),
            )
        }
    }
}

// ── Individual Mood Card ──────────────────────────────────────────────────────

@Composable
private fun MoodCard(
    mood        : Mood,
    isSelected  : Boolean,
    anySelected : Boolean,
    onSelect    : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Scale: selected → bigger, unselected (when another is selected) → slightly smaller
    val scale by animateFloatAsState(
        targetValue = when {
            isSelected          -> if (isPressed) 0.95f else 1.08f
            anySelected         -> 0.92f
            isPressed           -> 0.96f
            else                -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "mood_scale_${mood.name}",
    )

    val moodColor = mood.toColor()

    val backgroundColor by animateColorAsState(
        targetValue   = if (isSelected) moodColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label         = "mood_bg_${mood.name}",
    )

    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) moodColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(250),
        label         = "mood_border_${mood.name}",
    )

    val contentAlpha by animateFloatAsState(
        targetValue   = if (anySelected && !isSelected) 0.5f else 1f,
        animationSpec = tween(200),
        label         = "mood_alpha_${mood.name}",
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onSelect,
            )
            .padding(vertical = 16.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Emoji — enlarges on selection
        val emojiSize by animateFloatAsState(
            targetValue   = if (isSelected) 28f else 22f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label         = "emoji_size_${mood.name}",
        )
        Text(
            text  = mood.emoji,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = emojiSize.sp),
        )

        Text(
            text  = mood.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
        )
    }
}

// ── Color Mapping ─────────────────────────────────────────────────────────────

private fun Mood.toColor(): Color = when (this) {
    Mood.GREAT -> MoodGreat
    Mood.GOOD  -> MoodGood
    Mood.OKAY  -> MoodOkay
    Mood.LOW   -> MoodLow
    Mood.ROUGH -> MoodRough
}
