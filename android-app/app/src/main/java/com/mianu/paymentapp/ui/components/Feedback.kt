package com.mianu.paymentapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.data.ApiError
import com.mianu.paymentapp.data.isRetryable
import com.mianu.paymentapp.ui.theme.MianuShapes
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.pulseSoft
import com.mianu.paymentapp.ui.theme.shimmer
import com.mianu.paymentapp.ui.viewmodel.UiMessage

/**
 * Error, empty and loading states.
 *
 * These exist so failures are visible rather than silent — the previous screens caught exceptions
 * and rendered nothing, which left an operator staring at a blank panel with no idea whether the
 * server was down or the list was genuinely empty.
 */

/** Full-panel error with a retry affordance when the failure is worth retrying. */
@Composable
fun ErrorState(
    error: ApiError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val colors = MianuTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.danger.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = error.message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.fgMuted,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null && error.isRetryable) {
            TextButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Try again")
            }
        }
    }
}

/** Empty state with an optional call to action. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Default.Info,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = MianuTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.surfaceSunken),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.fgMuted,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.fg,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.fgMuted,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Spacer(Modifier.height(4.dp))
            action()
        }
    }
}

/** Skeleton list placeholder shown while a request is in flight. */
@Composable
fun LoadingList(
    modifier: Modifier = Modifier,
    rows: Int = 4,
    rowHeight: androidx.compose.ui.unit.Dp = 68.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(rows) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .shimmer(MianuShapes.Medium),
            )
        }
    }
}

/** Inline banner for one-shot messages. Tone selects both color and icon. */
@Composable
fun MessageBanner(
    message: UiMessage,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = MianuTheme.colors
    val (tint, icon) = when (message.tone) {
        UiMessage.Tone.Success -> colors.success to Icons.Default.CheckCircle
        UiMessage.Tone.Error -> colors.danger to Icons.Default.ErrorOutline
        UiMessage.Tone.Warning -> colors.warning to Icons.Default.WarningAmber
        UiMessage.Tone.Neutral -> colors.info to Icons.Default.Info
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MianuShapes.Medium)
            .background(tint.copy(alpha = 0.10f), MianuShapes.Medium)
            .border(1.dp, tint.copy(alpha = 0.28f), MianuShapes.Medium)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.fg,
            modifier = Modifier.weight(1f),
        )
        if (onDismiss != null) {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", style = MaterialTheme.typography.labelMedium, color = tint)
            }
        }
    }
}

/** Pulsing dot for live/connected indicators. */
@Composable
fun LiveDot(
    modifier: Modifier = Modifier,
    color: Color = MianuTheme.colors.success,
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .pulseSoft()
            .clip(CircleShape)
            .background(color),
    )
}

/** Centers content in the remaining space — used for full-screen loading and empty states. */
@Composable
fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) { content() }
}
