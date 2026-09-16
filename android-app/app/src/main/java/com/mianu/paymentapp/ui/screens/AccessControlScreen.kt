package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MeetingRoom
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
import com.mianu.paymentapp.data.models.AccessAction
import com.mianu.paymentapp.ui.components.CapacityMeter
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.GlassPanel
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.MianuSecondaryButton
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.ScanTarget
import com.mianu.paymentapp.nfc.OnNfcScan
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.SegmentedSelector
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.ScanOutcome
import com.mianu.paymentapp.ui.viewmodel.ScanState
import com.mianu.paymentapp.ui.viewmodel.ScanViewModel

/**
 * Hall access scanner — admits and releases delegates, tracking live occupancy.
 *
 * Shares [ScanViewModel] with the meal scanner: same interaction, same result readout, and a
 * steward may switch between the two on one device mid-event.
 */
@Composable
fun AccessControlScreen(viewModel: ScanViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var cardUid by remember { mutableStateOf("") }

    // Deaf to taps until a hall exists to scan against — otherwise a badge on the empty state would
    // come back "no hall selected", which reads as a problem with the badge rather than with setup.
    OnNfcScan(enabled = !state.isBusy && state.halls.isNotEmpty()) { uid ->
        cardUid = uid
        viewModel.scanAccess(uid)
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
                title = "Hall access",
                eyebrow = "Scanning",
                subtitle = "Admit or release delegates",
            )

            // if/else, never an early `return@Column`. Returning out of a composable lambda after
            // other composables have already emitted leaves Compose's group stack unbalanced,
            // which crashes recomposition with an IndexOutOfBounds inside Stack.pop — a stack
            // trace that points at the runtime rather than at this file.
            if (state.halls.isEmpty()) {
                MianuCard {
                    EmptyState(
                        title = "No halls configured",
                        description = "An admin needs to add a hall before access can be scanned.",
                        icon = Icons.Default.MeetingRoom,
                    )
                }
            } else {
                ScannerContent(
                    state = state,
                    cardUid = cardUid,
                    onCardUidChange = { cardUid = it },
                    onHallChange = viewModel::onHallChange,
                    onActionChange = viewModel::onActionChange,
                    onScan = { viewModel.scanAccess(cardUid.trim()) },
                    onReset = {
                        cardUid = ""
                        viewModel.reset()
                    },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ScannerContent(
    state: ScanState,
    cardUid: String,
    onCardUidChange: (String) -> Unit,
    onHallChange: (String) -> Unit,
    onActionChange: (AccessAction) -> Unit,
    onScan: () -> Unit,
    onReset: () -> Unit,
) {
    val colors = MianuTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Hall",
            style = MaterialTheme.typography.labelMedium,
            color = colors.fgMuted,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.halls.size) { index ->
                val hall = state.halls[index]
                FilterPill(
                    text = hall.name,
                    selected = hall.id == state.selectedHallId,
                    onClick = { onHallChange(hall.id) },
                )
            }
        }
    }

    state.selectedHall?.let { hall ->
        MianuCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${hall.currentOccupancy} of ${hall.capacityThreshold} inside",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.fg,
                )
                StatusPill(
                    text = when {
                        hall.isAtCapacity -> "At capacity"
                        hall.isNearCapacity -> "Filling up"
                        else -> "Space available"
                    },
                    color = when {
                        hall.isAtCapacity -> colors.danger
                        hall.isNearCapacity -> colors.warning
                        else -> colors.success
                    },
                )
            }
            Spacer(Modifier.height(10.dp))
            CapacityMeter(ratio = hall.occupancyRatio)
        }
    }

    SegmentedSelector(
        options = listOf(AccessAction.ENTRY, AccessAction.EXIT),
        selected = state.action,
        onSelect = onActionChange,
        label = { if (it == AccessAction.ENTRY) "Entry" else "Exit" },
    )

    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        ScanTarget(
            outcome = state.outcome,
            idleHint = "Tap a delegate badge at the door",
        )

        Spacer(Modifier.height(24.dp))

        MianuTextField(
            value = cardUid,
            onValueChange = onCardUidChange,
            label = "Card UID",
            placeholder = "e.g. 04A2B3C4D5",
            leadingIcon = Icons.Default.CreditCard,
            enabled = !state.isBusy,
        )

        Spacer(Modifier.height(14.dp))

        MianuButton(
            text = if (state.action == AccessAction.ENTRY) "Admit delegate" else "Release delegate",
            onClick = onScan,
            modifier = Modifier.fillMaxWidth(),
            enabled = cardUid.isNotBlank(),
            loading = state.isBusy,
            glow = true,
        )

        if (state.outcome !is ScanOutcome.Idle && !state.isBusy) {
            Spacer(Modifier.height(8.dp))
            MianuSecondaryButton(
                text = "Scan another",
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    SectionHeader(title = "Recent scans", eyebrow = "This session")

    if (state.history.isEmpty()) {
        MianuCard {
            EmptyState(
                title = "Nothing scanned yet",
                description = "Entries and exits recorded here will appear in this list.",
                icon = Icons.Default.MeetingRoom,
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
                        text = if (entry.granted) "Allowed" else "Denied",
                        color = if (entry.granted) colors.success else colors.danger,
                    )
                }
            }
        }
    }
}
