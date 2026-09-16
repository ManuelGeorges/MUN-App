package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.ui.UserRole
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.navigation.Screen
import com.mianu.paymentapp.ui.viewmodel.DashboardState

/**
 * Non-admin dashboard quick actions, scoped to what the [role] can actually do.
 *
 * The tiles must line up with the role's permissions and bottom-bar tabs — surfacing an action the
 * server will refuse (an organizer opening team management, say) is worse than omitting it. See
 * [AdminDashboardSections] for the admin/chief counterpart.
 */
@Composable
fun OrganizerDashboardSections(
    role: UserRole,
    state: DashboardState,
    onNavigate: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (role) {
            UserRole.ORGANIZER -> {
                SectionHeader(title = "On the floor", eyebrow = "Quick actions")
                QuickAction(
                    title = "Conference attendance",
                    subtitle = "Swipe badges at entrance to check in attendees",
                    icon = Icons.Default.HowToReg,
                    onClick = { onNavigate(Screen.AttendanceScan.route) },
                )
                QuickAction(
                    title = "Meal scanning",
                    subtitle = "Serve breakfast and lunch",
                    icon = Icons.Default.Restaurant,
                    onClick = { onNavigate(Screen.MealScan.route) },
                )
                QuickAction(
                    title = "Hall access",
                    subtitle = "Admit delegates and track occupancy",
                    icon = Icons.Default.MeetingRoom,
                    onClick = { onNavigate(Screen.AccessScan.route) },
                )
            }

            UserRole.TEAM_LEADER -> {
                SectionHeader(title = "Your team", eyebrow = "Quick actions")
                QuickAction(
                    title = "Broadcast",
                    subtitle = "Send an announcement to your team",
                    icon = Icons.Default.Campaign,
                    onClick = { onNavigate(Screen.Broadcast.route) },
                )
                QuickAction(
                    title = "Team roster",
                    subtitle = if (state.teams.isEmpty()) {
                        "See who is on your team"
                    } else {
                        "${state.teams.size} teams configured"
                    },
                    icon = Icons.Default.Groups,
                    onClick = { onNavigate(Screen.Teams.route) },
                )
                QuickAction(
                    title = "Hall availability",
                    subtitle = "Find a hall with room",
                    icon = Icons.Default.EventSeat,
                    onClick = { onNavigate(Screen.Availability.route) },
                )
            }

            // Team members (press) and any other limited role: room-finding only.
            else -> {
                SectionHeader(title = "Around the venue", eyebrow = "Quick actions")
                QuickAction(
                    title = "Hall availability",
                    subtitle = "See which halls have room",
                    icon = Icons.Default.EventSeat,
                    onClick = { onNavigate(Screen.Availability.route) },
                )
            }
        }
    }
}
