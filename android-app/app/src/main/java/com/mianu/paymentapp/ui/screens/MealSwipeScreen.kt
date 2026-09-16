package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.models.MealType
import com.mianu.paymentapp.nfc.OnNfcScan
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.GlassPanel
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.MianuSecondaryButton
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.ScanTarget
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.SegmentedSelector
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.ScanOutcome
import com.mianu.paymentapp.ui.viewmodel.ScanViewModel

/**
 * Meal service scanner.
 *
 * Tapping a badge records the meal immediately — no confirm step, because the queue is the
 * constraint here. Manual UID entry stays as the fallback for a damaged badge or a device with no
 * reader.
 */
@Composable
fun MealSwipeScreen(viewModel: ScanViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors
    var cardUid by remember { mutableStateOf("") }

    // Taps are ignored while a swipe is in flight rather than queued: a steward who taps twice
    // impatiently should not record two meals against the same delegate.
    OnNfcScan(enabled = !state.isBusy) { uid ->
        cardUid = uid
        viewModel.swipeMeal(uid)
    }

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SectionHeader(
                title = "Meal service",
                eyebrow = "Scanning",
                subtitle = "Record a delegate's meal",
            )

            SegmentedSelector(
                options = listOf(MealType.BREAKFAST, MealType.LUNCH),
                selected = state.mealType,
                onSelect = viewModel::onMealTypeChange,
                label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            )

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                ScanTarget(
                    outcome = state.outcome,
                    idleHint = "Tap a delegate badge to record their meal",
                )

                Spacer(Modifier.height(24.dp))

                MianuTextField(
                    value = cardUid,
                    onValueChange = { cardUid = it },
                    label = "Card UID",
                    placeholder = "e.g. 04A2B3C4D5",
                    leadingIcon = Icons.Default.CreditCard,
                    enabled = !state.isBusy,
                )

                Spacer(Modifier.height(14.dp))

                MianuButton(
                    text = "Record meal",
                    onClick = { viewModel.swipeMeal(cardUid.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cardUid.isNotBlank(),
                    loading = state.isBusy,
                    icon = Icons.Default.Search,
                    glow = true,
                )

                if (state.outcome !is ScanOutcome.Idle && !state.isBusy) {
                    Spacer(Modifier.height(8.dp))
                    MianuSecondaryButton(
                        text = "Scan another",
                        onClick = {
                            cardUid = ""
                            viewModel.reset()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SectionHeader(title = "Recent scans", eyebrow = "This session")

            if (state.history.isEmpty()) {
                MianuCard {
                    EmptyState(
                        title = "Nothing scanned yet",
                        description = "Meals recorded on this device will appear here.",
                        icon = Icons.Default.CreditCard,
                    )
                }
            } else {
                state.history.forEach { entry ->
                    MianuCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.fg,
                                )
                                Text(
                                    text = entry.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.fgMuted,
                                )
                            }
                            StatusPill(
                                text = if (entry.granted) "Served" else "Denied",
                                color = if (entry.granted) colors.success else colors.danger,
                            )
                        }
                    }
                }
            }
        }
    }
}
