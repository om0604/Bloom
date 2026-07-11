package com.bloom.app.ui.garden

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloom.app.data.model.GardenStage
import com.bloom.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// ─────────────────────────────────────────────────────────────────────────────
// GardenScreen
//
// Design philosophy:
//   - The garden is the emotional signature of Bloom
//   - NOT a progress bar — a living illustration
//   - The plant is drawn with Canvas — fully custom, no image assets
//   - Each growth stage has a distinct visual character
//   - Subtle breathing animation makes it feel alive
//   - Encouraging copy — never gamified, never about productivity
//   - The message below adapts to the user's growth stage
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GardenScreen(viewModel: GardenViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800)),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text  = "Your garden",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    if (state.entryCount == 0) {
                        // Empty State
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Your garden is waiting to be planted.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Every reflection you write helps it grow.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // ── The plant ─────────────────────────────────────────────────
                        GardenPlant(
                            stage    = state.stage,
                            modifier = Modifier.size(240.dp),
                            animated = true,
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // ── Stage label ───────────────────────────────────────────────
                        AnimatedContent(
                            targetState = state.stage,
                            transitionSpec = {
                                fadeIn(tween(600)) togetherWith fadeOut(tween(300))
                            },
                            label = "stage_transition",
                        ) { stage ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text  = stage.displayName,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text      = stage.description,
                                    style     = MaterialTheme.typography.bodyLarge,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // ── Progress to next stage ────────────────────────────────────
                        if (state.nextStage != null) {
                            GardenProgressSection(
                                currentCount          = state.entryCount,
                                nextStage             = state.nextStage,
                                entriesUntilNextStage = state.entriesUntilNextStage,
                            )
                        } else {
                            // Fully grown
                            Surface(
                                color  = MaterialTheme.colorScheme.secondaryContainer,
                                shape  = MaterialTheme.shapes.large,
                            ) {
                                Text(
                                    text     = "🌳  Your tree is fully grown.\nKeep reflecting — it keeps you growing.",
                                    style    = MaterialTheme.typography.bodyLarge,
                                    color    = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(24.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── Entry count ───────────────────────────────────────────────
                        Text(
                            text  = "${state.entryCount} ${if (state.entryCount == 1) "reflection" else "reflections"} written",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Progress Section ──────────────────────────────────────────────────────────

@Composable
private fun GardenProgressSection(
    currentCount          : Int,
    nextStage             : GardenStage?,
    entriesUntilNextStage : Int,
) {
    if (nextStage == null) return

    val currentStage = GardenStage.fromEntryCount(currentCount)
    val range = nextStage.minEntries - currentStage.minEntries
    val progress = if (range > 0) {
        (currentCount - currentStage.minEntries).toFloat() / range.toFloat()
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label         = "garden_progress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text  = "$entriesUntilNextStage more ${if (entriesUntilNextStage == 1) "reflection" else "reflections"} until ${nextStage.displayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LinearProgressIndicator(
            progress         = { animatedProgress },
            modifier         = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color            = MaterialTheme.colorScheme.secondary,
            trackColor       = MaterialTheme.colorScheme.secondaryContainer,
            strokeCap        = StrokeCap.Round,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GardenPlant — Canvas-drawn plant illustration
//
// Each stage has a unique visual representation:
//   SEED:   A small brown oval in the soil
//   SPROUT: A stem with two small leaves
//   LEAF:   A taller stem with larger paired leaves
//   FLOWER: Stem + leaves + a circular bloom on top
//   TREE:   Trunk + branching + full canopy
//
// "animated" enables a gentle breathing scale animation.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GardenPlant(
    stage    : GardenStage,
    modifier : Modifier = Modifier,
    animated : Boolean  = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "garden_breathe")

    val breatheScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue  = 1f,
            targetValue   = 1.04f,
            animationSpec = infiniteRepeatable(
                animation  = tween(2500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "breathe_scale",
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    // Animate stage transition
    val targetAlpha by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(800),
        label         = "plant_alpha",
    )

    Canvas(
        modifier = modifier
            .scale(breatheScale)
            .fillMaxSize(),
    ) {
        when (stage) {
            GardenStage.SEED    -> drawSeed()
            GardenStage.SPROUT  -> drawSprout()
            GardenStage.LEAF    -> drawLeaf()
            GardenStage.FLOWER  -> drawFlower()
            GardenStage.TREE    -> drawTree()
        }
    }
}

// ── Plant Drawing Functions ───────────────────────────────────────────────────

private fun DrawScope.drawSeed() {
    val cx = size.width / 2
    val cy = size.height * 0.72f

    // Soil line
    drawLine(
        color       = GardenSeed.copy(alpha = 0.3f),
        start       = Offset(cx - size.width * 0.3f, cy),
        end         = Offset(cx + size.width * 0.3f, cy),
        strokeWidth = 3f,
        cap         = StrokeCap.Round,
    )

    // Seed oval
    drawOval(
        color   = GardenSeed,
        topLeft = Offset(cx - size.width * 0.1f, cy - size.height * 0.07f),
        size    = Size(size.width * 0.2f, size.height * 0.12f),
    )
}

private fun DrawScope.drawSprout() {
    val cx  = size.width / 2
    val baseY = size.height * 0.75f
    val stemHeight = size.height * 0.35f

    // Stem
    drawLine(
        color       = GardenSprout,
        start       = Offset(cx, baseY),
        end         = Offset(cx, baseY - stemHeight),
        strokeWidth = 6f,
        cap         = StrokeCap.Round,
    )

    // Left leaf
    val leafPath = Path().apply {
        moveTo(cx, baseY - stemHeight * 0.5f)
        quadraticBezierTo(
            cx - size.width * 0.2f, baseY - stemHeight * 0.65f,
            cx - size.width * 0.15f, baseY - stemHeight * 0.75f,
        )
        quadraticBezierTo(
            cx - size.width * 0.05f, baseY - stemHeight * 0.6f,
            cx, baseY - stemHeight * 0.5f,
        )
    }
    drawPath(leafPath, color = GardenSprout)

    // Right leaf
    val rightLeafPath = Path().apply {
        moveTo(cx, baseY - stemHeight * 0.55f)
        quadraticBezierTo(
            cx + size.width * 0.2f, baseY - stemHeight * 0.7f,
            cx + size.width * 0.15f, baseY - stemHeight * 0.8f,
        )
        quadraticBezierTo(
            cx + size.width * 0.05f, baseY - stemHeight * 0.65f,
            cx, baseY - stemHeight * 0.55f,
        )
    }
    drawPath(rightLeafPath, color = GardenSprout)
}

private fun DrawScope.drawLeaf() {
    val cx      = size.width / 2
    val baseY   = size.height * 0.78f
    val stemH   = size.height * 0.5f

    // Stem
    drawLine(
        color       = GardenLeaf,
        start       = Offset(cx, baseY),
        end         = Offset(cx, baseY - stemH),
        strokeWidth = 8f,
        cap         = StrokeCap.Round,
    )

    // Large left leaf
    val leftLeaf = Path().apply {
        moveTo(cx, baseY - stemH * 0.45f)
        quadraticBezierTo(
            cx - size.width * 0.35f, baseY - stemH * 0.6f,
            cx - size.width * 0.28f, baseY - stemH * 0.78f,
        )
        quadraticBezierTo(
            cx - size.width * 0.08f, baseY - stemH * 0.62f,
            cx, baseY - stemH * 0.45f,
        )
    }
    drawPath(leftLeaf, color = GardenLeaf)

    // Large right leaf
    val rightLeaf = Path().apply {
        moveTo(cx, baseY - stemH * 0.5f)
        quadraticBezierTo(
            cx + size.width * 0.35f, baseY - stemH * 0.65f,
            cx + size.width * 0.28f, baseY - stemH * 0.83f,
        )
        quadraticBezierTo(
            cx + size.width * 0.08f, baseY - stemH * 0.67f,
            cx, baseY - stemH * 0.5f,
        )
    }
    drawPath(rightLeaf, color = GardenLeaf)
}

private fun DrawScope.drawFlower() {
    val cx      = size.width / 2
    val baseY   = size.height * 0.8f
    val stemH   = size.height * 0.55f
    val flowerR = size.width * 0.12f

    // Stem
    drawLine(
        color       = GardenLeaf,
        start       = Offset(cx, baseY),
        end         = Offset(cx, baseY - stemH),
        strokeWidth = 8f,
        cap         = StrokeCap.Round,
    )

    // Leaves
    val leftLeaf = Path().apply {
        moveTo(cx, baseY - stemH * 0.4f)
        quadraticBezierTo(
            cx - size.width * 0.28f, baseY - stemH * 0.52f,
            cx - size.width * 0.22f, baseY - stemH * 0.65f,
        )
        quadraticBezierTo(
            cx - size.width * 0.06f, baseY - stemH * 0.53f,
            cx, baseY - stemH * 0.4f,
        )
    }
    drawPath(leftLeaf, color = GardenLeaf)

    // Flower petals (6 petals)
    val flowerCx = cx
    val flowerCy = baseY - stemH
    repeat(6) { i ->
        val angle = (i * 60.0) * PI / 180.0
        val petalCx = flowerCx + (flowerR * 1.6f * cos(angle)).toFloat()
        val petalCy = flowerCy + (flowerR * 1.6f * sin(angle)).toFloat()
        drawCircle(
            color  = GardenFlower.copy(alpha = 0.8f),
            radius = flowerR,
            center = Offset(petalCx, petalCy),
        )
    }

    // Flower center
    drawCircle(
        color  = Amber70,
        radius = flowerR * 0.7f,
        center = Offset(flowerCx, flowerCy),
    )
}

private fun DrawScope.drawTree() {
    val cx      = size.width / 2
    val baseY   = size.height * 0.85f
    val trunkH  = size.height * 0.35f
    val trunkW  = size.width * 0.08f

    // Trunk
    drawRoundRect(
        color   = GardenSeed,
        topLeft = Offset(cx - trunkW / 2, baseY - trunkH),
        size    = Size(trunkW, trunkH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
    )

    // Canopy (3 overlapping circles for a tree shape)
    val canopyColor = GardenTree
    drawCircle(
        color  = canopyColor.copy(alpha = 0.7f),
        radius = size.width * 0.28f,
        center = Offset(cx - size.width * 0.1f, baseY - trunkH - size.height * 0.1f),
    )
    drawCircle(
        color  = canopyColor.copy(alpha = 0.7f),
        radius = size.width * 0.28f,
        center = Offset(cx + size.width * 0.1f, baseY - trunkH - size.height * 0.1f),
    )
    drawCircle(
        color  = canopyColor,
        radius = size.width * 0.3f,
        center = Offset(cx, baseY - trunkH - size.height * 0.2f),
    )
}
