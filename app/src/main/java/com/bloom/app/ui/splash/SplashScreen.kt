package com.bloom.app.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloom.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var logoVisible by remember { mutableStateOf(false) }
    var logoScaled by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var reflectVisible by remember { mutableStateOf(false) }
    var growVisible by remember { mutableStateOf(false) }
    var taglineBloomVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (logoScaled) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 600),
        label = "logoScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    val finalScale = logoScale * if (logoScaled) breathScale else 1f

    LaunchedEffect(Unit) {
        // Sequence matching the implementation plan
        delay(400)
        logoVisible = true
        titleVisible = true
        
        delay(500)
        logoScaled = true
        
        delay(400)
        reflectVisible = true
        
        delay(400)
        growVisible = true
        
        delay(400)
        taglineBloomVisible = true
        
        delay(1600)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(animationSpec = tween(400))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_bloom_splash),
                    contentDescription = "Bloom Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(finalScale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(animationSpec = tween(400))
            ) {
                Text(
                    text = "Bloom",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(
                    visible = reflectVisible,
                    enter = fadeIn(animationSpec = tween(400))
                ) {
                    Text(
                        text = "Reflect.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(
                    visible = growVisible,
                    enter = fadeIn(animationSpec = tween(400))
                ) {
                    Text(
                        text = "Grow.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(
                    visible = taglineBloomVisible,
                    enter = fadeIn(animationSpec = tween(400))
                ) {
                    Text(
                        text = "Bloom.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
