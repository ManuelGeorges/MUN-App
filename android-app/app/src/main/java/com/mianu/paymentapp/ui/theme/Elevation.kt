package com.mianu.paymentapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The shadow scale from theme-details.md. The web theme layers multiple box-shadows per level;
 * Compose gives us a single elevation value, so each level is tuned to read at roughly the same
 * depth rather than matching the CSS literally.
 *
 * Shadows are also tinted: on dark surfaces an untinted black shadow disappears, so the dark
 * variants lean on a deeper spot color to stay visible.
 */
object MianuElevation {
    val Card: Dp = 2.dp
    val Raised: Dp = 8.dp
    val Overlay: Dp = 20.dp
    val Glass: Dp = 12.dp
}

/** Corner radii used across the app, so cards and sheets agree on their curvature. */
object MianuShapes {
    val Small = RoundedCornerShape(10.dp)
    val Medium = RoundedCornerShape(14.dp)
    val Large = RoundedCornerShape(20.dp)
    val XLarge = RoundedCornerShape(28.dp)
    val Pill = RoundedCornerShape(999.dp)
}

/** Subtle shadow for standard cards. */
fun Modifier.cardShadow(shape: Shape = MianuShapes.Medium): Modifier =
    mianuShadow(MianuElevation.Card, shape)

/** Deeper shadow for elevated components. */
fun Modifier.raisedShadow(shape: Shape = MianuShapes.Medium): Modifier =
    mianuShadow(MianuElevation.Raised, shape)

/** Deepest shadow, for modals and popovers. */
fun Modifier.overlayShadow(shape: Shape = MianuShapes.Large): Modifier =
    mianuShadow(MianuElevation.Overlay, shape)

private fun Modifier.mianuShadow(elevation: Dp, shape: Shape): Modifier = composed {
    val dark = MianuTheme.colors.isDark
    shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (dark) Color.Black else MianuNeutral.N900.copy(alpha = 0.6f),
        spotColor = if (dark) Color.Black else MianuNeutral.N900.copy(alpha = 0.8f),
    )
}

/**
 * The accent-colored glow used for active states and focus rings.
 *
 * Compose can't render an outer colored glow directly, so this draws the shadow with the accent as
 * its spot color — the result reads as a halo on both themes.
 */
@Composable
fun Modifier.accentGlow(
    shape: Shape = MianuShapes.Medium,
    color: Color = MianuTheme.colors.accent,
    radius: Dp = 16.dp,
): Modifier = shadow(
    elevation = radius,
    shape = shape,
    ambientColor = color,
    spotColor = color,
)
