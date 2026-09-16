package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.TeamResponse
import com.mianu.paymentapp.data.models.TeamRole
import com.mianu.paymentapp.data.models.UserCreate
import com.mianu.paymentapp.data.models.UserResponse
import com.mianu.paymentapp.data.models.UserRole
import com.mianu.paymentapp.data.models.UserUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersState(
    val users: UiState<List<UserResponse>> = UiState.Loading,
    val teams: List<TeamResponse> = emptyList(),
    val query: String = "",
    val roleFilter: UserRole? = null,
    val showInactive: Boolean = true,
    val message: UiMessage? = null,
    val isMutating: Boolean = false,
) {
    /** Search + filters applied to the loaded list. Empty while loading or on error. */
    val visibleUsers: List<UserResponse>
        get() {
            val all = users.dataOrNull ?: return emptyList()
            val term = query.trim().lowercase()
            return all.filter { user ->
                val matchesQuery = term.isEmpty() ||
                    user.name.lowercase().contains(term) ||
                    user.email?.lowercase()?.contains(term) == true ||
                    user.phone?.contains(term) == true
                val matchesRole = roleFilter == null || user.roleEnum == roleFilter
                val matchesActive = showInactive || user.isActive
                matchesQuery && matchesRole && matchesActive
            }
        }

    fun teamName(teamId: String?): String? =
        teamId?.let { id -> teams.firstOrNull { it.id == id }?.name }
}

class UsersViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersState())
    val state: StateFlow<UsersState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(users = UiState.Loading) }
            when (val result = repo.getUsers()) {
                is ApiResult.Success -> _state.update { it.copy(users = UiState.Success(result.data)) }
                is ApiResult.Failure -> _state.update { it.copy(users = UiState.Error(result.error)) }
            }
            // Team names are only used to label rows, so a failure here degrades quietly.
            (repo.getTeams() as? ApiResult.Success)?.let { teams ->
                _state.update { it.copy(teams = teams.data) }
            }
        }
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }
    fun onRoleFilterChange(role: UserRole?) = _state.update { it.copy(roleFilter = role) }
    fun toggleShowInactive() = _state.update { it.copy(showInactive = !it.showInactive) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun createUser(
        name: String,
        email: String?,
        phone: String?,
        password: String?,
        role: UserRole,
        mealAllowance: Int = 6,
        teamId: String? = null,
        onDone: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isMutating = true) }
            val result = repo.createUser(
                UserCreate(
                    name = name,
                    email = email?.takeIf { it.isNotBlank() },
                    phone = phone?.takeIf { it.isNotBlank() },
                    password = password?.takeIf { it.isNotBlank() },
                    tokenBalance = 0.0,
                    mealAllowance = mealAllowance,
                    mealsBalance = mealAllowance,
                    role = role,
                    teamId = teamId?.takeIf { it.isNotBlank() },
                    teamRole = if (teamId.isNullOrBlank()) null else TeamRole.MEMBER,
                ),
            )
            _state.update { it.copy(isMutating = false) }
            when (result) {
                is ApiResult.Success -> {
                    notify("${result.data.name} registered with $mealAllowance meals", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun adjustMeals(userId: String, delta: Int? = null, mealsBalance: Int? = null, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isMutating = true) }
            val result = repo.adjustUserMeals(userId, delta = delta, mealsBalance = mealsBalance)
            _state.update { it.copy(isMutating = false) }
            when (result) {
                is ApiResult.Success -> {
                    notify("${result.data.name}: ${result.data.mealsBalance} meals remaining", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun updateUser(userId: String, update: UserUpdate, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isMutating = true) }
            val result = repo.updateUser(userId, update)
            _state.update { it.copy(isMutating = false) }
            when (result) {
                is ApiResult.Success -> {
                    notify("Saved", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun toggleActive(user: UserResponse) {
        viewModelScope.launch {
            when (val result = repo.setUserActive(user.id, !user.isActive)) {
                is ApiResult.Success -> {
                    val verb = if (result.data.isActive) "reactivated" else "suspended"
                    notify("${result.data.name} $verb", UiMessage.Tone.Success)
                    refresh()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun deleteUser(user: UserResponse) {
        viewModelScope.launch {
            when (val result = repo.deleteUser(user.id)) {
                is ApiResult.Success -> {
                    notify("${user.name} removed", UiMessage.Tone.Warning)
                    refresh()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun linkCard(userId: String, cardUid: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isMutating = true) }
            val result = repo.linkCard(userId, cardUid)
            _state.update { it.copy(isMutating = false) }
            when (result) {
                is ApiResult.Success -> {
                    notify("Card linked to ${result.data.user.name}", UiMessage.Tone.Success)
                    refresh()
                    onDone()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun assignToTeam(userId: String, teamId: String, role: TeamRole) {
        viewModelScope.launch {
            when (val result = repo.assignMember(teamId, userId, role)) {
                is ApiResult.Success -> {
                    notify("Team assignment updated", UiMessage.Tone.Success)
                    refresh()
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    private fun notify(text: String, tone: UiMessage.Tone) =
        _state.update { it.copy(message = UiMessage(text, tone)) }
}
