package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.data.onSuccess
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.IconBadge
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import kotlinx.coroutines.launch

data class ActivityItem(
    val id: String,
    val title: String,
    val detail: String,
    val timestamp: String,
    val category: String,
    val icon: ImageVector,
)

@Composable
fun ActivityScreen() {
    val session by SessionManager.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filterCategory by remember { mutableStateOf("ALL") }

    fun refreshActivity() {
        scope.launch {
            isLoading = true
            val activityList = mutableListOf<ActivityItem>()
            val currentUserId = session.userId

            if (!currentUserId.isNullOrBlank()) {
                MianuRepository.instance.getInbox(currentUserId).onSuccess { notifs ->
                    notifs.forEach { n ->
                        activityList.add(
                            ActivityItem(
                                id = n.id,
                                title = "Announcement: ${n.audience.name}",
                                detail = n.message,
                                timestamp = n.timestamp.take(16).replace('T', ' '),
                                category = "BROADCAST",
                                icon = Icons.Default.Campaign,
                            )
                        )
                    }
                }
            }

            MianuRepository.instance.getLiveScans().onSuccess { scans ->
                scans.forEach { s ->
                    activityList.add(
                        ActivityItem(
                            id = s.id,
                            title = "${s.delegateName} — ${s.locationOrService}",
                            detail = if (s.allowed) "Status: Authorized" else "Status: Denied (${s.reason ?: "Unauthorized"})",
                            timestamp = s.timestamp.take(16).replace('T', ' '),
                            category = if (s.scanType == "meal_swipe") "MEALS" else "ACCESS",
                            icon = if (s.scanType == "meal_swipe") Icons.Default.Restaurant else Icons.Default.Contactless,
                        )
                    )
                }
            }

            MianuRepository.instance.getDashboardOverview().onSuccess { dash ->
                activityList.add(
                    ActivityItem(
                        id = "overview-meals",
                        title = "Meal Service Operating",
                        detail = "${dash.totalMealsToday} meals served today across catering halls",
                        timestamp = "Live Status",
                        category = "SYSTEM",
                        icon = Icons.Default.Restaurant,
                    )
                )
                activityList.add(
                    ActivityItem(
                        id = "overview-access",
                        title = "Access Control Active",
                        detail = "${dash.totalAccessScansToday} hall access scans logged today",
                        timestamp = "Live Status",
                        category = "SYSTEM",
                        icon = Icons.Default.Contactless,
                    )
                )
            }

            items = activityList
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshActivity()
    }

    val filteredItems = items.filter {
        filterCategory == "ALL" || it.category == filterCategory
    }

    AmbientBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader(
                        title = "Activity & Audit",
                        eyebrow = "Conference Log",
                        subtitle = "Real-time updates, notifications & service events",
                    )
                    IconButton(onClick = { refreshActivity() }) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = colors.accent,
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(
                        text = "All Activity",
                        selected = filterCategory == "ALL",
                        onClick = { filterCategory = "ALL" },
                    )
                    FilterPill(
                        text = "Broadcasts",
                        selected = filterCategory == "BROADCAST",
                        onClick = { filterCategory = "BROADCAST" },
                    )
                    FilterPill(
                        text = "System",
                        selected = filterCategory == "SYSTEM",
                        onClick = { filterCategory = "SYSTEM" },
                    )
                }
            }

            if (isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(color = colors.accent)
                    }
                }
            } else if (filteredItems.isEmpty()) {
                item {
                    MianuCard {
                        EmptyState(
                            title = "No activity recorded",
                            description = "Conference events, broadcasts and transactions will appear here.",
                            icon = Icons.Default.Notifications,
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    MianuCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconBadge(icon = item.icon, tint = colors.accent)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.fg,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = item.detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.fgMuted,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.fgMuted,
                                )
                            }
                            StatusPill(
                                text = item.category,
                                color = if (item.category == "BROADCAST") colors.accent else colors.fgMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}
