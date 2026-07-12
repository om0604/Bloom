package com.bloom.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.bloom.app.data.model.Mood
import com.bloom.app.ui.theme.*

@Composable
fun MoodSlider(
    selectedMood: Mood?,
    onMoodSelect: (Mood) -> Unit,
    onMoodHover: (Mood) -> Unit = {},
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
        if (isDragging) {
            if (activeIndex != lastHapticIndex) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                lastHapticIndex = activeIndex
            }
            onMoodHover(moods[activeIndex])
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
                .padding(horizontal = 24.dp)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            moods.forEachIndexed { index, mood ->
                val isSelected = index == activeIndex && (selectedMood != null || isDragging)
                val labelAlpha by animateFloatAsState(if (isSelected) 1f else 0.5f, label="alpha")
                
                Text(
                    text = mood.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = labelAlpha),
                    modifier = Modifier
                        .offset { IntOffset((index * segmentWidth - 24.dp.toPx()).roundToInt(), 0) }
                        .width(48.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Color Mapping ─────────────────────────────────────────────────────────────

fun Mood.toColor(): Color = when (this) {
    Mood.GREAT -> MoodGreat
    Mood.GOOD  -> MoodGood
    Mood.OKAY  -> MoodOkay
    Mood.LOW   -> MoodLow
    Mood.ROUGH -> MoodRough
}
