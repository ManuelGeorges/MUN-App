package com.mianu.paymentapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.ui.theme.EyebrowStyle
import com.mianu.paymentapp.ui.theme.MianuShapes
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.cardShadow
import com.mianu.paymentapp.ui.theme.glass
import com.mianu.paymentapp.ui.theme.glassSheen
import com.mianu.paymentapp.ui.theme.glassStrong
import com.mianu.paymentapp.ui.theme.raisedShadow

/**
 * The surface primitives every screen builds on. Three levels, matching the shadow scale:
 * [MianuCard] for standard content, [GlassCard] for panels floating over the ambient mesh, and
 * [GlassPanel] for hero elements.
 */

@Composable
fun MianuCard(
    modifier: Modifier = Modifier,
    shape: Shape = MianuShapes.Medium,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MianuTheme.colors
    Column(
        modifier = modifier
            .cardShadow(shape)
            .clip(shape)
            .background(Color(0xFF0E1116), shape)
            .border(1.dp, colors.border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Translucent panel with a specular top edge. Needs an ambient backdrop to read correctly. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MianuShapes.Large,
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .raisedShadow(shape)
            .then(if (strong) Modifier.glassStrong(shape) else Modifier.glass(shape))
            .glassSheen(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Hero glass surface — heavier blur substitute and deeper shadow. One per screen at most. */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MianuShapes.XLarge,
    content: @Composable ColumnScope.() -> Unit,
) = GlassCard(
    modifier = modifier,
    shape = shape,
    strong = true,
    contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
    content = content,
)

/** Section heading with bold uppercase title matching the reference image. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    val colors = MianuTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = EyebrowStyle,
                    color = colors.fgMuted,
                )
                Spacer(Modifier.height(3.dp))
            }
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = colors.fg,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.fgMuted,
                )
            }
        }
        if (trailing != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
    }
}

/** Small status pill — role labels, states, counts. */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    icon: ImageVector? = null,
) {
    val colors = MianuTheme.colors
    val isCommittee = color == colors.committeeAccent
    val bg = if (isCommittee) colors.committeeBg else if (color == Color.White) Color(0xFF181C26) else color.copy(alpha = 0.14f)
    val border = if (isCommittee) colors.committeeBorder else if (color == Color.White) Color(0xFF2E3545) else color.copy(alpha = 0.30f)
    val textColor = if (color == Color.White) Color.White else color

    Row(
        modifier = modifier
            .clip(MianuShapes.Pill)
            .background(bg, MianuShapes.Pill)
            .border(1.dp, border, MianuShapes.Pill)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
        }
        Text(
            text = text.uppercase(),
            style = com.mianu.paymentapp.ui.theme.TechBadgeStyle,
            color = textColor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Dual-tag Committee Badge (e.g. [CSH] [COMITÉ EN BINÔMES]) from the reference image. */
@Composable
fun CommitteeBadge(
    code: String,
    type: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(MianuShapes.Pill)
                .background(Color(0xFF14171E), MianuShapes.Pill)
                .border(1.dp, Color(0xFF2A2F3D), MianuShapes.Pill)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = code.uppercase(),
                style = com.mianu.paymentapp.ui.theme.TechBadgeStyle,
                color = Color.White,
            )
        }
        if (type != null) {
            Box(
                modifier = Modifier
                    .clip(MianuShapes.Pill)
                    .background(Color(0xFF0F1E36), MianuShapes.Pill)
                .border(1.dp, Color(0xFF1E3A5F), MianuShapes.Pill)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = type.uppercase(),
                    style = com.mianu.paymentapp.ui.theme.TechBadgeStyle,
                    color = Color(0xFF5B96F7),
                )
            }
        }
    }
}

/** Circular monogram used in user rows and the app bar. */
@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    color: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF151821))
            .border(1.dp, Color(0xFF2C3242), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

/** Hairline divider tuned to the border token. */
@Composable
fun MianuDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MianuTheme.colors.border),
    )
}

/** Outline used by inputs and secondary buttons. */
@Composable
fun mianuBorder(): BorderStroke = BorderStroke(1.dp, MianuTheme.colors.border)
