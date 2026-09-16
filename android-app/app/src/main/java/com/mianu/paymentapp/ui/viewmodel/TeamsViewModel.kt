package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.SessionManager
import com.mianu.paymentapp.data.models.BroadcastAudience
import com.mianu.paymentapp.data.models.NotificationResponse
import com.mianu.paymentapp.data.models.TeamResponse
import com.mianu.paymentapp.data.models.TeamUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.mianu.paymentapp.data.models.TeamRole
import com.mianu.paymentapp.data.models.UserResponse

data class TeamsState(
    val teams: UiState<List<TeamResponse>> = UiState.Loading,
    val selectedTeamId: String? = null,
    val teamMembers: List<UserResponse> = emptyList(),
    val allUsers: List<UserResponse> = emptyList(),
    val isLoadingMembers: Boolean = false,
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoadingNotifications: Boolean = false,
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val message: UiMessage? = null,
) {
    val selectedTeam: TeamResponse?
        get() = teams.dataOrNull?.firstOrNull { it.id == selectedTeamId }

    /**
     * Broadcasting needs a resolved user id for `sender_id`. It may be missing if the post-login
     * profile lookup didn't find a matching account.
     */
    val canBroadcast: Boolean
        get() = draftMessage.isNotBlank() && !isSending &&
            selectedTeamId != null && SessionManager.currentUserId != null
}

class TeamsViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(TeamsState())
    val state: StateFlow<TeamsState> = _state.asStateFlow()

    init {
        refresh()
        loadAllUsers()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(teams = UiState.Loading) }
            when (val result = repo.getTeams()) {
                is ApiResult.Success -> {
                    val nextSelectedId = _state.value.selectedTeamId?.takeIf { currentId ->
                        result.data.any { it.id == currentId }
                    } ?: result.data.firstOrNull()?.id

                    _state.update { current ->
                        current.copy(
                            teams = UiState.Success(result.data),
                            selectedTeamId = nextSelectedId,
                        )
                    }
                    nextSelectedId?.let { id ->
                        loadNotifications(id)
                        loadTeamMembers(id)
                    }
                }
                is ApiResult.Failure -> _state.update { it.copy(teams = UiState.Error(result.error)) }
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            when (val result = repo.getUsers()) {
                is ApiResult.Success -> _state.update { it.copy(allUsers = result.data) }
                is ApiResult.Failure -> Unit
            }
        }
    }

    fun selectTeam(teamId: String) {
        _state.update {
            it.copy(
                selectedTeamId = teamId,
                notifications = emptyList(),
                teamMembers = emptyList(),
            )
        }
        loadNotifications(teamId)
        loadTeamMembers(teamId)
    }

    fun loadTeamMembers(teamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMembers = true) }
            when (val result = repo.getTeamMembers(teamId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(isLoadingMembers = false, teamMembers = result.data)
                }
                is ApiResult.Failure -> _state.update { it.copy(isLoadingMembers = false) }
            }
        }
    }

    fun loadNotifications(teamId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingNotifications = true) }
            val result = repo.getTeamNotifications(teamId)
            _state.update {
                it.copy(
                    isLoadingNotifications = false,
                    notifications = (result as? ApiResult.Success)?.data.orEmpty()
                        // Newest first — the server returns insertion order.
                        .sortedByDescending { n -> n.timestamp },
                )
            }
        }
    }

    fun onDraftChange(value: String) = _state.update { it.copy(draftMessage = value) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun createTeam(name: String, capacity: Int, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repo.createTeam(name, capacity)) {
                is ApiResult.Success -> {
                    notify("Team ${result.data.name} created", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun updateTeam(teamId: String, name: String?, capacity: Int?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repo.updateTeam(teamId, TeamUpdate(name, capacity))) {
                is ApiResult.Success -> {
                    notify("Team updated", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun deleteTeam(teamId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repo.deleteTeam(teamId)) {
                is ApiResult.Success -> {
                    notify("Team deleted", UiMessage.Tone.Warning)
                    _state.update {
                        it.copy(selectedTeamId = null, teamMembers = emptyList(), notifications = emptyList())
                    }
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun assignMember(teamId: String, userId: String, role: TeamRole = TeamRole.MEMBER, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repo.assignMember(teamId, userId, role)) {
                is ApiResult.Success -> {
                    notify("Member added to team", UiMessage.Tone.Success)
                    loadTeamMembers(teamId)
                    refresh()
                    loadAllUsers()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun removeMember(teamId: String, userId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repo.removeMemberFromTeam(teamId, userId)) {
                is ApiResult.Success -> {
                    notify("Member removed from team", UiMessage.Tone.Success)
                    loadTeamMembers(teamId)
                    refresh()
                    loadAllUsers()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun broadcast() {
        val current = _state.value
        val teamId = current.selectedTeamId ?: return
        val senderId = SessionManager.currentUserId ?: run {
            notify("Can't identify your account — sign in again to broadcast.", UiMessage.Tone.Error)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val result = repo.broadcast(
                senderId = senderId,
                message = current.draftMessage.trim(),
                audience = BroadcastAudience.TEAM,
                teamId = teamId,
            )
            _state.update { it.copy(isSending = false) }
            when (result) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            draftMessage = "",
                            notifications = listOf(result.data) + it.notifications,
                            message = UiMessage("Broadcast sent", UiMessage.Tone.Success),
                        )
                    }
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    private fun notify(text: String, tone: UiMessage.Tone) =
        _state.update { it.copy(message = UiMessage(text, tone)) }
}
