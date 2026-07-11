package com.bloom.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.bloom.app.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Shared UI Components
//
// Every composable here:
//   - Is focused on a single responsibility
//   - Accepts only what it needs via parameters
//   - Uses MaterialTheme tokens (never hardcoded colors)
//   - Is documented with its intended use case
// ─────────────────────────────────────────────────────────────────────────────


// ── BloomCard ────────────────────────────────────────────────────────────────
/**
 * The standard content card for Bloom.
 * Rounded, soft shadow, warm surface color.
 * Used wherever content needs visual grouping.
 */
@Composable
fun BloomCard(
    modifier        : Modifier = Modifier,
    onClick         : (() -> Unit)? = null,
    content         : @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue   = if (isPressed) 1.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "card_elevation",
    )

    Surface(
        modifier = modifier
            .shadow(
                elevation   = elevation,
                shape       = MaterialTheme.shapes.large,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            )
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication        = null,
                    onClick           = onClick,
                ) else Modifier
            ),
        color  = MaterialTheme.colorScheme.surface,
        shape  = MaterialTheme.shapes.large,
    ) {
        Column(content = content)
    }
}


// ── BloomPrimaryButton ────────────────────────────────────────────────────────
/**
 * Primary CTA button — pill shape, amber fill, subtle press animation.
 */
@Composable
fun BloomPrimaryButton(
    text        : String,
    onClick     : () -> Unit,
    modifier    : Modifier = Modifier,
    enabled     : Boolean  = true,
    leadingIcon : (@Composable () -> Unit)? = null,
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.height(52.dp),
        shape    = PillShape,
        colors   = ButtonDefaults.buttonColors(
            containerColor  = MaterialTheme.colorScheme.primary,
            contentColor    = MaterialTheme.colorScheme.onPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = 0.dp,
            pressedElevation  = 0.dp,
        ),
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text  = text,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}


// ── BloomTextButton ───────────────────────────────────────────────────────────
/**
 * Secondary action — no background, warm primary color.
 */
@Composable
fun BloomTextButton(
    text    : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick  = onClick,
        modifier = modifier,
        colors   = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}


// ── QuoteCard ─────────────────────────────────────────────────────────────────
/**
 * Displays the daily quote on the Home screen.
 * Italic Lora for the quote text — editorial and warm.
 */
@Composable
fun QuoteCard(
    quote       : String,
    author      : String,
    modifier    : Modifier = Modifier,
) {
    BloomCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text  = "\u201C$quote\u201D",    // Unicode left/right double quotes
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "— $author",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


// ── StreakBadge ───────────────────────────────────────────────────────────────
/**
 * Compact streak indicator.
 * Shows a flame emoji + count. Simple and celebratory.
 */
@Composable
fun StreakBadge(
    streakDays  : Int,
    modifier    : Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape    = PillShape,
        color    = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier                = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement   = Arrangement.spacedBy(6.dp),
            verticalAlignment       = Alignment.CenterVertically,
        ) {
            Text(text = "🌱", style = MaterialTheme.typography.bodyMedium)
            Text(
                text  = "$streakDays Day Reflection Streak",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}


// ── SectionHeader ─────────────────────────────────────────────────────────────
/**
 * Consistent section label used across screens.
 */
@Composable
fun SectionHeader(
    text        : String,
    modifier    : Modifier = Modifier,
) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.titleSmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}


// ── BloomDivider ──────────────────────────────────────────────────────────────
/**
 * Subtle horizontal divider — lighter than Material default.
 */
@Composable
fun BloomDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier,
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant,
    )
}

// ── Shimmer Loading ───────────────────────────────────────────────────────────

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmerStartOffset"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    background(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = shimmerColors,
            start = androidx.compose.ui.geometry.Offset(startOffsetX, 0f),
            end = androidx.compose.ui.geometry.Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun BloomShimmerBlock(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect()
    )
}

@Composable
fun HomeShimmerLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column {
            BloomShimmerBlock(modifier = Modifier.height(32.dp).width(200.dp))
            Spacer(modifier = Modifier.height(8.dp))
            BloomShimmerBlock(modifier = Modifier.height(48.dp).width(150.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BloomShimmerBlock(modifier = Modifier.height(20.dp).width(120.dp))
            BloomShimmerBlock(modifier = Modifier.height(100.dp).fillMaxWidth())
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BloomShimmerBlock(modifier = Modifier.height(20.dp).width(120.dp))
            BloomShimmerBlock(modifier = Modifier.height(120.dp).fillMaxWidth())
        }
    }
}
