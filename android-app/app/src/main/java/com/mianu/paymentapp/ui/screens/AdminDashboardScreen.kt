package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.navigation.Screen
import com.mianu.paymentapp.ui.viewmodel.DashboardState

import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Restaurant

/**
 * The admin half of the dashboard — back-office entry points.
 *
 * Rendered by [DashboardScreen] rather than routed to directly, so both roles share one landing
 * route and one set of live metrics.
 */
@Composable
fun AdminDashboardSections(
    state: DashboardState,
    onNavigate: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Manage", eyebrow = "Administration")

        QuickAction(
            title = "Attendance Scanner",
            subtitle = "Swipe badges to record general attendance",
            icon = Icons.Default.HowToReg,
            onClick = { onNavigate(Screen.AttendanceScan.route) },
        )
        QuickAction(
            title = "Delegates & Meals",
            subtitle = "Register, adjust meal allowances, link cards",
            icon = Icons.Default.People,
            onClick = { onNavigate(Screen.Users.route) },
        )
        QuickAction(
            title = "Teams & Rosters",
            subtitle = "Manage teams, assign & remove members",
            icon = Icons.Default.Group,
            onClick = { onNavigate(Screen.Teams.route) },
        )
        QuickAction(
            title = "Meal Scanner",
            subtitle = "Process breakfast & lunch scans live",
            icon = Icons.Default.Restaurant,
            onClick = { onNavigate(Screen.MealScan.route) },
        )
        QuickAction(
            title = "Live Locations",
            subtitle = "Track delegates across conference halls",
            icon = Icons.Default.Place,
            onClick = { onNavigate(Screen.Locations.route) },
        )
        QuickAction(
            title = "Halls",
            subtitle = if (state.hallsNeedingAttention.isEmpty()) {
                "Capacity thresholds and live presence"
            } else {
                "${state.hallsNeedingAttention.size} need attention"
            },
            icon = Icons.Default.MeetingRoom,
            onClick = { onNavigate(Screen.Halls.route) },
        )
        QuickAction(
            title = "Reports & analytics",
            subtitle = "Daily totals, trends and exports",
            icon = Icons.Default.Assessment,
            onClick = { onNavigate(Screen.Reports.route) },
        )
    }
}
