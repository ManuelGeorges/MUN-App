package com.mianu.paymentapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
import com.mianu.paymentapp.ui.UserRole

/**
 * Every destination in the app.
 *
 * Routes are stable strings; [icon] is only set for screens that can appear in the bottom bar.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
) {
    data object Login : Screen("login", "Sign in")

    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.SpaceDashboard)
    data object AttendanceScan : Screen("attendance_scan", "Attendance", Icons.Default.HowToReg)
    data object MealScan : Screen("meal_scan", "Meals", Icons.Default.Restaurant)
    data object AccessScan : Screen("access_scan", "Access", Icons.Default.Contactless)
    data object Locations : Screen("locations", "Locations", Icons.Default.Place)
    data object Wallet : Screen("wallet", "Wallet", Icons.Default.Wallet)
    data object Users : Screen("users", "Delegates", Icons.Default.People)
    data object Teams : Screen("teams", "Teams", Icons.Default.Groups)
    data object Halls : Screen("halls", "Halls", Icons.Default.MeetingRoom)
    data object Availability : Screen("availability", "Availability", Icons.Default.EventSeat)
    data object Broadcast : Screen("broadcast", "Broadcast", Icons.Default.Campaign)
    data object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
    data object Activity : Screen("activity", "Activity", Icons.Default.Campaign)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

/**
 * Bottom-bar destinations per role.
 *
 * Capped at five — Material's navigation bar degrades badly past that. Each bar carries only what
 * that role actually does; anything else is reached from within a section.
 *
 *  - **Admin / chief organizer** work the back office: delegates, locations, broadcast, reports.
 *  - **Organizer** works the floor: scanning terminals only (attendance, meals & access doors).
 *  - **Team leader** coordinates their team: dashboard, broadcast, team.
 *  - **Team member** (press) only needs to find space and read messages.
 */
fun bottomNavFor(role: UserRole): List<Screen> = when (role) {
    UserRole.ADMIN, UserRole.CHIEF_ORGANIZER -> listOf(
        Screen.Dashboard,
        Screen.Users,
        Screen.Locations,
        Screen.Broadcast,
        Screen.Settings,
    )
    UserRole.ORGANIZER -> listOf(
        Screen.AttendanceScan,
        Screen.MealScan,
        Screen.AccessScan,
        Screen.Settings,
    )
    UserRole.TEAM_LEADER -> listOf(
        Screen.Dashboard,
        Screen.Broadcast,
        Screen.Teams,
        Screen.Availability,
        Screen.Settings,
    )
    UserRole.TEAM_MEMBER -> listOf(
        Screen.Availability,
        Screen.Settings,
    )
    UserRole.NONE -> emptyList()
}
