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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mianu.paymentapp.data.models.BroadcastAudience
import com.mianu.paymentapp.data.models.NotificationResponse
import com.mianu.paymentapp.ui.components.EmptyState
import com.mianu.paymentapp.ui.components.FilterPill
import com.mianu.paymentapp.ui.components.GlassPanel
import com.mianu.paymentapp.ui.components.MianuButton
import com.mianu.paymentapp.ui.components.MianuCard
import com.mianu.paymentapp.ui.components.MianuTextField
import com.mianu.paymentapp.ui.components.SectionHeader
import com.mianu.paymentapp.ui.components.StatusPill
import com.mianu.paymentapp.ui.theme.AmbientBackground
import com.mianu.paymentapp.ui.theme.MianuTheme
import com.mianu.paymentapp.ui.viewmodel.BroadcastViewModel
import com.mianu.paymentapp.ui.viewmodel.UiMessage

/**
 * Broadcast console.
 *
 * The audience picker offers only what this account may actually use — a leader never sees
 * "everyone" — but the send button's fate is the server's to decide; a rejection is surfaced as-is.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroadcastScreen(viewModel: BroadcastViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MianuTheme.colors

    // One-shot result banner. The Android side has no toast host, so success/failure rides the
    // section as a StatusPill and clears on the next send.
    LaunchedEffect(state.uiMessage?.id) {
        if (state.uiMessage != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.consumeMessage()
        }
    }

    AmbientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SectionHeader(
                title = "Broadcast",
                eyebrow = "Messaging",
                subtitle = "Send an announcement",
            )

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Audience",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.fgMuted,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.audiences.forEach { audience ->
                        FilterPill(
                            text = audienceLabel(audience),
                            selected = state.audience == audience,
                            onClick = { viewModel.onAudienceChange(audience) },
                        )
                    }
                }

                if (state.audience == BroadcastAudience.TEAM && state.canReachEveryone) {
                    Spacer(Modifier.height(14.dp))
                    LabelledPicker(
                        label = "Team",
                        options = state.teams.map { it.id to it.name },
                        selectedId = state.selectedTeamId,
                        onSelect = viewModel::onTeamChange,
                    )
                }

                if (state.audience == BroadcastAudience.PARTICIPANT) {
                    Spacer(Modifier.height(14.dp))
                    LabelledPicker(
                        label = "Participant",
                        options = state.recipients.map { it.id to it.name },
                        selectedId = state.selectedRecipientId,
                        onSelect = viewModel::onRecipientChange,
                    )
                }

                Spacer(Modifier.height(14.dp))
                MianuTextField(
                    value = state.message,
                    onValueChange = viewModel::onMessageChange,
                    label = "Message",
                    placeholder = "Opening ceremony starts in 15 minutes…",
                    enabled = !state.isSending,
                )

                Spacer(Modifier.height(14.dp))
                MianuButton(
                    text = "Send broadcast",
                    onClick = viewModel::send,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSend,
                    loading = state.isSending,
                    icon = Icons.Default.Campaign,
                    glow = true,
                )

                state.uiMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    StatusPill(
                        text = message.text,
                        color = if (message.tone == UiMessage.Tone.Error) colors.danger else colors.success,
                    )
                }
            }

            SectionHeader(title = "Recently sent", eyebrow = "Your messages")

            if (state.sent.isEmpty()) {
                MianuCard {
                    EmptyState(
                        title = "Nothing sent yet",
                        description = "Messages you send will appear here.",
                        icon = Icons.Default.Campaign,
                    )
                }
            } else {
                state.sent.forEach { SentRow(it) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelledPicker(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    val colors = MianuTheme.colors
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = colors.fgMuted)
        Spacer(Modifier.height(6.dp))
        if (options.isEmpty()) {
            Text(
                text = "None available",
                style = MaterialTheme.typography.bodySmall,
                color = colors.fgMuted,
            )
        } else {
            // A wrap of pills rather than a dropdown: rosters here are small, and pills keep the
            // choice one tap away without a menu that would obscure the message field below.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { (id, name) ->
                    FilterPill(text = name, selected = id == selectedId, onClick = { onSelect(id) })
                }
            }
        }
    }
}

@Composable
private fun SentRow(notification: NotificationResponse) {
    val colors = MianuTheme.colors
    MianuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.fg,
                )
                Text(
                    text = "${notification.audienceLabel} · ${notification.recipientCount} reached",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.fgMuted,
                )
            }
        }
    }
}

private fun audienceLabel(audience: BroadcastAudience): String = when (audience) {
    BroadcastAudience.ALL -> "Everyone"
    BroadcastAudience.TEAM -> "A team"
    BroadcastAudience.PARTICIPANT -> "One person"
}
