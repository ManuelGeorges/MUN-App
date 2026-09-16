package com.mianu.paymentapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism surfaces (`.glass`, `.glass-strong`, `.glass-sheen` in the web theme).
 *
 * Caveat worth knowing: the web version uses `backdrop-filter: blur()`, which samples whatever is
 * painted behind the element. Compose has no backdrop filter — `Modifier.blur` blurs an element's
 * own content, not what's underneath it. So these approximate the effect with a translucent
 * gradient fill plus a hairline light border, which is what sells "glass" visually anyway. Layer
 * them over [ambientMesh] and the illusion holds.
 */

/** Standard glass surface: translucent fill, hairline border. */
@Composable
fun Modifier.glass(shape: Shape = MianuShapes.Large): Modifier =
    glassInternal(shape = shape, strong = false)

/** Heavier glass: more opaque, brighter border. Use when content needs to stay legible. */
@Composable
fun Modifier.glassStrong(shape: Shape = MianuShapes.Large): Modifier =
    glassInternal(shape = shape, strong = true)

@Composable
private fun Modifier.glassInternal(shape: Shape, strong: Boolean): Modifier {
    val colors = MianuTheme.colors
    val dark = colors.isDark

    // Dark glass tints toward white (light passing through); light glass tints toward the raised
    // surface so it doesn't wash out against an already-pale background.
    val topAlpha = when {
        dark && strong -> 0.14f
        dark -> 0.08f
        strong -> 0.92f
        else -> 0.72f
    }
    val bottomAlpha = when {
        dark && strong -> 0.07f
        dark -> 0.03f
        strong -> 0.80f
        else -> 0.55f
    }
    val tint = if (dark) Color.White else colors.surfaceRaised

    val borderAlpha = when {
        dark && strong -> 0.22f
        dark -> 0.14f
        strong -> 0.90f
        else -> 0.65f
    }
    val borderColor = if (dark) Color.White.copy(alpha = borderAlpha) else colors.border.copy(alpha = borderAlpha)

    return this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                listOf(tint.copy(alpha = topAlpha), tint.copy(alpha = bottomAlpha)),
            ),
            shape = shape,
        )
        .border(width = 1.dp, color = borderColor, shape = shape)
}

/**
 * A specular highlight along the top edge, mimicking light catching a glass rim.
 *
 * Apply *after* [glass] so it paints over the fill. It fades out by ~35% of the element's height,
 * which keeps it reading as an edge highlight rather than a gradient background.
 */
@Composable
fun Modifier.glassSheen(shape: Shape = MianuShapes.Large): Modifier {
    val dark = MianuTheme.colors.isDark
    return this.background(
        brush = Brush.linearGradient(
            0.0f to Color.White.copy(alpha = if (dark) 0.16f else 0.85f),
            0.35f to Color.Transparent,
            start = Offset.Zero,
            end = Offset(0f, Float.POSITIVE_INFINITY),
        ),
        shape = shape,
    )
}
