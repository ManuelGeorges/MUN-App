package com.mianu.paymentapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.data.models.MealWindowRequest
import com.mianu.paymentapp.ui.UserRole
import com.mianu.paymentapp.ui.components.Avatar
import com.mianu.paymentapp.ui.components.DetailRow
import com.mianu.paymentapp.ui.components.GlassCard
import com.mianu.paymentapp.ui.components.LiveDot
import com.mianu.paymentapp.ui.components.MessageBanner
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.MianuSecondaryButton
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.OpsViewModel

/**
 * Session details, backend connectivity, and meal-window configuration.
 *
 * Meal windows are admin-only: they change when every delegate in the conference can eat.
 */
@Composable
fun SettingsScreen(
    role: UserRole,
    onSignOut: () -> Unit,
    viewModel: OpsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val session by SessionManager.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    var healthy by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        healthy = MianuRepository.instance.checkHealth() is com.mianu.paymentapp.data.ApiResult.Success
    }

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(title = "Settings", eyebrow = "Account & configuration")

            state.message?.let { message ->
                MessageBanner(message = message, onDismiss = viewModel::consumeMessage)
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Avatar(
                        initials = session.label.take(2).uppercase(),
                        size = 52.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.fg,
                        )
                        Text(
                            text = session.apiRole?.displayName ?: "Signed in",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.fgMuted,
                        )
                    }
                    StatusPill(
                        text = if (role == UserRole.ADMIN) "Admin" else "Logistics",
                        color = if (role == UserRole.ADMIN) colors.danger else colors.accent,
                    )
                }
            }

            MianuCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(title = "Backend", eyebrow = "Connection")
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (healthy) {
                        true -> {
                            LiveDot(color = colors.success)
                            Text(
                                "Connected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.success,
                            )
                        }
                        false -> {
                            LiveDot(color = colors.danger)
                            Text(
                                "Unreachable — check that the API is running",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.danger,
                            )
                        }
                        null -> Text(
                            "Checking…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.fgMuted,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                DetailRow(label = "Endpoint", value = com.mianu.paymentapp.network.RetrofitClient.ENDPOINT_HOST)
                DetailRow(label = "Session", value = if (session.userId != null) "Resolved" else "Partial")
            }

            if (role == UserRole.ADMIN) {
                MealWindowsCard(
                    windows = state.mealWindows,
                    isSaving = state.isSavingWindows,
                    onChange = viewModel::onWindowsChange,
                    onSave = viewModel::saveMealWindows,
                )
            }

            MianuSecondaryButton(
                text = "Sign out",
                onClick = {
                    SessionManager.signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Filled.Logout,
                tint = colors.danger,
            )

            Text(
                text = "MIANUCOM Operations · v0.1.0",
                style = MaterialTheme.typography.bodySmall,
                color = colors.fgMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun MealWindowsCard(
    windows: MealWindowRequest,
    isSaving: Boolean,
    onChange: (MealWindowRequest) -> Unit,
    onSave: () -> Unit,
) {
    val colors = MianuTheme.colors

    MianuCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Meal windows",
            eyebrow = "Service hours",
            subtitle = "When swipes are accepted",
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MianuTextField(
                value = windows.breakfastStart,
                onValueChange = { onChange(windows.copy(breakfastStart = it)) },
                label = "Breakfast from",
                placeholder = "07:00",
                modifier = Modifier.weight(1f),
            )
            MianuTextField(
                value = windows.breakfastEnd,
                onValueChange = { onChange(windows.copy(breakfastEnd = it)) },
                label = "until",
                placeholder = "10:00",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MianuTextField(
                value = windows.lunchStart,
                onValueChange = { onChange(windows.copy(lunchStart = it)) },
                label = "Lunch from",
                placeholder = "12:00",
                modifier = Modifier.weight(1f),
            )
            MianuTextField(
                value = windows.lunchEnd,
                onValueChange = { onChange(windows.copy(lunchEnd = it)) },
                label = "until",
                placeholder = "15:00",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "24-hour times, HH:MM.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.fgMuted,
        )

        Spacer(Modifier.height(14.dp))
        MianuButton(
            text = "Save windows",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            loading = isSaving,
            icon = Icons.Default.Save,
        )
    }
}
