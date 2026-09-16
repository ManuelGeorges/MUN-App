package com.mianu.paymentapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Dashboard : Destination("dashboard", "Dashboard", Icons.Filled.Home)
    data object Users : Destination("users", "Users", Icons.Filled.AccountCircle)
    data object Activity : Destination("activity", "Activity", Icons.Filled.List)
    data object Settings : Destination("settings", "Settings", Icons.Filled.Settings)
}

val BottomNavDestinations = listOf(
    Destination.Dashboard,
    Destination.Users,
    Destination.Activity,
    Destination.Settings,
)
