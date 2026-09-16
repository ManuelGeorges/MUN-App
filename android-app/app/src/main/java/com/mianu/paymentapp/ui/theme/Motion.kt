package com.mianu.paymentapp.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * The motion vocabulary from theme-details.md.
 *
 * Two families:
 *  - Mount transitions ([MianuEnter]) — one-shot, used with [AnimatedVisibility] or [MountAnimation].
 *  - Continuous effects — [shimmer], [pulseSoft], [floating], [glowPulse], [sheen], [flashDanger] —
 *    which are Modifiers driven by an infinite transition.
 */

/** Easing curve matching the web theme's default `ease-out` feel. */
val MianuEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

object MianuDuration {
    const val Fast = 180
    const val Normal = 320
    const val Slow = 520
    const val Shimmer = 1400
    const val Pulse = 2200
    const val Float = 3600
    const val Gradient = 8000
    const val Sheen = 2600
}

/** One-shot mount transitions. */
object MianuEnter {
    val FadeIn: EnterTransition = fadeIn(tween(MianuDuration.Normal, easing = FastOutSlowInEasing))

    val FadeInUp: EnterTransition = fadeIn(tween(MianuDuration.Normal, easing = MianuEasing)) +
        slideInVertically(tween(MianuDuration.Normal, easing = MianuEasing)) { it / 6 }

    val ScaleIn: EnterTransition = fadeIn(tween(MianuDuration.Normal, easing = MianuEasing)) +
        scaleIn(tween(MianuDuration.Normal, easing = MianuEasing), initialScale = 0.94f)

    val SlideInRight: EnterTransition = fadeIn(tween(MianuDuration.Normal, easing = MianuEasing)) +
        slideInHorizontally(tween(MianuDuration.Normal, easing = MianuEasing)) { it / 3 }

    val ExpandIn: EnterTransition = fadeIn(tween(MianuDuration.Fast)) +
        expandVertically(tween(MianuDuration.Normal, easing = MianuEasing))
}

/**
 * Plays [enter] once when this enters composition.
 *
 * [delayMillis] lets a list of siblings stagger — pass `index * 60` and the group cascades in
 * rather than popping as one block.
 */
@Composable
fun MountAnimation(
    modifier: Modifier = Modifier,
    enter: EnterTransition = MianuEnter.FadeInUp,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(visible = visible, enter = enter, modifier = modifier) {
        content()
    }
}

/**
 * Loading skeleton effect: a highlight band sweeping left to right.
 *
 * Apply to a sized Box — it paints its own background, so the Box doesn't need one.
 */
@Composable
fun Modifier.shimmer(shape: Shape = MianuShapes.Small): Modifier {
    val colors = MianuTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(MianuDuration.Shimmer, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    val base = colors.surfaceSunken
    val highlight = if (colors.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.75f)

    // Sweep the gradient across roughly 2x the element width so the band fully clears each cycle.
    return this
        .clip(shape)
        .background(base, shape)
        .drawWithContent {
            drawContent()
            val width = size.width
            val start = (progress * 2f - 0.5f) * width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(start, 0f),
                    end = Offset(start + width * 0.55f, size.height),
                ),
            )
        }
}

/** Gentle opacity pulsing — for "live"/"syncing" indicators. */
@Composable
fun Modifier.pulseSoft(minAlpha: Float = 0.45f, maxAlpha: Float = 1f): Modifier {
    val transition = rememberInfiniteTransition(label = "pulseSoft")
    val alpha by transition.animateFloat(
        initialValue = maxAlpha,
        targetValue = minAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(MianuDuration.Pulse, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    return this.alpha(alpha)
}

/** Slow vertical drift, for badges and hero icons. */
@Composable
fun Modifier.floating(distance: Float = 6f): Modifier {
    val transition = rememberInfiniteTransition(label = "float")
    val offset by transition.animateFloat(
        initialValue = -distance,
        targetValue = distance,
        animationSpec = infiniteRepeatable(
            animation = tween(MianuDuration.Float, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatOffset",
    )
    return this.graphicsLayer { translationY = offset }
}

/** Pulsing accent halo, for active scanners and focus states. */
@Composable
fun Modifier.glowPulse(
    shape: Shape = MianuShapes.Pill,
    color: Color = MianuTheme.colors.accent,
): Modifier {
    val transition = rememberInfiniteTransition(label = "glowPulse")
    val radius by transition.animateFloat(
        initialValue = 8f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(MianuDuration.Pulse, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowRadius",
    )
    return this.accentGlow(shape = shape, color = color, radius = radius.dp)
}

/**
 * Sweeping specular reflection across a glass surface.
 *
 * Unlike [glassSheen] (a static edge highlight) this one travels, so use it sparingly — one hero
 * element per screen, not every card.
 */
@Composable
fun Modifier.sheen(shape: Shape = MianuShapes.Large): Modifier {
    val dark = MianuTheme.colors.isDark
    val transition = rememberInfiniteTransition(label = "sheen")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(MianuDuration.Sheen, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sheenProgress",
    )
    val highlight = Color.White.copy(alpha = if (dark) 0.10f else 0.55f)

    return this
        .clip(shape)
        .drawWithContent {
            drawContent()
            val width = size.width
            val start = (progress * 1.8f - 0.4f) * width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(start, 0f),
                    end = Offset(start + width * 0.4f, size.height),
                ),
            )
        }
}

/** Glowing red flash for critical warnings — denied scans, insufficient balance. */
@Composable
fun Modifier.flashDanger(active: Boolean, shape: Shape = MianuShapes.Large): Modifier {
    if (!active) return this
    val danger = MianuTheme.colors.danger
    val transition = rememberInfiniteTransition(label = "flashDanger")
    val alpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flashAlpha",
    )
    return this
        .clip(shape)
        .background(danger.copy(alpha = alpha), shape)
}

/**
 * Slowly drifting gradient, for hero panels and the login backdrop.
 *
 * Animates the gradient's endpoints rather than its colors, which keeps the motion readable without
 * the hue shifting under text.
 */
@Composable
fun Modifier.gradientShift(
    colors: List<Color>,
    shape: Shape = MianuShapes.Large,
): Modifier {
    val transition = rememberInfiniteTransition(label = "gradientShift")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(MianuDuration.Gradient, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gradientOffset",
    )
    return this
        .clip(shape)
        .drawWithContent {
            val span = size.width.coerceAtLeast(size.height)
            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(-span * 0.5f + shift * span, 0f),
                    end = Offset(span + shift * span, size.height),
                ),
            )
            drawContent()
        }
}

/** Convenience: a Box rendering a shimmering placeholder block. */
@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = MianuShapes.Small,
) {
    Box(modifier = modifier.shimmer(shape))
}
