package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Google Workspace Shimmer Modifier.
 * Animates a smooth linear gradient sweep between surfaceContainer and surfaceContainerHighest
 * for skeletal loading states.
 */
fun Modifier.workspaceShimmer(): Modifier = composed {
  val transition = rememberInfiniteTransition(label = "shimmer_transition")
  val translateAnim by transition.animateFloat(
    initialValue = -500f,
    targetValue = 1500f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "shimmer_translation",
  )

  val shimmerColors = listOf(
    MaterialTheme.colorScheme.surfaceContainer,
    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
    MaterialTheme.colorScheme.surfaceContainer,
  )

  val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset(x = translateAnim, y = 0f),
    end = Offset(x = translateAnim + 400f, y = 400f),
  )

  this.background(brush)
}
