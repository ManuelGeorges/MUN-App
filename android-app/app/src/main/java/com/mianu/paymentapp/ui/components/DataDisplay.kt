package com.mianu.paymentapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.ui.theme.EyebrowStyle
import com.mianu.paymentapp.ui.theme.MetricStyle
import com.mianu.paymentapp.ui.theme.MianuShapes
import com.mianu.paymentapp.ui.theme.MianuTheme

/** Direction of a trend indicator. [Flat] renders no arrow. */
enum class Trend { Up, Down, Flat }

/**
 * Headline metric tile.
 *
 * Note the deliberate color choice: a trend arrow is tinted by whether the movement is *good*, not
 * by its direction. Rising meal counts are healthy; a rising denial rate is not, and callers say
 * which via [trendIsGood].
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    icon: ImageVector? = null,
    trend: Trend = Trend.Flat,
    trendIsGood: Boolean = true,
    accent: Color = MianuTheme.colors.accent,
) {
    val colors = MianuTheme.colors
    MianuCard(modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = title.uppercase(),
                style = EyebrowStyle,
                color = colors.fgMuted,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(MianuShapes.Small)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(text = value, style = MetricStyle, color = colors.fg, maxLines = 1)

        if (caption != null) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trend != Trend.Flat) {
                    val tint = if (trendIsGood) colors.success else colors.danger
                    Icon(
                        imageVector = if (trend == Trend.Up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                }
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.fgMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Capacity meter for halls and teams.
 *
 * The fill color escalates with load — accent, then warning past 80%, then danger at capacity — so
 * a steward can read the state from across a room without parsing the numbers.
 */
@Composable
fun CapacityMeter(
    ratio: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 8.dp,
) {
    val colors = MianuTheme.colors
    val target = when {
        ratio >= 1f -> colors.danger
        ratio >= 0.8f -> colors.warning
        else -> colors.accent
    }
    val animatedRatio by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "capacityRatio",
    )
    val animatedColor by animateColorAsState(target, tween(400), label = "capacityColor")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MianuShapes.Pill)
            .background(colors.surfaceSunken),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedRatio)
                .fillMaxHeight()
                .clip(MianuShapes.Pill)
                .background(animatedColor),
        )
    }
}

/** Label/value pair for detail panels. */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MianuTheme.colors.fg,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MianuTheme.colors.fgMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/** Circular icon badge used as a list-row leading element. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MianuTheme.colors.accent,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** Lays out stat cards two-up with consistent spacing. */
@Composable
fun StatRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** Vertical stack with the app's standard section spacing. */
@Composable
fun SectionColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
