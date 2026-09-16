package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.data.models.BroadcastAudience
import com.mianu.paymentapp.data.models.NotificationResponse
import com.mianu.paymentapp.data.models.TeamResponse
import com.mianu.paymentapp.data.models.UserResponse
import com.mianu.paymentapp.ui.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BroadcastState(
    val message: String = "",
    val audience: BroadcastAudience = BroadcastAudience.TEAM,
    val teams: List<TeamResponse> = emptyList(),
    val recipients: List<UserResponse> = emptyList(),
    val selectedTeamId: String? = null,
    val selectedRecipientId: String? = null,
    val sent: List<NotificationResponse> = emptyList(),
    val isSending: Boolean = false,
    val isLoading: Boolean = true,
    val uiMessage: UiMessage? = null,
) {
    /** Only staff with event-wide reach may address everyone; leaders are confined to their team. */
    val canReachEveryone: Boolean
        get() = SessionManager.currentRole == UserRole.ADMIN ||
            SessionManager.currentRole == UserRole.ORGANIZER

    val audiences: List<BroadcastAudience>
        get() = if (canReachEveryone) {
            listOf(BroadcastAudience.ALL, BroadcastAudience.TEAM, BroadcastAudience.PARTICIPANT)
        } else {
            listOf(BroadcastAudience.TEAM, BroadcastAudience.PARTICIPANT)
        }

    val canSend: Boolean
        get() = message.isNotBlank() && !isSending && when (audience) {
            BroadcastAudience.ALL -> true
            BroadcastAudience.TEAM -> selectedTeamId != null
            BroadcastAudience.PARTICIPANT -> selectedRecipientId != null
        }
}

/**
 * Drives the broadcast console.
 *
 * The audience list is narrowed to what this account may actually use, so a leader isn't offered
 * "everyone" and then refused. The server still decides — its rejection text is shown verbatim
 * rather than being second-guessed here.
 */
class BroadcastViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(BroadcastState())
    val state: StateFlow<BroadcastState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val ownTeamId = SessionManager.state.value.teamId
            val teams = (repo.getTeams() as? ApiResult.Success)?.data.orEmpty()

            // A leader picks recipients from their own roster; wider roles from everyone.
            val recipients = if (_state.value.canReachEveryone) {
                (repo.getUsers() as? ApiResult.Success)?.data.orEmpty()
            } else {
                ownTeamId?.let { (repo.getTeamMembers(it) as? ApiResult.Success)?.data }.orEmpty()
            }

            _state.update { current ->
                current.copy(
                    isLoading = false,
                    teams = teams,
                    recipients = recipients,
                    selectedTeamId = current.selectedTeamId
                        ?: ownTeamId
                        ?: teams.firstOrNull()?.id,
                    selectedRecipientId = current.selectedRecipientId ?: recipients.firstOrNull()?.id,
                )
            }
            loadSent()
        }
    }

    private fun loadSent() {
        val senderId = SessionManager.currentUserId ?: return
        viewModelScope.launch {
            (repo.getSentNotifications(senderId) as? ApiResult.Success)?.let { result ->
                _state.update { it.copy(sent = result.data) }
            }
        }
    }

    fun onMessageChange(value: String) = _state.update { it.copy(message = value) }
    fun onAudienceChange(value: BroadcastAudience) = _state.update { it.copy(audience = value) }
    fun onTeamChange(id: String) = _state.update { it.copy(selectedTeamId = id) }
    fun onRecipientChange(id: String) = _state.update { it.copy(selectedRecipientId = id) }
    fun consumeMessage() = _state.update { it.copy(uiMessage = null) }

    fun send() {
        val current = _state.value
        val senderId = SessionManager.currentUserId
        if (!current.canSend) return
        if (senderId == null) {
            _state.update {
                it.copy(uiMessage = UiMessage("Not signed in.", UiMessage.Tone.Error))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val result = repo.broadcast(
                senderId = senderId,
                message = current.message.trim(),
                audience = current.audience,
                teamId = current.selectedTeamId.takeIf { current.audience == BroadcastAudience.TEAM },
                recipientId = current.selectedRecipientId
                    .takeIf { current.audience == BroadcastAudience.PARTICIPANT },
            )
            _state.update { it.copy(isSending = false) }

            when (result) {
                is ApiResult.Success -> {
                    val count = result.data.recipientCount
                    _state.update {
                        it.copy(
                            message = "",
                            uiMessage = UiMessage(
                                "Sent to $count recipient${if (count == 1) "" else "s"}.",
                                UiMessage.Tone.Success,
                            ),
                        )
                    }
                    loadSent()
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(uiMessage = UiMessage(result.error.message, UiMessage.Tone.Error))
                }
            }
        }
    }
}
