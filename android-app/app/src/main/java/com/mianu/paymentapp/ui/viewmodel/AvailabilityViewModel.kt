package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.HallAvailability
import com.mianu.paymentapp.data.models.HallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AvailabilityState(
    val halls: List<HallAvailability> = emptyList(),
    val isLoading: Boolean = true,
) {
    val summary: String
        get() {
            if (halls.isEmpty()) return "No halls configured"
            val free = halls.count { it.state == HallState.EMPTY || it.state == HallState.AVAILABLE }
            return "$free of ${halls.size} with room right now"
        }
}

/** Powers the team-member hall-availability screen. Read-only, refetch on demand. */
class AvailabilityViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(AvailabilityState())
    val state: StateFlow<AvailabilityState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repo.getHallAvailability()
            _state.update {
                it.copy(
                    isLoading = false,
                    halls = (result as? ApiResult.Success)?.data ?: it.halls,
                )
            }
        }
    }
}
