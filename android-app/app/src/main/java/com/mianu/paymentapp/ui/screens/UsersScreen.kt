package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.mianu.paymentapp.data.models.TeamResponse
import com.mianu.paymentapp.data.models.TeamRole
import com.mianu.paymentapp.data.models.UserResponse
import com.mianu.paymentapp.data.models.UserRole
import com.mianu.paymentapp.data.models.UserUpdate
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.ErrorState
import com.mianu.paymentapp.nfc.NfcEvent
import com.mianu.paymentapp.nfc.NfcHub
import com.mianu.paymentapp.nfc.OnNfcEvent
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.LoadingList
import com.mianu.paymentapp.ui.components.MessageBanner
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuShapes
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.UiState
import com.mianu.paymentapp.ui.viewmodel.UsersViewModel

/** Delegate directory: search, filter, register, link cards, suspend and remove. */
@Composable
fun UsersScreen(viewModel: UsersViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    var showCreateDialog by remember { mutableStateOf(false) }
    var cardLinkTarget by remember { mutableStateOf<UserResponse?>(null) }
    var deleteTarget by remember { mutableStateOf<UserResponse?>(null) }
    var adjustMealsTarget by remember { mutableStateOf<UserResponse?>(null) }
    var editTarget by remember { mutableStateOf<UserResponse?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.accent,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Register", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
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
                        title = "Delegates",
                        eyebrow = "Directory",
                        subtitle = state.users.let { s ->
                            if (s is UiState.Success) "${state.visibleUsers.size} of ${s.data.size} shown" else null
                        },
                    )
                }

                item {
                    MianuTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        label = "Search",
                        placeholder = "Name, email or phone",
                        leadingIcon = Icons.Default.Search,
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterPill(
                                text = "All roles",
                                selected = state.roleFilter == null,
                                onClick = { viewModel.onRoleFilterChange(null) },
                            )
                        }
                        items(UserRole.entries.toList()) { role ->
                            FilterPill(
                                text = role.displayName,
                                selected = state.roleFilter == role,
                                onClick = { viewModel.onRoleFilterChange(role) },
                            )
                        }
                        item {
                            FilterPill(
                                text = if (state.showInactive) "Incl. suspended" else "Active only",
                                selected = !state.showInactive,
                                onClick = viewModel::toggleShowInactive,
                            )
                        }
                    }
                }

                state.message?.let { message ->
                    item { MessageBanner(message = message, onDismiss = viewModel::consumeMessage) }
                }

                when (val users = state.users) {
                    is UiState.Loading -> item { LoadingList(rows = 5) }

                    is UiState.Error -> item {
                        ErrorState(error = users.error, onRetry = viewModel::refresh)
                    }

                    is UiState.Success -> {
                        if (state.visibleUsers.isEmpty()) {
                            item {
                                EmptyState(
                                    title = if (users.data.isEmpty()) {
                                        "No delegates registered"
                                    } else {
                                        "No matches"
                                    },
                                    description = if (users.data.isEmpty()) {
                                        "Register the first delegate to get started."
                                    } else {
                                        "Try a different search or clear the filters."
                                    },
                                    icon = Icons.Default.PersonAdd,
                                    action = {
                                        if (users.data.isEmpty()) {
                                            MianuButton(
                                                text = "Register delegate",
                                                onClick = { showCreateDialog = true },
                                                icon = Icons.Default.Add,
                                            )
                                        }
                                    },
                                )
                            }
                        } else {
                            items(state.visibleUsers, key = { it.id }) { user ->
                                UserRow(
                                    user = user,
                                    teamName = state.teamName(user.teamId),
                                    onClick = { editTarget = user },
                                    onLinkCard = { cardLinkTarget = user },
                                    onAdjustMeals = { adjustMealsTarget = user },
                                    onEdit = { editTarget = user },
                                    onToggleActive = { viewModel.toggleActive(user) },
                                    onDelete = { deleteTarget = user },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            teams = state.teams,
            isSubmitting = state.isMutating,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, email, phone, password, role, mealAllowance, teamId ->
                viewModel.createUser(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    role = role,
                    mealAllowance = mealAllowance,
                    teamId = teamId,
                ) {
                    showCreateDialog = false
                }
            },
        )
    }

    cardLinkTarget?.let { user ->
        LinkCardDialog(
            user = user,
            isSubmitting = state.isMutating,
            onDismiss = { cardLinkTarget = null },
            onLink = { uid ->
                viewModel.linkCard(user.id, uid) { cardLinkTarget = null }
            },
        )
    }

    adjustMealsTarget?.let { user ->
        AdjustMealsDialog(
            user = user,
            isSubmitting = state.isMutating,
            onDismiss = { adjustMealsTarget = null },
            onAdjust = { delta, bal ->
                viewModel.adjustMeals(user.id, delta = delta, mealsBalance = bal) {
                    adjustMealsTarget = null
                }
            },
        )
    }

    editTarget?.let { user ->
        EditUserDialog(
            user = user,
            teams = state.teams,
            isSubmitting = state.isMutating,
            onDismiss = { editTarget = null },
            onUpdate = { update ->
                viewModel.updateUser(user.id, update) {
                    editTarget = null
                }
            },
            onAdjustMeals = {
                val target = user
                editTarget = null
                adjustMealsTarget = target
            },
        )
    }

    deleteTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = colors.surfaceOverlay,
            shape = MianuShapes.Large,
            title = { Text("Remove ${user.name}?", color = colors.fg) },
            text = {
                Text(
                    "This deletes the delegate and unlinks their card. Suspending them instead " +
                        "keeps their history intact.",
                    color = colors.fgMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(user)
                    deleteTarget = null
                }) {
                    Text("Remove", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = colors.fgMuted)
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateUserDialog(
    teams: List<TeamResponse> = emptyList(),
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String?, String?, String?, UserRole, Int, String?) -> Unit,
) {
    val colors = MianuTheme.colors
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mealAllowance by remember { mutableStateOf("6") }
    var role by remember { mutableStateOf(UserRole.USER) }
    var selectedTeamId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceOverlay,
        shape = MianuShapes.Large,
        title = { Text("Register delegate", color = colors.fg) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MianuTextField(value = name, onValueChange = { name = it }, label = "Full name")
                MianuTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email (optional)",
                    keyboardType = KeyboardType.Email,
                )
                MianuTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone (optional)",
                    keyboardType = KeyboardType.Phone,
                )
                MianuTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password (optional)",
                    supportingText = "Defaults to Welcome@2026 if blank",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                )
                MianuTextField(
                    value = mealAllowance,
                    onValueChange = { mealAllowance = it.filter(Char::isDigit) },
                    label = "Meal allowance (meals for conference)",
                    supportingText = "Default: 6 meals (2 meals/day x 3 days)",
                    keyboardType = KeyboardType.Number,
                )
                Text("Role", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { option ->
                        FilterPill(
                            text = option.displayName,
                            selected = role == option,
                            onClick = { role = option },
                        )
                    }
                }
                if (teams.isNotEmpty()) {
                    Text("Team assignment (optional)", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterPill(
                            text = "None",
                            selected = selectedTeamId == null,
                            onClick = { selectedTeamId = null },
                        )
                        teams.forEach { team ->
                            FilterPill(
                                text = team.name,
                                selected = selectedTeamId == team.id,
                                onClick = { selectedTeamId = team.id },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        name.trim(),
                        email.trim().ifBlank { null },
                        phone.trim().ifBlank { null },
                        password.trim().ifBlank { null },
                        role,
                        mealAllowance.toIntOrNull() ?: 6,
                        selectedTeamId,
                    )
                },
                enabled = name.isNotBlank() && !isSubmitting,
            ) {
                Text(if (isSubmitting) "Saving…" else "Register", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fgMuted) }
        },
    )
}

@Composable
private fun AdjustMealsDialog(
    user: UserResponse,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onAdjust: (delta: Int?, balance: Int?) -> Unit,
) {
    val colors = MianuTheme.colors
    var customBalance by remember { mutableStateOf(user.mealsBalance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceOverlay,
        shape = MianuShapes.Large,
        title = { Text("Adjust meals • ${user.name}", color = colors.fg) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Current balance: ${user.mealsBalance} / ${user.mealAllowance} meals remaining",
                    color = colors.fgMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Quick adjustments:",
                    color = colors.fg,
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MianuButton(
                        text = "-1 Meal",
                        onClick = { onAdjust(-1, null) },
                        enabled = !isSubmitting && user.mealsBalance > 0,
                        modifier = Modifier.weight(1f),
                    )
                    MianuButton(
                        text = "+1 Meal",
                        onClick = { onAdjust(1, null) },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
                MianuButton(
                    text = "Reset to Allowance (${user.mealAllowance})",
                    onClick = { onAdjust(null, user.mealAllowance) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                MianuTextField(
                    value = customBalance,
                    onValueChange = { customBalance = it.filter(Char::isDigit) },
                    label = "Set exact meals balance",
                    keyboardType = KeyboardType.Number,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val bal = customBalance.toIntOrNull()
                    if (bal != null) onAdjust(null, bal)
                },
                enabled = customBalance.isNotBlank() && !isSubmitting,
            ) {
                Text(if (isSubmitting) "Saving…" else "Set Balance", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = colors.fgMuted) }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditUserDialog(
    user: UserResponse,
    teams: List<TeamResponse>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (UserUpdate) -> Unit,
    onAdjustMeals: () -> Unit,
) {
    val colors = MianuTheme.colors
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var role by remember { mutableStateOf(user.roleEnum) }
    var selectedTeamId by remember { mutableStateOf(user.teamId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceOverlay,
        shape = MianuShapes.Large,
        title = { Text("Edit delegate • ${user.name}", color = colors.fg) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MianuTextField(value = name, onValueChange = { name = it }, label = "Full name")
                MianuTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                )
                MianuTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    keyboardType = KeyboardType.Phone,
                )
                Text("Role", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { option ->
                        FilterPill(
                            text = option.displayName,
                            selected = role == option,
                            onClick = { role = option },
                        )
                    }
                }
                if (teams.isNotEmpty()) {
                    Text("Team assignment", style = MaterialTheme.typography.labelMedium, color = colors.fgMuted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterPill(
                            text = "No Team",
                            selected = selectedTeamId == null,
                            onClick = { selectedTeamId = null },
                        )
                        teams.forEach { team ->
                            FilterPill(
                                text = team.name,
                                selected = selectedTeamId == team.id,
                                onClick = { selectedTeamId = team.id },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                MianuButton(
                    text = "Adjust meals (${user.mealsBalance}/${user.mealAllowance})",
                    onClick = onAdjustMeals,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(
                        UserUpdate(
                            name = name.trim(),
                            email = email.trim().ifBlank { null },
                            phone = phone.trim().ifBlank { null },
                            role = role,
                            teamId = selectedTeamId,
                            teamRole = if (selectedTeamId != null) (user.teamRole ?: TeamRole.MEMBER) else null,
                        )
                    )
                },
                enabled = name.isNotBlank() && !isSubmitting,
            ) {
                Text(if (isSubmitting) "Saving…" else "Save", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fgMuted) }
        },
    )
}

@Composable
private fun LinkCardDialog(
    user: UserResponse,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onLink: (String) -> Unit,
) {
    val colors = MianuTheme.colors
    var uid by remember { mutableStateOf("") }
    var encode by remember { mutableStateOf(false) }
    var writeNote by remember { mutableStateOf<String?>(null) }
    var writeFailed by remember { mutableStateOf(false) }

    // Re-armed after every write, so a desk enrolling a stack of badges keeps working on one toggle
    // instead of a tap per badge. Bumping this key is what re-arms.
    var armCount by remember { mutableStateOf(0) }

    // Arming is single-shot in the hub, and disarms on dispose — closing this dialog must not leave
    // the next badge tapped anywhere in the app silently overwritten.
    DisposableEffect(encode, armCount) {
        if (encode) NfcHub.armWrite(cardUid = "", delegateName = user.name)
        onDispose { NfcHub.cancelWrite() }
    }

    OnNfcEvent(enabled = !isSubmitting) { event ->
        when (event) {
            is NfcEvent.Scanned -> {
                uid = event.uid
                writeNote = null
                writeFailed = false
            }
            // Encoding is supplementary — the link is keyed on the hardware UID either way, so a
            // failed write still yields a usable badge and must not block enrolment.
            is NfcEvent.Written -> {
                uid = event.uid
                writeNote = "Badge encoded for ${user.name}."
                writeFailed = false
                armCount++
            }
            is NfcEvent.WriteFailed -> {
                uid = event.uid
                writeNote = "${event.reason} The card can still be linked."
                writeFailed = true
                armCount++
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceOverlay,
        shape = MianuShapes.Large,
        title = { Text("Link card", color = colors.fg) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Tap ${user.name}'s badge to read it, or enter the UID by hand.",
                    color = colors.fgMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                MianuTextField(
                    value = uid,
                    onValueChange = { uid = it },
                    label = "Card UID",
                    placeholder = "e.g. 04A2B3C4D5",
                )

                FilterPill(
                    text = if (encode) "Encoding on tap" else "Encode badge on tap",
                    selected = encode,
                    onClick = {
                        encode = !encode
                        writeNote = null
                        writeFailed = false
                    },
                )

                Text(
                    text = writeNote
                        // Says "overwrites" plainly: this is destructive to whatever the tag held,
                        // and a steward should know that before arming it on a stack of badges.
                        ?: if (encode) {
                            "The next badge tapped will be overwritten with ${user.name}'s details."
                        } else {
                            "Optional. Writes the delegate's name onto the badge so a lost one can be identified."
                        },
                    color = if (writeFailed) colors.danger else colors.fgMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLink(uid.trim()) },
                enabled = uid.isNotBlank() && !isSubmitting,
            ) {
                Text(if (isSubmitting) "Linking…" else "Link", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.fgMuted) }
        },
    )
}
