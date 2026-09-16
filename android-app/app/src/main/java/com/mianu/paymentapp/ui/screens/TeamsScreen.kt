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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.mianu.paymentapp.data.models.TeamResponse
import com.mianu.paymentapp.data.models.TeamRole
import com.mianu.paymentapp.data.models.UserResponse
import com.mianu.paymentapp.ui.components.Avatar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.Permissions
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.ui.components.CapacityMeter
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.ErrorState
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.GlassCard
import com.mianu.paymentapp.ui.components.IconBadge
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
import com.mianu.paymentapp.ui.viewmodel.TeamsViewModel
import com.mianu.paymentapp.ui.viewmodel.UiState

/** Teams and their broadcast feed. */
@Composable
fun TeamsScreen(viewModel: TeamsViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var deleteTeamTarget by remember { mutableStateOf<TeamResponse?>(null) }
    var removeMemberTarget by remember { mutableStateOf<UserResponse?>(null) }

    // Creating and editing teams is an admin/chief-organizer action. A team leader opens this screen
    // to see their roster and broadcast, not to spin up teams — so they get no create controls, and
    // the server would 403 them anyway.
    val canManage = SessionManager.has(Permissions.MANAGE_TEAMS)

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (canManage) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = colors.accent,
                    contentColor = Color.Black,
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                    text = { Text("New team", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                )
            }
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SectionHeader(
                        title = "Teams",
                        eyebrow = "Coordination",
                        subtitle = "Staffing levels, rosters and broadcasts",
                    )
                }

                state.message?.let { message ->
                    item { MessageBanner(message = message, onDismiss = viewModel::consumeMessage) }
                }

                when (val teams = state.teams) {
                    is UiState.Loading -> item { LoadingList(rows = 3) }

                    is UiState.Error -> item {
                        ErrorState(error = teams.error, onRetry = viewModel::refresh)
                    }

                    is UiState.Success -> {
                        if (teams.data.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No teams yet",
                                    description = if (canManage) {
                                        "Create a team to coordinate staff and send broadcasts."
                                    } else {
                                        "No teams have been set up yet. An organizer needs to create one."
                                    },
                                    icon = Icons.Default.Group,
                                    action = if (canManage) {
                                        {
                                            MianuButton(
                                                text = "Create team",
                                                onClick = { showCreateDialog = true },
                                                icon = Icons.Default.GroupAdd,
                                            )
                                        }
                                    } else null,
                                )
                            }
                        } else {
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(teams.data, key = { it.id }) { team ->
                                        FilterPill(
                                            text = team.name,
                                            selected = team.id == state.selectedTeamId,
                                            onClick = { viewModel.selectTeam(team.id) },
                                        )
                                    }
                                }
                            }

                            state.selectedTeam?.let { team ->
                                item {
                                    MianuCard(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = team.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = colors.fg,
                                                )
                                                Text(
                                                    text = "${state.teamMembers.size} of ${team.capacity} assigned",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colors.fgMuted,
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StatusPill(
                                                    text = if (state.teamMembers.size >= team.capacity) "Full" else "Open",
                                                    color = if (state.teamMembers.size >= team.capacity) colors.warning else colors.success,
                                                )
                                                if (canManage) {
                                                    Spacer(Modifier.size(8.dp))
                                                    IconButton(onClick = { deleteTeamTarget = team }) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Delete team",
                                                            tint = colors.danger,
                                                            modifier = Modifier.size(20.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        val ratio = if (team.capacity > 0) {
                                            (state.teamMembers.size.toFloat() / team.capacity).coerceIn(0f, 1f)
                                        } else 0f
                                        CapacityMeter(ratio = ratio)
                                    }
                                }

                                // Team Roster Section
                                item {
                                    SectionHeader(
                                        title = "Team Roster",
                                        eyebrow = "Members",
                                        subtitle = "${state.teamMembers.size} assigned",
                                        trailing = if (canManage) {
                                            {
                                                MianuButton(
                                                    text = "Add Member",
                                                    icon = Icons.Default.PersonAdd,
                                                    onClick = { showAddMemberDialog = true },
                                                )
                                            }
                                        } else null,
                                    )
                                }

                                if (state.isLoadingMembers) {
                                    item { LoadingList(rows = 2, rowHeight = 60.dp) }
                                } else if (state.teamMembers.isEmpty()) {
                                    item {
                                        MianuCard {
                                            EmptyState(
                                                title = "No members yet",
                                                description = "Add delegates to ${team.name} to form the team.",
                                                icon = Icons.Default.Group,
                                            )
                                        }
                                    }
                                } else {
                                    items(state.teamMembers, key = { it.id }) { member ->
                                        MianuCard(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                Avatar(
                                                    initials = member.initials,
                                                    color = colors.accent,
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = member.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = colors.fg,
                                                    )
                                                    val sub = member.email ?: member.phone ?: member.roleEnum.displayName
                                                    Text(
                                                        text = sub,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = colors.fgMuted,
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    val isLeader = member.teamRole == TeamRole.LEADER
                                                    StatusPill(
                                                        text = if (isLeader) "Team Leader" else "Member",
                                                        color = if (isLeader) colors.accent else colors.info,
                                                        icon = Icons.Default.Group,
                                                    )
                                                }
                                                if (canManage) {
                                                    IconButton(onClick = { removeMemberTarget = member }) {
                                                        Icon(
                                                            Icons.Default.PersonRemove,
                                                            contentDescription = "Remove from team",
                                                            tint = colors.danger,
                                                            modifier = Modifier.size(20.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        SectionHeader(title = "Broadcast", eyebrow = "Send an update")
                                        Spacer(Modifier.height(12.dp))
                                        MianuTextField(
                                            value = state.draftMessage,
                                            onValueChange = viewModel::onDraftChange,
                                            label = "Message",
                                            placeholder = "Opening ceremony starts in 15 minutes…",
                                            singleLine = false,
                                            supportingText = if (state.canBroadcast) {
                                                null
                                            } else if (state.draftMessage.isBlank()) {
                                                "Write a message to broadcast to ${team.name}"
                                            } else {
                                                "Your account id couldn't be resolved — sign in again"
                                            },
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        MianuButton(
                                            text = "Send to ${team.name}",
                                            onClick = viewModel::broadcast,
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = state.canBroadcast,
                                            loading = state.isSending,
                                            icon = Icons.AutoMirrored.Filled.Send,
                                        )
                                    }
                                }

                                item {
                                    SectionHeader(title = "Recent broadcasts", eyebrow = "Feed")
                                }

                                if (state.isLoadingNotifications) {
                                    item { LoadingList(rows = 2, rowHeight = 76.dp) }
                                } else if (state.notifications.isEmpty()) {
                                    item {
                                        MianuCard {
                                            EmptyState(
                                                title = "No broadcasts yet",
                                                description = "Messages sent to ${team.name} will appear here.",
                                                icon = Icons.Default.Campaign,
                                            )
                                        }
                                    }
                                } else {
                                    items(state.notifications, key = { it.id }) { notification ->
                                        MianuCard(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                IconBadge(
                                                    icon = Icons.Default.Campaign,
                                                    tint = colors.accent,
                                                    size = 34.dp,
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = notification.message,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = colors.fg,
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = notification.timestamp
                                                            .take(19).replace('T', ' '),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = colors.fgMuted,
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

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var capacity by remember { mutableStateOf("10") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = colors.surfaceOverlay,
            shape = MianuShapes.Large,
            title = { Text("Create team", color = colors.fg) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MianuTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Team name",
                        placeholder = "Registration desk",
                    )
                    MianuTextField(
                        value = capacity,
                        onValueChange = { capacity = it.filter(Char::isDigit) },
                        label = "Capacity",
                        keyboardType = KeyboardType.Number,
                        supportingText = "How many staff this team can hold",
                    )
                }
            },
            confirmButton = {
                val size = capacity.toIntOrNull() ?: 0
                TextButton(
                    onClick = {
                        viewModel.createTeam(name.trim(), size) { showCreateDialog = false }
                    },
                    // Server requires capacity > 0, so block a zero here rather than round-trip it.
                    enabled = name.isNotBlank() && size > 0,
                ) {
                    Text("Create", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = colors.fgMuted)
                }
            },
        )
    }

    if (showAddMemberDialog) {
        state.selectedTeam?.let { team ->
            AddMemberDialog(
                availableUsers = state.allUsers,
                currentMembers = state.teamMembers,
                onDismiss = { showAddMemberDialog = false },
                onAssign = { userId, role ->
                    viewModel.assignMember(team.id, userId, role) {
                        showAddMemberDialog = false
                    }
                },
            )
        }
    }

    deleteTeamTarget?.let { team ->
        AlertDialog(
            onDismissRequest = { deleteTeamTarget = null },
            containerColor = colors.surfaceOverlay,
            shape = MianuShapes.Large,
            title = { Text("Delete team ${team.name}?", color = colors.fg) },
            text = {
                Text(
                    "This will remove the team and unassign all ${state.teamMembers.size} member(s). Delegates will remain in the directory.",
                    color = colors.fgMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTeam(team.id) { deleteTeamTarget = null }
                }) {
                    Text("Delete", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTeamTarget = null }) {
                    Text("Cancel", color = colors.fgMuted)
                }
            },
        )
    }

    removeMemberTarget?.let { member ->
        val team = state.selectedTeam
        if (team != null) {
            AlertDialog(
                onDismissRequest = { removeMemberTarget = null },
                containerColor = colors.surfaceOverlay,
                shape = MianuShapes.Large,
                title = { Text("Remove ${member.name}?", color = colors.fg) },
                text = {
                    Text(
                        "Remove ${member.name} from ${team.name}? They will become an unassigned delegate.",
                        color = colors.fgMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeMember(team.id, member.id) { removeMemberTarget = null }
                    }) {
                        Text("Remove", color = colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { removeMemberTarget = null }) {
                        Text("Cancel", color = colors.fgMuted)
                    }
                },
            )
        }
    }
}

@Composable
private fun AddMemberDialog(
    availableUsers: List<UserResponse>,
    currentMembers: List<UserResponse>,
    onDismiss: () -> Unit,
    onAssign: (userId: String, role: TeamRole) -> Unit,
) {
    val colors = MianuTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf(TeamRole.MEMBER) }

    val unassignedUsers = remember(availableUsers, currentMembers, searchQuery) {
        val memberIds = currentMembers.map { it.id }.toSet()
        val q = searchQuery.trim().lowercase()
        availableUsers
            .filter { it.id !in memberIds }
            .filter {
                q.isEmpty() ||
                    it.name.lowercase().contains(q) ||
                    it.email?.lowercase()?.contains(q) == true ||
                    it.phone?.contains(q) == true
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceOverlay,
        shape = MianuShapes.Large,
        title = { Text("Add team member", color = colors.fg) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MianuTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search delegates",
                    placeholder = "Type name or email...",
                    leadingIcon = Icons.Default.Search,
                )

                Text("Select delegate:", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                if (unassignedUsers.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) "All delegates are already in this team" else "No matching delegates",
                        color = colors.fgMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        unassignedUsers.take(8).forEach { user ->
                            FilterPill(
                                text = "${user.name} (${user.roleEnum.displayName})",
                                selected = selectedUserId == user.id,
                                onClick = { selectedUserId = user.id },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Role in team:", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(
                        text = "Member",
                        selected = selectedRole == TeamRole.MEMBER,
                        onClick = { selectedRole = TeamRole.MEMBER },
                    )
                    FilterPill(
                        text = "Team Leader",
                        selected = selectedRole == TeamRole.LEADER,
                        onClick = { selectedRole = TeamRole.LEADER },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedUserId?.let { uid -> onAssign(uid, selectedRole) }
                },
                enabled = selectedUserId != null,
            ) {
                Text("Add to team", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fgMuted) }
        },
    )
}
