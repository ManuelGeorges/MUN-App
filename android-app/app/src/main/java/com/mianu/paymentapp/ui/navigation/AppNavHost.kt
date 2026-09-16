package com.mianu.paymentapp.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mianu.paymentapp.ui.UserRole
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.ui.screens.AccessControlScreen
import com.mianu.paymentapp.ui.screens.ActivityScreen
import com.mianu.paymentapp.ui.screens.AttendanceScanScreen
import com.mianu.paymentapp.ui.screens.AvailabilityScreen
import com.mianu.paymentapp.ui.screens.BroadcastScreen
import com.mianu.paymentapp.ui.screens.DashboardScreen
import com.mianu.paymentapp.ui.screens.HallsScreen
import com.mianu.paymentapp.ui.screens.LocationsScreen
import com.mianu.paymentapp.ui.screens.LoginScreen
import com.mianu.paymentapp.ui.screens.MealSwipeScreen
import com.mianu.paymentapp.ui.screens.ReportsScreen
import com.mianu.paymentapp.ui.screens.SettingsScreen
import com.mianu.paymentapp.ui.screens.TeamsScreen
import com.mianu.paymentapp.ui.screens.UsersScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    userRole: UserRole,
    onAuthenticated: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A cross-fade rather than a slide: bottom-bar destinations are siblings, and directional
    // motion between them would imply a hierarchy that doesn't exist.
    val fade = fadeIn(tween(220))
    val fadeAway = fadeOut(tween(160))

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier,
        enterTransition = { fade },
        exitTransition = { fadeAway },
        popEnterTransition = { fade },
        popExitTransition = { fadeAway },
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onAuthenticated = {
                    onAuthenticated()
                    val target = if (SessionManager.currentRole == UserRole.ORGANIZER) {
                        Screen.MealScan.route
                    } else {
                        Screen.Dashboard.route
                    }
                    navController.navigate(target) {
                        // Clear the auth screen so back doesn't return to a signed-in login form.
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Dashboard.route) {
            if (userRole == UserRole.ORGANIZER) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Screen.MealScan.route) {
                        popUpTo(Screen.MealScan.route) { inclusive = true }
                    }
                }
            } else {
                DashboardScreen(
                    role = userRole,
                    onNavigate = { route -> navController.navigateToTab(route) },
                )
            }
        }

        composable(Screen.AttendanceScan.route) { AttendanceScanScreen() }
        composable(Screen.MealScan.route) { MealSwipeScreen() }
        composable(Screen.AccessScan.route) { AccessControlScreen() }
        composable(Screen.Locations.route) {
            if (userRole == UserRole.ORGANIZER) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Screen.MealScan.route) {
                        popUpTo(Screen.MealScan.route) { inclusive = true }
                    }
                }
            } else {
                LocationsScreen()
            }
        }
        composable(Screen.Wallet.route) {
            if (userRole == UserRole.ORGANIZER) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(Screen.MealScan.route) {
                        popUpTo(Screen.MealScan.route) { inclusive = true }
                    }
                }
            } else {
                LocationsScreen()
            }
        }
        composable(Screen.Users.route) { UsersScreen() }
        composable(Screen.Teams.route) { TeamsScreen() }
        composable(Screen.Halls.route) { HallsScreen() }
        composable(Screen.Availability.route) { AvailabilityScreen() }
        composable(Screen.Broadcast.route) { BroadcastScreen() }
        composable(Screen.Reports.route) { ReportsScreen() }
        composable(Screen.Activity.route) { ActivityScreen() }

        composable(Screen.Settings.route) {
            SettingsScreen(
                role = userRole,
                onSignOut = {
                    onSignOut()
                    navController.navigate(Screen.Login.route) {
                        // Drop the whole authenticated graph on sign-out.
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

/**
 * Navigates to a top-level destination without stacking duplicates.
 *
 * Saves and restores each tab's state so returning to a list doesn't reset its scroll position or
 * refetch, and pops back to the base destination so the back stack stays one level deep.
 */
fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        val baseRoute = if (SessionManager.currentRole == UserRole.ORGANIZER) {
            Screen.MealScan.route
        } else {
            Screen.Dashboard.route
        }
        popUpTo(baseRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
