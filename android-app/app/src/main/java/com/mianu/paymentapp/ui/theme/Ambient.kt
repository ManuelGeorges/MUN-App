package com.mianu.paymentapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/**
 * Normalized constellation points for the conference backdrop seen in the reference image.
 */
private val ConstellationNodes = listOf(
    0.10f to 0.08f, 0.22f to 0.05f, 0.36f to 0.11f, 0.16f to 0.18f,
    0.48f to 0.14f, 0.64f to 0.09f, 0.80f to 0.07f, 0.90f to 0.15f,
    0.75f to 0.22f, 0.58f to 0.23f, 0.44f to 0.28f, 0.28f to 0.26f,
    0.14f to 0.36f, 0.30f to 0.40f, 0.50f to 0.36f, 0.68f to 0.38f,
    0.86f to 0.33f, 0.92f to 0.46f, 0.78f to 0.50f, 0.60f to 0.52f,
    0.44f to 0.58f, 0.22f to 0.54f, 0.08f to 0.63f, 0.32f to 0.68f,
    0.50f to 0.70f, 0.72f to 0.66f, 0.88f to 0.70f, 0.82f to 0.84f,
    0.62f to 0.86f, 0.42f to 0.83f, 0.20f to 0.86f, 0.50f to 0.93f,
)

/**
 * Constellation Particle Network Ambient Background:
 * Deep pure pitch black (#000000) with luminous connected starry nodes and soft cluster glow,
 * exactly matching the backdrop of MIANU-SM in the reference image.
 */
@Composable
fun Modifier.ambientMesh(): Modifier {
    val colors = MianuTheme.colors

    return this
        .background(colors.surface)
        .drawBehind {
            val w = size.width
            val h = size.height

            // Pure black base
            drawRect(Color(0xFF000000))

            // Soft radiant cluster glow behind the upper-center area
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x0AFFFFFF), Color.Transparent),
                    center = Offset(w * 0.50f, h * 0.22f),
                    radius = w * 0.65f,
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.50f, h * 0.22f),
            )

            // Convert normalized coordinates to absolute pixels
            val points = ConstellationNodes.map { (rx, ry) -> Offset(rx * w, ry * h) }

            // Draw connecting constellation lines
            val maxDistance = w * 0.22f
            val maxDistSq = maxDistance * maxDistance

            for (i in points.indices) {
                for (j in (i + 1) until points.size) {
                    val p1 = points[i]
                    val p2 = points[j]
                    val dx = p1.x - p2.x
                    val dy = p1.y - p2.y
                    val distSq = dx * dx + dy * dy

                    if (distSq < maxDistSq) {
                        val distance = sqrt(distSq)
                        val alpha = ((1.0f - (distance / maxDistance)) * 0.25f).coerceIn(0.04f, 0.28f)
                        drawLine(
                            color = Color(0xFFFFFFFF).copy(alpha = alpha),
                            start = p1,
                            end = p2,
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                }
            }

            // Draw constellation star nodes with delicate glowing auras
            points.forEachIndexed { index, p ->
                val isKeyNode = index % 3 == 0
                if (isKeyNode) {
                    // Radiant aura for primary stars
                    drawCircle(
                        color = Color(0x2EFFFFFF),
                        radius = 4.5.dp.toPx(),
                        center = p,
                    )
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 2.5.dp.toPx(),
                        center = p,
                    )
                } else {
                    drawCircle(
                        color = Color(0xB3FFFFFF),
                        radius = 1.5.dp.toPx(),
                        center = p,
                    )
                }
            }
        }
}

/** Full-bleed constellation backdrop. */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().ambientMesh(), content = content)
}

/** The brand gradient — high-contrast dark carbon and silver monochrome. */
@Composable
fun mianuBrandGradient(): List<Color> {
    return listOf(
        Color(0xFF2A2E39),
        Color(0xFF14171F),
        Color(0xFF000000),
    )
}
