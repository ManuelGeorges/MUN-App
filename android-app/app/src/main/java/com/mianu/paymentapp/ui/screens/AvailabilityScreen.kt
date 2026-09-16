package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.models.HallAvailability
import com.mianu.paymentapp.data.models.HallState
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.LoadingList
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.AvailabilityViewModel

/**
 * Where there is room to work right now.
 *
 * Built for team members — press especially. Carries no occupant identities: the endpoint returns
 * only counts, so this screen can't surface who is where.
 */
@Composable
fun AvailabilityScreen(viewModel: AvailabilityViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(
                title = "Hall availability",
                eyebrow = "Live",
                subtitle = state.summary,
            )

            when {
                state.isLoading -> LoadingList(rows = 4)
                state.halls.isEmpty() -> MianuCard {
                    EmptyState(
                        title = "No halls yet",
                        description = "Availability appears once an organizer adds halls.",
                        icon = Icons.Default.EventSeat,
                    )
                }
                else -> state.halls.forEach { AvailabilityCard(it) }
            }
        }
    }
}

@Composable
private fun AvailabilityCard(hall: HallAvailability) {
    val colors = MianuTheme.colors
    val tone = when (hall.state) {
        HallState.EMPTY, HallState.AVAILABLE -> colors.success
        HallState.FILLING -> colors.warning
        HallState.FULL -> colors.danger
    }

    MianuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hall.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.fg,
                    )
                    Text(
                        text = "${hall.seatsFree} seat${if (hall.seatsFree == 1) "" else "s"} free · " +
                            "${hall.occupancy} of ${hall.capacity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.fgMuted,
                    )
                }
                StatusPill(text = stateLabel(hall.state), color = tone)
            }

            Spacer(Modifier.height(12.dp))

            // Occupancy meter. Full width, tinted by state, so the read is instant at a glance.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.surfaceSunken),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hall.occupancyRatio)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(tone),
                )
            }
        }
    }
}

private fun stateLabel(state: HallState): String = when (state) {
    HallState.EMPTY -> "Empty"
    HallState.AVAILABLE -> "Available"
    HallState.FILLING -> "Filling up"
    HallState.FULL -> "Full"
}
