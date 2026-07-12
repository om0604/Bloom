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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.shadow
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
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

            // ── Mood Slider ────────────────────────────────────────────────────
            MoodSlider(
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

// ── Mood Slider ─────────────────────────────────────────────────────────────

@Composable
private fun MoodSlider(
    selectedMood: Mood?,
    onMoodSelect: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val moods = Mood.entries
    val density = LocalDensity.current

    var trackWidth by remember { mutableFloatStateOf(0f) }
    var dragOffset by remember { mutableFloatStateOf(-1f) }
    var isDragging by remember { mutableStateOf(false) }

    val segmentWidth = if (trackWidth > 0) trackWidth / (moods.size - 1) else 0f
    
    val targetOffset = when {
        isDragging && dragOffset >= 0 -> dragOffset.coerceIn(0f, trackWidth)
        selectedMood != null -> (selectedMood.ordinal * segmentWidth)
        else -> trackWidth / 2f
    }

    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "slider_thumb_offset"
    )

    val activeIndex = if (segmentWidth > 0) {
        (animatedOffset / segmentWidth).roundToInt().coerceIn(0, moods.lastIndex)
    } else selectedMood?.ordinal ?: 2

    var lastHapticIndex by remember { mutableIntStateOf(activeIndex) }
    LaunchedEffect(activeIndex, isDragging) {
        if (isDragging && activeIndex != lastHapticIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticIndex = activeIndex
        }
    }

    val activeColor by animateColorAsState(
        targetValue = if (selectedMood != null || isDragging) moods[activeIndex].toColor() else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "slider_color"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .onSizeChanged { trackWidth = it.width.toFloat() }
                .pointerInput(trackWidth) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragOffset = offset.x.coerceIn(0f, trackWidth)
                        },
                        onDragEnd = {
                            isDragging = false
                            val finalIndex = (dragOffset / segmentWidth).roundToInt().coerceIn(0, moods.lastIndex)
                            onMoodSelect(moods[finalIndex])
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset = (dragOffset + dragAmount).coerceIn(0f, trackWidth)
                        }
                    )
                }
                .pointerInput(trackWidth) {
                    detectTapGestures(
                        onTap = { offset ->
                            val tappedIndex = (offset.x / segmentWidth).roundToInt().coerceIn(0, moods.lastIndex)
                            onMoodSelect(moods[tappedIndex])
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(density) { animatedOffset.toDp() })
                        .clip(MaterialTheme.shapes.small)
                        .background(activeColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moods.forEach { 
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (it.ordinal <= activeIndex && (selectedMood != null || isDragging)) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            val thumbScale by animateFloatAsState(
                targetValue = if (isDragging) 1.2f else if (selectedMood != null) 1.1f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "thumb_scale"
            )
            
            Box(
                modifier = Modifier
                    .offset { IntOffset((animatedOffset - 24.dp.toPx()).roundToInt(), 0) }
                    .size(48.dp)
                    .scale(thumbScale)
                    .shadow(4.dp, MaterialTheme.shapes.extraLarge)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, activeColor, MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                val emojiSize by animateFloatAsState(if (selectedMood != null || isDragging) 24f else 20f, label="emoji")
                Text(
                    text = moods[activeIndex].emoji,
                    fontSize = emojiSize.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            moods.forEachIndexed { index, mood ->
                val isSelected = index == activeIndex && (selectedMood != null || isDragging)
                val labelAlpha by animateFloatAsState(if (isSelected) 1f else 0.5f, label="alpha")
                
                Text(
                    text = mood.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = labelAlpha),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
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
