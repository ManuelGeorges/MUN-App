package com.mianu.paymentapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mianu.paymentapp.ui.UserRole
import com.mianu.paymentapp.ui.navigation.Screen
import com.mianu.paymentapp.ui.navigation.bottomNavFor
import com.mianu.paymentapp.ui.navigation.navigateToTab
import com.mianu.paymentapp.ui.theme.MianuTheme

/**
 * Role-scoped bottom navigation. Renders nothing when signed out, so the login screen is full-bleed.
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    userRole: UserRole,
) {
    val items = bottomNavFor(userRole)
    val colors = MianuTheme.colors
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Hidden when signed out, and on the login route even if a role is somehow already set.
    //
    // Wrapped in a conditional rather than guarded by an early `return`: bailing out after
    // currentBackStackEntryAsState() has already opened a composition group unbalances Compose's
    // group stack, and the resulting IndexOutOfBounds surfaces during recomposition with a stack
    // trace that names only the runtime.
    val visible = items.isNotEmpty() && currentRoute != Screen.Login.route

    if (visible) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Hairline top border. The bar sits over the ambient mesh, where a soft shadow alone
            // reads as mud rather than as an edge.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border),
            )

            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color(0xFF000000),
                contentColor = colors.fgMuted,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { if (!selected) navController.navigateToTab(screen.route) },
                        icon = {
                            screen.icon?.let { icon ->
                                Icon(imageVector = icon, contentDescription = screen.title)
                            }
                        },
                        label = {
                            Text(
                                text = screen.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 0.6.sp,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                ),
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color.White,
                            selectedTextColor = androidx.compose.ui.graphics.Color.White,
                            unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF717886),
                            unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF717886),
                            indicatorColor = androidx.compose.ui.graphics.Color(0x22FFFFFF),
                        ),
                    )
                }
            }
        }
    }
}
