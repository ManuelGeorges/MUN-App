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
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.mianu.paymentapp.ui.components.StatCard
import com.mianu.paymentapp.ui.components.StatRow
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.ScanOutcome
import com.mianu.paymentapp.ui.viewmodel.ScanViewModel

enum class AttendanceActionOption(val actionKey: String, val label: String) {
    CHECK_IN("check_in", "Check In"),
    CHECK_OUT("check_out", "Check Out"),
    TOGGLE("toggle", "Toggle Presence"),
}

/**
 * Main entrance attendance terminal scanner.
 *
 * Tapping a badge records overall conference attendance (present / absent) rather than hall occupancy.
 */
@Composable
fun AttendanceScanScreen(viewModel: ScanViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors
    var cardUid by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf(AttendanceActionOption.CHECK_IN) }

    LaunchedEffect(Unit) {
        viewModel.loadAttendanceSummary()
    }

    OnNfcScan(enabled = !state.isBusy) { uid ->
        cardUid = uid
        viewModel.onAttendanceActionChange(selectedAction.actionKey)
        viewModel.scanAttendance(uid)
    }

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(
                    title = "Conference Attendance",
                    eyebrow = "Turnstile Scanner",
                    subtitle = "Swipe badges to register general attendance",
                )
                IconButton(onClick = { viewModel.loadAttendanceSummary() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh telemetry",
                        tint = colors.fg,
                    )
                }
            }

            // Attendance KPI Telemetry
            state.attendanceSummary?.let { summary ->
                StatRow {
                    StatCard(
                        title = "Attendees in Sum",
                        value = "${summary.totalPresent} / ${summary.totalRegistered}",
                        caption = "${summary.attendanceRate}% on-site attendance",
                        icon = Icons.Default.People,
                        accent = Color(0xFF34D399),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        title = "Main Swipes",
                        value = summary.totalSwipes.toString(),
                        caption = "Badge scans recorded",
                        icon = Icons.Default.HowToReg,
                        accent = Color(0xFF60A5FA),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            SegmentedSelector(
                options = AttendanceActionOption.values().toList(),
                selected = selectedAction,
                onSelect = {
                    selectedAction = it
                    viewModel.onAttendanceActionChange(it.actionKey)
                },
                label = { it.label },
            )

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                ScanTarget(
                    outcome = state.outcome,
                    idleHint = "Tap badge on device to register attendance",
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
                    text = "Register Attendance (${selectedAction.label})",
                    onClick = {
                        viewModel.onAttendanceActionChange(selectedAction.actionKey)
                        viewModel.scanAttendance(cardUid.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cardUid.isNotBlank(),
                    loading = state.isBusy,
                    icon = Icons.Default.HowToReg,
                    glow = true,
                )

                if (state.outcome !is ScanOutcome.Idle && !state.isBusy) {
                    Spacer(Modifier.height(8.dp))
                    MianuSecondaryButton(
                        text = "Scan another badge",
                        onClick = {
                            cardUid = ""
                            viewModel.reset()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SectionHeader(title = "Recent entrance scans", eyebrow = "This session")

            if (state.history.isEmpty()) {
                MianuCard {
                    EmptyState(
                        title = "No entrance swipes yet",
                        description = "Badge swipes recorded during this session will appear here.",
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
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = entry.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.fgMuted,
                                )
                            }
                            StatusPill(
                                text = if (entry.granted) "Recorded" else "Failed",
                                color = if (entry.granted) Color(0xFF34D399) else colors.danger,
                            )
                        }
                    }
                }
            }
        }
    }
}
