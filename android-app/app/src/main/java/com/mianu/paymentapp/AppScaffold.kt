package com.mianu.paymentapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.ui.UserRole
import com.mianu.paymentapp.ui.components.BottomNavigationBar
import com.mianu.paymentapp.ui.navigation.AppNavHost
import com.mianu.paymentapp.ui.navigation.Screen
import com.mianu.paymentapp.ui.theme.MianuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val session by SessionManager.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onLogin = currentRoute == Screen.Login.route || currentRoute == null
    val role = session.role

    val screenTitle = listOf(
        Screen.Dashboard, Screen.MealScan, Screen.AccessScan, Screen.Locations, Screen.Wallet,
        Screen.Users, Screen.Teams, Screen.Halls, Screen.Availability,
        Screen.Broadcast, Screen.Reports, Screen.Activity, Screen.Settings,
    ).firstOrNull { it.route == currentRoute }?.title

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            if (!onLogin && role != UserRole.NONE) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceRaised)
                                    .border(1.dp, colors.border, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "M",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = screenTitle ?: "MIANU-SM",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.fg,
                                )
                                Text(
                                    text = "MIANU-SM IV • 2026",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.fgMuted,
                                    letterSpacing = 0.8.sp,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.92f),
                        scrolledContainerColor = Color.Black.copy(alpha = 0.98f),
                        titleContentColor = Color.White,
                    ),
                    actions = {
                        val roleLabel = when (role) {
                            UserRole.ADMIN -> "ADMIN"
                            UserRole.CHIEF_ORGANIZER -> "CHIEF ORG"
                            UserRole.ORGANIZER -> "ORGANIZER"
                            UserRole.TEAM_LEADER -> "LEADER"
                            UserRole.TEAM_MEMBER -> "MEMBER"
                            UserRole.NONE -> ""
                        }
                        if (roleLabel.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF14171E))
                                    .border(1.dp, Color(0xFF333A48), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = roleLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!onLogin) {
                BottomNavigationBar(navController = navController, userRole = role)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AppNavHost(
                navController = navController,
                userRole = role,
                // SessionManager is the source of truth; these hooks just let navigation react.
                onAuthenticated = {},
                onSignOut = {},
            )
        }
    }
}
