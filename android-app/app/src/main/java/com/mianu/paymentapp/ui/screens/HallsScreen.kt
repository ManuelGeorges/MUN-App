package com.mianu.paymentapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.models.UserRole
import com.mianu.paymentapp.ui.components.CapacityMeter
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.ErrorState
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.LoadingList
import com.mianu.paymentapp.ui.components.MessageBanner
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuShapes
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.HallsViewModel
import com.mianu.paymentapp.ui.viewmodel.UiState

/** Hall configuration and live occupancy. Tapping a hall expands its presence list. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HallsScreen(viewModel: HallsViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.accent,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                text = { Text("Add hall", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            )
        },
    ) { padding ->
        AmbientBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, top = 20.dp, bottom = 92.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SectionHeader(
                        title = "Halls",
                        eyebrow = "Access control",
                        subtitle = "Capacity thresholds and who's inside",
                    )
                }

                state.message?.let { message ->
                    item { MessageBanner(message = message, onDismiss = viewModel::consumeMessage) }
                }

                when (val halls = state.halls) {
                    is UiState.Loading -> item { LoadingList(rows = 3, rowHeight = 96.dp) }

                    is UiState.Error -> item {
                        ErrorState(error = halls.error, onRetry = viewModel::refresh)
                    }

                    is UiState.Success -> {
                        if (halls.data.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No halls configured",
                                    description = "Add a hall to start controlling access and tracking occupancy.",
                                    icon = Icons.Default.MeetingRoom,
                                    action = {
                                        MianuButton(
                                            text = "Add hall",
                                            onClick = { showCreateDialog = true },
                                            icon = Icons.Default.AddBusiness,
                                        )
                                    },
                                )
                            }
                        } else {
                            items(halls.data, key = { it.id }) { hall ->
                                val expanded = state.expandedHallId == hall.id
                                MianuCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { viewModel.toggleHall(hall.id) },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = hall.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = colors.fg,
                                            )
                                            Text(
                                                text = "${hall.currentOccupancy} of ${hall.capacityThreshold} inside",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.fgMuted,
                                            )
                                        }
                                        StatusPill(
                                            text = when {
                                                hall.isAtCapacity -> "At capacity"
                                                hall.isNearCapacity -> "Filling up"
                                                else -> "Open"
                                            },
                                            color = when {
                                                hall.isAtCapacity -> colors.danger
                                                hall.isNearCapacity -> colors.warning
                                                else -> colors.success
                                            },
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    CapacityMeter(ratio = hall.occupancyRatio)

                                    if (hall.allowedRoles.isNotEmpty()) {
                                        Spacer(Modifier.height(10.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            hall.allowedRoles.forEach { role ->
                                                StatusPill(
                                                    text = role.displayName,
                                                    color = colors.info,
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(visible = expanded) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Spacer(Modifier.height(14.dp))
                                            Text(
                                                text = "CURRENTLY INSIDE",
                                                style = com.mianu.paymentapp.ui.theme.EyebrowStyle,
                                                color = colors.fgMuted,
                                            )
                                            Spacer(Modifier.height(8.dp))

                                            val present = state.presenceFor(hall.id)
                                            when {
                                                state.loadingPresenceFor == hall.id -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.height(20.dp),
                                                        strokeWidth = 2.dp,
                                                        color = colors.accent,
                                                    )
                                                }
                                                present.isEmpty() -> {
                                                    Text(
                                                        text = "Nobody is currently inside.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = colors.fgMuted,
                                                    )
                                                }
                                                else -> {
                                                    present.forEach { userId ->
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.padding(vertical = 4.dp),
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Person,
                                                                contentDescription = null,
                                                                tint = colors.fgMuted,
                                                                modifier = Modifier.height(16.dp),
                                                            )
                                                            Text(
                                                                text = state.displayName(userId),
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = colors.fg,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateHallDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, capacity, roles ->
                viewModel.createHall(name, capacity, roles) { showCreateDialog = false }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateHallDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, List<UserRole>) -> Unit,
) {
    val colors = MianuTheme.colors
    var name by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("100") }
    // Defaults to everyone; narrowing is the deliberate act, not widening.
    val selectedRoles = remember { mutableStateListOf(*UserRole.entries.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceOverlay,
        shape = MianuShapes.Large,
        title = { Text("Add hall", color = colors.fg) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MianuTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Hall name",
                    placeholder = "General Assembly",
                )
                MianuTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter(Char::isDigit) },
                    label = "Capacity threshold",
                    keyboardType = KeyboardType.Number,
                )
                Text("Allowed roles", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        FilterPill(
                            text = role.displayName,
                            selected = role in selectedRoles,
                            onClick = {
                                if (role in selectedRoles) selectedRoles.remove(role)
                                else selectedRoles.add(role)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            val size = capacity.toIntOrNull() ?: 0
            TextButton(
                onClick = { onCreate(name.trim(), size, selectedRoles.toList()) },
                // A hall nobody may enter is never intended, so require at least one role.
                enabled = name.isNotBlank() && size > 0 && selectedRoles.isNotEmpty(),
            ) {
                Text("Add", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fgMuted) }
        },
    )
}
