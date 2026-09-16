package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.ui.UserRole
import com.mianu.paymentapp.ui.components.CapacityMeter
import com.mianu.paymentapp.ui.components.ErrorState
import com.mianu.paymentapp.ui.components.LoadingList
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatCard
import com.mianu.paymentapp.ui.components.StatRow
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.navigation.Screen
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuEnter
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.theme.MountAnimation
import com.mianu.paymentapp.ui.viewmodel.DashboardViewModel
import com.mianu.paymentapp.ui.viewmodel.UiState

/**
 * The landing screen for both roles.
 *
 * The header and live metrics are shared; the panels below differ by role and live in
 * [AdminDashboardSections] / [OrganizerDashboardSections].
 */
@Composable
fun DashboardScreen(
    role: UserRole,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session by SessionManager.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    AmbientBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                MountAnimation(enter = MianuEnter.FadeInUp) {
                    Column {
                        Text(
                            text = greeting(session.label),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.fg,
                        )
                        Text(
                            text = if (role == UserRole.ADMIN) {
                                "Conference command centre"
                            } else {
                                "Floor operations"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.fgMuted,
                        )
                    }
                }
            }

            item {
                when (val overview = state.overview) {
                    is UiState.Loading -> LoadingList(rows = 2, rowHeight = 104.dp)
                    is UiState.Error -> MianuCard {
                        ErrorState(error = overview.error, onRetry = viewModel::refresh)
                    }
                    is UiState.Success -> {
                        val data = overview.data
                        MountAnimation(enter = MianuEnter.FadeInUp, delayMillis = 80) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatRow {
                                    StatCard(
                                        title = "Meals today",
                                        value = data.totalMealsToday.toString(),
                                        caption = "Served today",
                                        icon = Icons.Default.Restaurant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatCard(
                                        title = "Door scans",
                                        value = data.totalAccessScansToday.toString(),
                                        caption = "Access logs today",
                                        icon = Icons.Default.Contactless,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                StatRow {
                                    StatCard(
                                        title = "Active delegates",
                                        value = data.activeUsers.toString(),
                                        caption = "Currently enabled",
                                        icon = Icons.Default.People,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (role == UserRole.ADMIN || role == UserRole.CHIEF_ORGANIZER) {
                                        StatCard(
                                            title = "Inside halls",
                                            value = data.peopleInsideHalls.toString(),
                                            caption = "Live on-site count",
                                            icon = Icons.Default.Place,
                                            accent = Color(0xFF34D399),
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Halls at or near capacity are shown to admins managing venue logistics.
            if ((role == UserRole.ADMIN || role == UserRole.CHIEF_ORGANIZER) && state.hallsNeedingAttention.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Needs attention",
                        eyebrow = "Capacity",
                        subtitle = "Halls approaching or at their threshold",
                    )
                }
                items(state.hallsNeedingAttention, key = { it.id }) { hall ->
                    MianuCard(onClick = { onNavigate(Screen.Halls.route) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionHeader(
                                title = hall.name,
                                trailing = {
                                    StatusPill(
                                        text = if (hall.isAtCapacity) "At capacity" else "Filling up",
                                        color = if (hall.isAtCapacity) colors.danger else colors.warning,
                                    )
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                            CapacityMeter(ratio = hall.occupancyRatio)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${hall.currentOccupancy} of ${hall.capacityThreshold} inside",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.fgMuted,
                            )
                        }
                    }
                }
            }

            item {
                when (role) {
                    // Chief organizers run the back office alongside admins.
                    UserRole.ADMIN, UserRole.CHIEF_ORGANIZER ->
                        AdminDashboardSections(state = state, onNavigate = onNavigate)
                    // Everyone else gets floor actions scoped to what their role can actually do —
                    // an organizer sees scanning and wallet, a leader sees broadcast and their team.
                    else -> OrganizerDashboardSections(
                        role = role,
                        state = state,
                        onNavigate = onNavigate,
                    )
                }
            }

            if ((role == UserRole.ADMIN || role == UserRole.CHIEF_ORGANIZER) && state.liveScans.isNotEmpty()) {
                item {
                    MianuCard(onClick = { onNavigate(Screen.Locations.route) }) {
                        SectionHeader(
                            title = "Live Operations Stream",
                            eyebrow = "Real-time Access & Meals",
                            subtitle = "Latest badge interactions across venue",
                            trailing = {
                                StatusPill(text = "LIVE", color = Color(0xFF34D399))
                            },
                        )
                        Spacer(Modifier.height(12.dp))
                        state.liveScans.take(4).forEach { scan ->
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scan.delegateName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.fg,
                                    )
                                    val isMeal = scan.scanType == "meal_swipe"
                                    val detail = if (isMeal && scan.mealsRemaining != null) {
                                        "${scan.locationOrService} • 🍴 ${scan.mealsRemaining} left"
                                    } else {
                                        scan.locationOrService
                                    }
                                    Text(
                                        text = detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isMeal) colors.accent else colors.fgMuted,
                                    )
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    StatusPill(
                                        text = if (scan.allowed) "AUTHORIZED" else "DENIED",
                                        color = if (scan.allowed) Color(0xFF34D399) else colors.danger,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = scan.timestamp.take(16).replace('T', ' '),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.fgMuted,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                MianuCard {
                    SectionHeader(title = "Teams", eyebrow = "Coordination")
                    Spacer(Modifier.height(10.dp))
                    if (state.teams.isEmpty()) {
                        Text(
                            text = "No teams configured yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.fgMuted,
                        )
                    } else {
                        state.teams.take(4).forEach { team ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                SectionHeader(
                                    title = team.name,
                                    trailing = {
                                        Text(
                                            text = "${team.currentSize}/${team.capacity}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (team.isFull) colors.warning else colors.fgMuted,
                                        )
                                    },
                                )
                                Spacer(Modifier.height(6.dp))
                                CapacityMeter(ratio = team.occupancyRatio, height = 6.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Navigable row used by both roles' quick-action lists. */
@Composable
fun QuickAction(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val colors = MianuTheme.colors
    MianuCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            com.mianu.paymentapp.ui.components.IconBadge(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.fg)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.fgMuted)
            }
        }
    }
}

private fun greeting(name: String): String {
    val hour = java.time.LocalTime.now().hour
    val part = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    return if (name.isBlank() || name == "Guest") part else "$part, $name"
}
