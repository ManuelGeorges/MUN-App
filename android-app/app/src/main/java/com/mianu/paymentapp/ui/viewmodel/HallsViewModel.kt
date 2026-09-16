package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.HallResponse
import com.mianu.paymentapp.data.models.HallUpdate
import com.mianu.paymentapp.data.models.UserResponse
import com.mianu.paymentapp.data.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HallsState(
    val halls: UiState<List<HallResponse>> = UiState.Loading,
    val expandedHallId: String? = null,
    /** User ids currently inside, keyed by hall. */
    val presence: Map<String, List<String>> = emptyMap(),
    val loadingPresenceFor: String? = null,
    val directory: Map<String, UserResponse> = emptyMap(),
    val message: UiMessage? = null,
) {
    /** Resolves a presence id to a name, falling back to a short id when the user isn't cached. */
    fun displayName(userId: String): String =
        directory[userId]?.name ?: "Delegate ${userId.take(8)}"

    fun presenceFor(hallId: String): List<String> = presence[hallId].orEmpty()
}

class HallsViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(HallsState())
    val state: StateFlow<HallsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(halls = UiState.Loading) }
            when (val result = repo.getHalls()) {
                is ApiResult.Success -> _state.update { it.copy(halls = UiState.Success(result.data)) }
                is ApiResult.Failure -> _state.update { it.copy(halls = UiState.Error(result.error)) }
            }
            // The presence endpoint returns bare ids, so keep a directory to render names.
            (repo.getUsers() as? ApiResult.Success)?.let { users ->
                _state.update { it.copy(directory = users.data.associateBy { u -> u.id }) }
            }
        }
    }

    fun toggleHall(hallId: String) {
        val alreadyOpen = _state.value.expandedHallId == hallId
        _state.update { it.copy(expandedHallId = if (alreadyOpen) null else hallId) }
        if (!alreadyOpen) loadPresence(hallId)
    }

    fun loadPresence(hallId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loadingPresenceFor = hallId) }
            val result = repo.getHallPresence(hallId)
            _state.update { current ->
                current.copy(
                    loadingPresenceFor = null,
                    presence = current.presence + (hallId to (result as? ApiResult.Success)?.data.orEmpty()),
                )
            }
        }
    }

    fun createHall(
        name: String,
        capacity: Int,
        allowedRoles: List<UserRole>,
        onDone: () -> Unit = {},
    ) {
        viewModelScope.launch {
            when (val result = repo.createHall(name, capacity, allowedRoles)) {
                is ApiResult.Success -> {
                    notify("${result.data.name} added", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun updateHall(hallId: String, update: HallUpdate, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = repo.updateHall(hallId, update)) {
                is ApiResult.Success -> {
                    notify("${result.data.name} updated", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    private fun notify(text: String, tone: UiMessage.Tone) =
        _state.update { it.copy(message = UiMessage(text, tone)) }
}
