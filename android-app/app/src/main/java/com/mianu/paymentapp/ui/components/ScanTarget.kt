package com.mianu.paymentapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.nfc.NfcAvailability
import com.mianu.paymentapp.nfc.nfcAvailability
import com.mianu.paymentapp.ui.theme.MianuEnter
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.floating
import com.mianu.paymentapp.ui.viewmodel.ScanOutcome

/**
 * The tap target and result readout shared by the meal and access scanners.
 *
 * State is conveyed three ways at once — color, icon and text — so the outcome survives glare, a
 * glanced look, and color-vision differences. That matters here: this is read at arm's length while
 * someone is holding up a queue.
 *
 * [idleHint] describes the READY case only; when the reader is off or absent this substitutes its
 * own hint, because "tap a badge" is actively misleading on a device that cannot scan one.
 */
@Composable
fun ScanTarget(
    outcome: ScanOutcome,
    modifier: Modifier = Modifier,
    idleHint: String = "Tap a delegate badge to the back of the device",
) {
    val colors = MianuTheme.colors
    val availability by nfcAvailability()
    val view = LocalView.current

    // We silenced the platform chime so it couldn't fire before the server ruled, which leaves the
    // verdict with no non-visual signal. This puts one back, on the right side of the decision.
    LaunchedEffect(outcome) {
        when (outcome) {
            is ScanOutcome.Granted -> view.performHapticFeedback(confirmConstant())
            is ScanOutcome.Denied -> view.performHapticFeedback(rejectConstant())
            else -> Unit
        }
    }

    val accentColor = when (outcome) {
        is ScanOutcome.Granted -> colors.success
        is ScanOutcome.Denied -> colors.danger
        ScanOutcome.Scanning -> colors.accent
        ScanOutcome.Idle -> colors.fgMuted
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Expanding rings, shown only while a scan is in flight.
            if (outcome is ScanOutcome.Scanning) {
                PulseRings(color = colors.accent)
            }

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .then(if (outcome is ScanOutcome.Idle) Modifier.floating() else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = outcome::class,
                    transitionSpec = { MianuEnter.ScaleIn togetherWith androidx.compose.animation.fadeOut(tween(120)) },
                    label = "scanIcon",
                ) { state ->
                    val icon = when (state) {
                        ScanOutcome.Granted::class -> Icons.Default.CheckCircle
                        ScanOutcome.Denied::class -> Icons.Default.ErrorOutline
                        else -> Icons.Default.Contactless
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(72.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        val headline = when (outcome) {
            is ScanOutcome.Granted -> outcome.headline
            is ScanOutcome.Denied -> outcome.headline
            ScanOutcome.Scanning -> "Verifying…"
            ScanOutcome.Idle -> "Ready to scan"
        }
        val detail = when (outcome) {
            is ScanOutcome.Granted -> outcome.detail
            is ScanOutcome.Denied -> outcome.detail
            ScanOutcome.Scanning -> "Hold the badge steady"
            ScanOutcome.Idle -> when (availability) {
                NfcAvailability.READY -> idleHint
                NfcAvailability.DISABLED ->
                    "NFC is switched off. Turn it on in system settings, or enter a card UID below."
                NfcAvailability.UNSUPPORTED ->
                    "This device has no NFC reader. Enter a card UID below to continue."
            }
        }

        Text(
            text = headline,
            style = MaterialTheme.typography.headlineSmall,
            color = if (outcome is ScanOutcome.Idle) colors.fg else accentColor,
            textAlign = TextAlign.Center,
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.fgMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp),
            )
        }
    }
}

/**
 * CONFIRM and REJECT carry distinct patterns a steward can tell apart without looking, but they only
 * exist from API 30. Below that both collapse to a long press, which is still better than silence.
 */
private fun confirmConstant(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
    else HapticFeedbackConstants.LONG_PRESS

private fun rejectConstant(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
    else HapticFeedbackConstants.LONG_PRESS

/** Two concentric rings expanding outward and fading, offset in phase. */
@Composable
private fun PulseRings(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulseRings")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "ringProgress",
    )

    Canvas(modifier = Modifier.size(230.dp)) {
        val maxRadius = size.minDimension / 2f
        // Second ring trails the first by half a cycle, so the pulse reads as continuous.
        listOf(progress, (progress + 0.5f) % 1f).forEach { p ->
            drawCircle(
                color = color.copy(alpha = (1f - p) * 0.35f),
                radius = maxRadius * (0.55f + p * 0.45f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
